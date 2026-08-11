package com.unbreaking.crystal.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class PreventDoubleChargeMixin {
    
    private int lastInteractTick = -1;
    private int interactionsThisTick = 0;

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        
        // التحقق من أن البلوك هو ريسباون أنكر
        if (player.clientWorld.getBlockState(hitResult.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR)) {
            
            int currentTick = player.age; 
            
            // تصفير العداد إذا دخلنا تيك جديد (مرت 50 ميلي ثانية)
            if (currentTick != lastInteractTick) {
                lastInteractTick = currentTick;
                interactionsThisTick = 0;
            }
            
            interactionsThisTick++;
            
            // السماح بحركتين فقط (شحن + تفجير) في نفس التيك
            if (interactionsThisTick > 2) {
                // CONSUME يمنع الاستمرار ووضع أنكر جديد أو استخدام اليد الثانية
                cir.setReturnValue(ActionResult.CONSUME);
            }
        }
    }
}