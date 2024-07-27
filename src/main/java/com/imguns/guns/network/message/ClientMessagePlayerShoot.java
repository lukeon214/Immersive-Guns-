package com.imguns.guns.network.message;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ClientMessagePlayerShoot implements FabricPacket {
    public static final PacketType<ClientMessagePlayerShoot> TYPE = PacketType.create(new Identifier(GunMod.MOD_ID, "client_message_player_shoot"), ClientMessagePlayerShoot::new);

    public ClientMessagePlayerShoot(PacketByteBuf buf) {
        this();
    }

    public ClientMessagePlayerShoot() {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    public void handle(ServerPlayerEntity player, PacketSender ignoredSender) {
        if (player == null) return;
        IGunOperator.fromLivingEntity(player).shoot(player::getPitch, player::getYaw);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
