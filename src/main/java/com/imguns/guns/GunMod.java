package com.imguns.guns;

import com.imguns.guns.api.resource.ResourceManager;
import com.imguns.guns.config.ClientConfig;
import com.imguns.guns.config.CommonConfig;
import com.imguns.guns.config.ServerConfig;
import com.imguns.guns.init.*;
import com.imguns.guns.resource.DedicatedServerReloadManager;
import com.imguns.guns.util.EnvironmentUtil;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;

import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GunMod implements ModInitializer {
	public static final String ORIGINAL_MOD_ID = "imguns";
	public static final String MOD_ID = "immersive_guns";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	/**
	 * 默认模型包文件夹
	 */
	public static final String DEFAULT_GUN_PACK_NAME = "default_guns";

	@Override
	public void onInitialize() {
		ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.COMMON, CommonConfig.init());
		ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ServerConfig.init());
		ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, ClientConfig.init());

		ModEvents.init();
		ModBlocks.init();
		ModCreativeTabs.init();
		ModItems.init();
		ModEntities.init();
		ModRecipe.init();
		ModContainer.init();
		ModSounds.init();
		ModParticles.init();
		ModDamageTypes.init();
		CommonRegistry.init();
		CommandRegistry.init();
		CompatRegistry.init();

		GunModComponents.init();

		registerDefaultExtraGunPack();

		if (EnvironmentUtil.isServer()) {
			DedicatedServerReloadManager.loadGunPack();
		}
	}

	private static void registerDefaultExtraGunPack() {
		String jarDefaultPackPath = String.format("/assets/%s/custom/%s", GunMod.MOD_ID, DEFAULT_GUN_PACK_NAME);
		ResourceManager.registerExtraGunPack(GunMod.class, jarDefaultPackPath);
	}
}