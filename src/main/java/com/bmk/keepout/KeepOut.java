package com.bmk.keepout;


import com.bmk.keepout.command.ToggleDimensionsCommand;
import com.bmk.keepout.config.ConfigManager;
import com.bmk.keepout.event.EndPortalBlocker;
import com.bmk.keepout.util.TickScheduler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeepOut implements ModInitializer {
	public static final String MOD_ID = "keepout";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ConfigManager.load();
		var cfg = ConfigManager.get();

		EndPortalBlocker.endEnabled = ConfigManager.get().endEnabled;

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			server.getGameRules().set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, cfg.netherEnabled, server);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ToggleDimensionsCommand.register(dispatcher);
			ReloadCommand.register(dispatcher);
		});

		TickScheduler.register();
		EndPortalBlocker.register();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
