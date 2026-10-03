package com.bmk.keepout.command;

import com.bmk.keepout.config.ConfigManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;

public class ReloadModCommand {
    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_ADMIN);


    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("keepout")
                        .requires(Commands.hasPermission(PERMISSION_CHECK))
                        .then(Commands.literal("reload")
                                .executes(ReloadModCommand::reload))
        );
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        try {
            ConfigManager.load();
            context.getSource().sendSuccess( () -> Component.literal("Keepout config reloaded from keepout.properties"), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(
                    Component.literal("Failed to reload Keepout config. Check server console for details")
            );
            e.printStackTrace();
            return 0;
        }
    }
}
