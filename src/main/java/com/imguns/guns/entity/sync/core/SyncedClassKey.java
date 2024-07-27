package com.imguns.guns.entity.sync.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Author: MrCrayfish.
 * Open source at <a href="https://github.com/MrCrayfish/Framework">Github</a> under LGPL License.
 */
public record SyncedClassKey<E extends Entity>(Class<E> entityClass, Identifier id) {
    public static final SyncedClassKey<LivingEntity> LIVING_ENTITY = new SyncedClassKey<>(LivingEntity.class, new Identifier("living_entity"));

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SyncedClassKey<?> that = (SyncedClassKey<?>) o;
        return this.entityClass.getName().equals(that.entityClass.getName());
    }

    @Override
    public int hashCode() {
        return this.entityClass.getName().hashCode();
    }
}
