package com.bmk.keepout.command;
import com.bmk.keepout.event.EndPortalBlocker;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.gamerules.GameRules;


public class ToggleDimensionsCommand {
    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_ADMIN);

    public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("toggle")
                        .requires(Commands.hasPermission(PERMISSION_CHECK))
                        .then(Commands.literal("nether")
                                .executes(ToggleDimensionsCommand::nether))
                        .then(Commands.literal("end")
                                .executes(ToggleDimensionsCommand::end))
        );
    }

    public static int nether (CommandContext<CommandSourceStack> context) {
        var server = context.getSource().getServer();
        var rules =  server.getGameRules();
        var rule = rules.get(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS);

        boolean newValue = !rule;

        rules.set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, newValue, server);

        context.getSource().sendSuccess(() -> Component.literal("Nether is now " + (newValue ? "enabled" : "disabled")), true);
        return 1;

    }

    public static int end (CommandContext<CommandSourceStack> context) {
        EndPortalBlocker.endEnabled = !EndPortalBlocker.endEnabled;
        boolean newValue = EndPortalBlocker.endEnabled;

        context.getSource().sendSuccess(() -> Component.literal("End is now " + (newValue ? "enabled" : "disabled")), true);
        return 1;
    }
}