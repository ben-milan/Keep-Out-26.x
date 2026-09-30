package com.bmk.keepout.command;

import com.bmk.keepout.event.EndPortalBlocker;
import com.bmk.keepout.util.SoundCue;
import com.bmk.keepout.util.TickScheduler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.List;

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

    public static int nether(CommandContext<CommandSourceStack> context) {
        var server = context.getSource().getServer();
        var rules = server.getGameRules();
        boolean newValue = !rules.get(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS);
        rules.set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, newValue, server);

        if (newValue) {
            Component title = buildTitle("! Hell Unleashed !", ChatFormatting.RED, ChatFormatting.DARK_RED);
            playWithLeadIn(server.getPlayerList().getPlayers(), title,
                    new SoundCue(SoundEvents.AMETHYST_BLOCK_CHIME, 5.0f),
                    new SoundCue(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f),
                    new SoundCue(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(3).value(), 0.8f),
                    new SoundCue(SoundEvents.BLAZE_AMBIENT, 0.4f));
        }

        context.getSource().sendSuccess(() -> Component.literal("Nether is now " + (newValue ? "enabled" : "disabled")), true);
        return 1;
    }

    public static int end(CommandContext<CommandSourceStack> context) {
        EndPortalBlocker.endEnabled = !EndPortalBlocker.endEnabled;
        boolean newValue = EndPortalBlocker.endEnabled;
        var server = context.getSource().getServer();

        if (newValue) {
            Component title = buildTitle("! End Unlocked !", ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE);
            playWithLeadIn(server.getPlayerList().getPlayers(), title,
                    new SoundCue(SoundEvents.AMETHYST_BLOCK_CHIME, 5.0f),
                    new SoundCue(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.8f),
                    new SoundCue(SoundEvents.PLAYER_LEVELUP, 0.8f),
                    new SoundCue(SoundEvents.ENDER_DRAGON_AMBIENT, 0.4f));
        }

        context.getSource().sendSuccess(() -> Component.literal("End is now " + (newValue ? "enabled" : "disabled")), true);
        return 1;
    }

    private static Component buildTitle(String text, ChatFormatting edgeColor, ChatFormatting mainColor) {
        String first = text.substring(0, 1);
        String middle = text.substring(1, text.length() - 1);
        String last = text.substring(text.length() - 1);

        return Component.empty()
                .append(Component.literal(first).withStyle(ChatFormatting.OBFUSCATED, edgeColor))
                .append(Component.literal(middle).withStyle(mainColor, ChatFormatting.ITALIC, ChatFormatting.BOLD))
                .append(Component.literal(last).withStyle(ChatFormatting.OBFUSCATED, edgeColor));
    }

    private static void playWithLeadIn(List<ServerPlayer> players, Component title, SoundCue... hitSounds) {
        for (ServerPlayer p : players) {
            p.level().playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.GOAT_HORN_SOUND_VARIANTS.getFirst(), SoundSource.MASTER, 1.0f, 1.0f); // lead-in
        }

        TickScheduler.schedule(20, () -> {
            for (ServerPlayer p : players) {
                p.connection.send(new ClientboundSetTitleTextPacket(title));
                p.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                for (SoundCue cue : hitSounds) {
                    p.level().playSound(null, p.getX(), p.getY(), p.getZ(),
                            cue.sound(), SoundSource.MASTER, cue.volume(), 1.0f);
                }
            }
        });
    }
}