package com.unbreaking.crystal.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class ItemSwapAnimationMixin {
    @Shadow private ItemStack mainHand;
    @Shadow private float equipProgressMainHand;

    @Inject(method = "updateHeldItems", at = @At("HEAD"))
    private void cancelItemSwapAnimation(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack nextHand = client.player.getMainHandStack();

        if (isFastSwapItem(this.mainHand) && isFastSwapItem(nextHand)) {
            this.mainHand = nextHand;
            this.equipProgressMainHand = 1.0f;
        }
    }

    private boolean isFastSwapItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        return stack.isOf(Items.RESPAWN_ANCHOR) || 
               stack.isOf(Items.GLOWSTONE) || 
               stack.isOf(Items.END_CRYSTAL) || 
               stack.isOf(Items.OBSIDIAN);
    }
}