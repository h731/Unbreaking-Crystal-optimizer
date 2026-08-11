package com.unbreaking.crystal.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPredictionFixMixin {

    private int lastTick = -1;
    private boolean anchorPlacedThisTick = false;
    private boolean glowstoneChargedThisTick = false;

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (player.clientWorld == null) return;

        int currentTick = player.age;
        if (currentTick != lastTick) {
            lastTick = currentTick;
            anchorPlacedThisTick = false;
            glowstoneChargedThisTick = false;
        }

        BlockState targetState = player.clientWorld.getBlockState(hitResult.getBlockPos());
        boolean isHoldingAnchor = player.getStackInHand(hand).isOf(Items.RESPAWN_ANCHOR);
        boolean isHoldingGlowstone = player.getStackInHand(hand).isOf(Items.GLOWSTONE);

        // 1. الأنكر الأول فوري محلياً، والأنكر الثاني عبر الباكيت فقط (يمنع الصوت المزدوج والبلوك الكاذب)
        if (isHoldingAnchor) {
            if (anchorPlacedThisTick) {
                player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, 0));
                player.swingHand(hand);
                cir.setReturnValue(ActionResult.FAIL);
                return;
            }
            anchorPlacedThisTick = true;
        }

        // 2. الشحنة الأولى فورية وسريعة، والشحنة الثانية في نفس التيك تسأل السيرفر فقط (تمنع الشحن الوهمي والصوت المدبل)
        if (targetState.isOf(Blocks.RESPAWN_ANCHOR) && isHoldingGlowstone) {
            if (glowstoneChargedThisTick) {
                player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult, 0));
                player.swingHand(hand);
                cir.setReturnValue(ActionResult.FAIL);
                return;
            }
            glowstoneChargedThisTick = true;
        }
    }
}