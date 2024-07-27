package com.imguns.guns.entity.sync.core;

import com.google.common.collect.ImmutableSet;
import com.imguns.guns.GunMod;
import com.imguns.guns.init.CommonRegistry;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish.
 * Open source at <a href="https://github.com/MrCrayfish/Framework">Github</a> under LGPL License.
 */
public class SyncedEntityData {
    private static final Marker SYNCED_ENTITY_DATA_MARKER = MarkerFactory.getMarker("SYNCED_ENTITY_DATA_TAC_COPY");
    private static SyncedEntityData INSTANCE;

    private final Set<SyncedClassKey<?>> registeredClassKeys = new HashSet<>();
    private final Object2ObjectMap<Identifier, SyncedClassKey<?>> idToClassKey = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, SyncedClassKey<?>> classNameToClassKey = new Object2ObjectOpenHashMap<>();
    private final Object2BooleanMap<String> clientClassNameCapabilityCache = new Object2BooleanOpenHashMap<>();
    private final Object2BooleanMap<String> serverClassNameCapabilityCache = new Object2BooleanOpenHashMap<>();

    private final Set<SyncedDataKey<?, ?>> registeredDataKeys = new HashSet<>();
    private final Reference2ObjectMap<SyncedClassKey<?>, HashMap<Identifier, SyncedDataKey<?, ?>>> classToKeys = new Reference2ObjectOpenHashMap<>();
    private final Reference2IntMap<SyncedDataKey<?, ?>> internalIds = new Reference2IntOpenHashMap<>();
    private final Int2ReferenceMap<SyncedDataKey<?, ?>> syncedIdToKey = new Int2ReferenceOpenHashMap<>();

    private final AtomicInteger nextIdTracker = new AtomicInteger();
    private final List<Entity> dirtyEntities = new ArrayList<>();
    private boolean dirty = false;

    private SyncedEntityData() {
    }

    public static SyncedEntityData instance() {
        if (INSTANCE == null) {
            INSTANCE = new SyncedEntityData();
        }
        return INSTANCE;
    }

    private <E extends Entity> void registerClassKey(SyncedClassKey<E> classKey) {
        if (!this.registeredClassKeys.contains(classKey)) {
            this.registeredClassKeys.add(classKey);
            this.idToClassKey.put(classKey.id(), classKey);
            this.classNameToClassKey.put(classKey.entityClass().getName(), classKey);
        }
    }

    /**
     * Registers a synced data key into the system.
     *
     * @param dataKey a synced data key instance
     */
    public synchronized <E extends Entity, T> void registerDataKey(SyncedDataKey<E, T> dataKey) {
        Identifier keyId = dataKey.id();
        SyncedClassKey<E> classKey = dataKey.classKey();
        if (CommonRegistry.isLoadComplete()) {
            throw new IllegalStateException(String.format("Tried to register synced data key %s for %s after game initialization", keyId, classKey.id()));
        }
        if (this.registeredDataKeys.contains(dataKey)) {
            throw new IllegalArgumentException(String.format("The synced data key %s for %s is already registered", keyId, classKey.id()));
        }
        // Attempt to register the class key. Will ignore if already registered.
        this.registerClassKey(dataKey.classKey());
        this.registeredDataKeys.add(dataKey);
        this.classToKeys.computeIfAbsent(classKey, c -> new HashMap<>()).put(keyId, dataKey);
        int nextId = this.nextIdTracker.getAndIncrement();
        this.internalIds.put(dataKey, nextId);
        this.syncedIdToKey.put(nextId, dataKey);
        GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered synced data key {} for {}", dataKey.id(), classKey.id());
    }

    /**
     * Sets the value of a synced data key to the specified player
     *
     * @param entity the player to assign the value to
     * @param key    a registered synced data key
     * @param value  a new value that matches the synced data key type
     */
    public <E extends Entity, T> void set(E entity, SyncedDataKey<?, ?> key, T value) {
        if (!this.registeredDataKeys.contains(key)) {
            String keys = this.registeredDataKeys.stream().map(k -> k.pairKey().toString()).collect(Collectors.joining(",", "[", "]"));
            GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered keys before throwing exception: {}", keys);
            throw new IllegalArgumentException(String.format("The synced data key %s for %s is not registered!", key.id(), key.classKey().id()));
        }
        DataHolder holder = this.getDataHolder(entity);
        if (holder != null && holder.set(entity, key, value)) {
            if (!entity.getWorld().isClient()) {
                this.dirty = true;
                this.dirtyEntities.add(entity);
            }
        }
    }

    /**
     * Gets the value for the synced data key from the specified player. It is best to check that
     * the player is alive before getting the value.
     *
     * @param entity the player to retrieve the data from
     * @param key    a registered synced data key
     */
    public <E extends Entity, T> T get(E entity, SyncedDataKey<E, T> key) {
        if (!this.registeredDataKeys.contains(key)) {
            String keys = this.registeredDataKeys.stream().map(k -> k.pairKey().toString()).collect(Collectors.joining(",", "[", "]"));
            GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Registered keys before throwing exception: {}", keys);
            throw new IllegalArgumentException(String.format("The synced data key %s for %s is not registered!", key.id(), key.classKey().id()));
        }
        DataHolder holder = this.getDataHolder(entity);
        return holder != null ? holder.get(key) : key.defaultValueSupplier().get();
    }

    public int getInternalId(SyncedDataKey<?, ?> key) {
        return this.internalIds.getInt(key);
    }

    @Nullable
    public SyncedClassKey<?> getClassKey(Identifier id) {
        return idToClassKey.get(id);
    }

    @Nullable
    public SyncedDataKey<?, ?> getKey(int id) {
        return this.syncedIdToKey.get(id);
    }

    @Nullable
    public SyncedDataKey<?, ?> getKey(SyncedClassKey<?> classKey, Identifier dataKey) {
        Map<Identifier, SyncedDataKey<?, ?>> keys = SyncedEntityData.instance().classToKeys.get(classKey);
        if (keys == null) {
            return null;
        }
        return keys.get(dataKey);
    }

    public Set<SyncedDataKey<?, ?>> getKeys() {
        return ImmutableSet.copyOf(this.registeredDataKeys);
    }

    @Nullable
    public DataHolder getDataHolder(Entity entity) {
        return DataHolderCapabilityProvider.CAPABILITY
                .maybeGet(entity)
                .map(DataHolderCapabilityProvider::getDataHolder)
                .orElse(null);
    }

    public boolean hasSyncedDataKey(Class<? extends Entity> entityClass) {
        // Gets the class name capability cache for the effective side.
        // This is needed to avoid concurrency issue due to client and server threads;
        // fast util does not support concurrent maps.
        Object2BooleanMap<String> cache = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
                ? this.clientClassNameCapabilityCache : this.serverClassNameCapabilityCache;
        // It's possible that the entity doesn't have a key, but it's superclass or subsequent does have a synced data key.
        // In order to prevent checking this every time we attach the capability, a simple one time check can be performed then cache the result.
        return cache.computeIfAbsent(entityClass.getName(), c -> {
            Class<?> targetClass = entityClass;
            // Should be good enough
            while (!targetClass.isAssignableFrom(Entity.class)) {
                if (this.classNameToClassKey.containsKey(targetClass.getName())) {
                    return true;
                }
                targetClass = targetClass.getSuperclass();
            }
            return false;
        });
    }

    public boolean updateMappings(Map<Identifier, List<Pair<Identifier, Integer>>> keyMap) {
        this.syncedIdToKey.clear();

        List<Pair<Identifier, Identifier>> missingKeys = new ArrayList<>();
        keyMap.forEach((classId, list) -> {
            SyncedClassKey<?> classKey = this.idToClassKey.get(classId);
            if (classKey == null || !this.classToKeys.containsKey(classKey)) {
                list.forEach(pair -> missingKeys.add(Pair.of(classId, pair.getLeft())));
                return;
            }

            Map<Identifier, SyncedDataKey<?, ?>> keys = this.classToKeys.get(classKey);
            list.forEach(pair -> {
                SyncedDataKey<?, ?> syncedDataKey = keys.get(pair.getLeft());
                if (syncedDataKey == null) {
                    missingKeys.add(Pair.of(classId, pair.getLeft()));
                    return;
                }
                this.syncedIdToKey.put((int) pair.getRight(), syncedDataKey);
            });
        });

        if (!missingKeys.isEmpty()) {
            String keys = missingKeys.stream().map(Object::toString).collect(Collectors.joining(",", "[", "]"));
            GunMod.LOGGER.info(SYNCED_ENTITY_DATA_MARKER, "Received unknown synced keys: {}", keys);
        }

        return missingKeys.isEmpty();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public List<Entity> getDirtyEntities() {
        return dirtyEntities;
    }
}
