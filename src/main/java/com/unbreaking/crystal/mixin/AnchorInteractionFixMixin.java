package com.unbreaking.crystal.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class AnchorInteractionFixMixin {

    private long lastAnchorInteractTime = 0;

    // 1. حل مشكلة الأصوات وإرسال الشحن المزدوج (Spam Prevention)
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void preventSpamAndDoubleSound(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        BlockState state = client.world.getBlockState(hitResult.getBlockPos());
        
        if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
            long currentTime = System.currentTimeMillis();
            // كول داون 50 ملي ثانية (بمقدار 1 Tick) لمنع اللعبة من مضاعفة النقرات وتداخل الأصوات
            if (currentTime - lastAnchorInteractTime < 50) {
                cir.setReturnValue(ActionResult.CONSUME); // نلغي النفرة الزائدة بصمت
                return;
            }
            lastAnchorInteractTime = currentTime;
        }
    }

    // 2. حل مشكلة بناء الغوست أنكر (Ghost Anchor Prevention)
    @Inject(method = "interactBlock", at = @At("RETURN"), cancellable = true)
    private void preventGhostBlocks(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        // إذا كان التفاعل ناجحاً بالفعل، لا نفعل شيئاً
        if (cir.getReturnValue().isAccepted()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        BlockState clickedState = client.world.getBlockState(hitResult.getBlockPos());
        ItemStack stackInHand = player.getStackInHand(hand);

        // إذا نقرت على أنكر موجود
        if (clickedState.isOf(Blocks.RESPAWN_ANCHOR)) {
            // وكنت تمسك أنكر آخر أو جلوستون واللعبة اعتبرت النفرة فاشلة (بسبب اللاق)
            if (stackInHand.isOf(Items.RESPAWN_ANCHOR) || stackInHand.isOf(Items.GLOWSTONE)) {
                // نخدع الكلاينت ونخبره أن التفاعل "نجح" (Success)
                // هذا يمنع اللعبة من محاولة وضع بلوكة جديدة تماماً، فيختفي الغوست بلوك!
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }
}