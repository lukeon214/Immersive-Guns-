package com.imguns.guns.network.message;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ClientMessagePlayerZoom implements FabricPacket {
    public static final PacketType<ClientMessagePlayerZoom> TYPE = PacketType.create(new Identifier(GunMod.MOD_ID, "client_message_player_zoom"), ClientMessagePlayerZoom::new);

    public ClientMessagePlayerZoom(PacketByteBuf buf) {
        this();
    }

    public ClientMessagePlayerZoom() {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    public void handle(ServerPlayerEntity player, PacketSender ignoredSender) {
        if (player == null) return;
        IGunOperator.fromLivingEntity(player).zoom();
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
