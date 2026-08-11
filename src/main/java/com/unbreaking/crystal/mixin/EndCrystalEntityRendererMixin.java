package com.unbreaking.crystal.mixin;

import com.unbreaking.crystal.config.CrystalConfig;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntityRenderer.class)
public class EndCrystalEntityRendererMixin {

    /**
     * إيقاف حركة الكريستال العمودية (الاهتزاز) عند تفعيل الخيار من الإعدادات
     */
    @Inject(method = "getYOffset", at = @At("HEAD"), cancellable = true)
    private static void onGetYOffset(float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (CrystalConfig.disableCrystalMovement) {
            cir.setReturnValue(0.0F);
        }
    }
}