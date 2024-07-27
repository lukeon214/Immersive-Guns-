package com.imguns.guns.init;

import com.imguns.guns.GunMod;
import com.imguns.guns.block.GunSmithTableBlock;
import com.imguns.guns.block.entity.GunSmithTableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static Block GUN_SMITH_TABLE = register("gun_smith_table", new GunSmithTableBlock());

    public static BlockEntityType<GunSmithTableBlockEntity> GUN_SMITH_TABLE_BE = register("gun_smith_table", GunSmithTableBlockEntity.TYPE);

    public static void init() {
    }

    private static Block register(String path, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(GunMod.MOD_ID, path), block);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String path, BlockEntityType<T> entityType) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(GunMod.MOD_ID, path), entityType);
    }
}
