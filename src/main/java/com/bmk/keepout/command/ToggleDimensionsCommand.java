package com.bmk.keepout.command;

import com.bmk.keepout.config.ConfigManager;
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

import java.util.ArrayList;
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

    private static ChatFormatting parseColor(String code, ChatFormatting fallback) {
        if (code == null || code.isEmpty()) return fallback;

        char c = Character.toLowerCase(code.charAt(0));
        boolean isColorCode = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f');
        if (!isColorCode) return fallback;

        ChatFormatting parsed = ChatFormatting.getByCode(c);
        return parsed != null ? parsed : fallback;
    }

    public static int nether(CommandContext<CommandSourceStack> context) {
        var cfg = ConfigManager.get();
        var server = context.getSource().getServer();
        var rules = server.getGameRules();
        boolean newValue = !rules.get(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS);
        rules.set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, newValue, server);

        cfg.netherEnabled = newValue;
        ConfigManager.updateBoolen("netherEnabled", newValue);

        if (newValue) {
            ChatFormatting primary = parseColor(cfg.netherPrimaryColor, ChatFormatting.DARK_RED);
            ChatFormatting secondary = parseColor(cfg.netherSecondaryColor, ChatFormatting.RED);
            Component title = buildTitle(cfg.netherEnabledTitle, secondary, primary);
            playWithLeadIn(server.getPlayerList().getPlayers(), title,
                    new SoundCue(SoundEvents.AMETHYST_BLOCK_CHIME, 5.0f),
                    new SoundCue(SoundEvents.WITHER_SPAWN, 0.8f),
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
        var cfg = ConfigManager.get();

        cfg.endEnabled = newValue;
        ConfigManager.updateBoolen("endEnabled", newValue);

        if (newValue) {
            ChatFormatting primary = parseColor(cfg.endPrimaryColor, ChatFormatting.LIGHT_PURPLE);
            ChatFormatting secondary = parseColor(cfg.endSecondaryColor, ChatFormatting.DARK_PURPLE);
            Component title = buildTitle(cfg.endEnabledTitle, secondary, primary);
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
        var cfg = ConfigManager.get();

        List<ChatFormatting> mainStyle = new ArrayList<>();
        mainStyle.add(mainColor);
        if (cfg.boldTitle) mainStyle.add(ChatFormatting.BOLD);
        if (cfg.italicTitle) mainStyle.add(ChatFormatting.ITALIC);
        if (cfg.underlineTitle) mainStyle.add(ChatFormatting.UNDERLINE);

        ChatFormatting[] styleArray = mainStyle.toArray(new ChatFormatting[0]);

        if (cfg.decorateTitle) {
            String decorator = " ! ";

            return Component.empty()
                    .append(Component.literal(decorator).withStyle(ChatFormatting.OBFUSCATED, edgeColor))
                    .append(Component.literal(text).withStyle(styleArray))
                    .append(Component.literal(decorator).withStyle(ChatFormatting.OBFUSCATED, edgeColor));
        } else {
            return Component.empty()
                    .append(Component.literal(text).withStyle(styleArray));
        }
    }

    private static void playWithLeadIn(List<ServerPlayer> players, Component title, SoundCue... hitSounds) {
        var cfg = ConfigManager.get();
        if (cfg.playSounds) {
            for (ServerPlayer p : players) {
                p.level().playSound(null, p.getX(), p.getY(), p.getZ(),
                        SoundEvents.GOAT_HORN_SOUND_VARIANTS.getFirst(), SoundSource.MASTER, 1.0f, 1.0f); // lead-in
            }
        }

        TickScheduler.schedule(cfg.leadInDelayTicks, () -> {
            for (ServerPlayer p : players) {
                if (cfg.showTitle) {
                    p.connection.send(new ClientboundSetTitleTextPacket(title));
                    p.connection.send(new ClientboundSetTitlesAnimationPacket(cfg.titleFadeIn, cfg.titleStay, cfg.titleFadeOut));
                }
                if (cfg.playSounds) {
                    for (SoundCue cue : hitSounds) {
                        p.level().playSound(null, p.getX(), p.getY(), p.getZ(),
                                cue.sound(), SoundSource.MASTER, cue.volume(), 1.0f);
                    }
                }
            }
        });
    }
}