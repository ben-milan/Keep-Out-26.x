package com.bmk.keepout.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class EndPortalBlocker {
    public static volatile boolean endEnabled = true;

    public static void register() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (endEnabled) return InteractionResult.PASS;
            if(!player.getItemInHand(hand).is(Items.ENDER_EYE)) return InteractionResult.PASS;
            if (!level.getBlockState(hitResult.getBlockPos()).is(Blocks.END_PORTAL_FRAME)) return InteractionResult.PASS;
            return InteractionResult.FAIL;
        });
    }
}
