package com.imguns.guns.network.message.event;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.LogicalSide;
import com.imguns.guns.api.event.common.GunFireEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ServerMessageGunFire implements FabricPacket {
    public static final PacketType<ServerMessageGunFire> TYPE = PacketType.create(new Identifier(GunMod.MOD_ID, "server_message_gun_fire"), ServerMessageGunFire::new);
    private final int shooterId;
    private final ItemStack gunItemStack;

    public ServerMessageGunFire(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readItemStack());
    }

    public ServerMessageGunFire(int shooterId, ItemStack gunItemStack) {
        this.shooterId = shooterId;
        this.gunItemStack = gunItemStack;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(shooterId);
        buf.writeItemStack(gunItemStack);
    }

    public void handle(PlayerEntity ignoredPlayer, PacketSender ignoredSender) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            doClientEvent(this);
        }
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    private static void doClientEvent(ServerMessageGunFire message) {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (level == null) {
            return;
        }
        if (level.getEntityById(message.shooterId) instanceof LivingEntity shooter) {
            GunFireEvent gunFireEvent = new GunFireEvent(shooter, message.gunItemStack, LogicalSide.CLIENT);
            gunFireEvent.post();
        }
    }
}
