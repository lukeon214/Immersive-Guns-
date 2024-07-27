package com.imguns.guns.network.message;

import com.imguns.guns.GunMod;
import com.imguns.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ClientMessagePlayerDrawGun implements FabricPacket {
    public static final PacketType<ClientMessagePlayerDrawGun> TYPE = PacketType.create(new Identifier(GunMod.MOD_ID, "client_message_player_draw_gun"), ClientMessagePlayerDrawGun::new);

    public ClientMessagePlayerDrawGun(PacketByteBuf buf) {
        this();
    }

    public ClientMessagePlayerDrawGun() {
    }

    @Override
    public void write(PacketByteBuf buf) {
    }

    public void handle(ServerPlayerEntity player, PacketSender ignoredSender) {
        if (player == null) return;
        PlayerInventory inventory = player.getInventory();
        int selected = inventory.selectedSlot;
        IGunOperator.fromLivingEntity(player).draw(() -> inventory.getStack(selected));
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
