package com.unbreaking.crystal.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Shadow private float equipProgressMainHand, prevEquipProgressMainHand;
    @Shadow private float equipProgressOffHand, prevEquipProgressOffHand;
    @Shadow private ItemStack mainHand, offHand;

    @Inject(method = "updateHeldItems", at = @At("TAIL"))
    private void onUpdateHeldItems(CallbackInfo ci) {
        if (mainHand.isOf(Items.END_CRYSTAL) || mainHand.isOf(Items.OBSIDIAN)) {
            equipProgressMainHand = prevEquipProgressMainHand = 1.0f;
        }
        if (offHand.isOf(Items.END_CRYSTAL) || offHand.isOf(Items.OBSIDIAN)) {
            equipProgressOffHand = prevEquipProgressOffHand = 1.0f;
        }
    }
}