package com.unbreaking.crystal.mixin;

import com.unbreaking.crystal.util.ICrystalAgeState;
import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndCrystalEntity.class)
public class SmartRaycastMixin {

    // نتدخل في دالة canHit التي تحدد ما إذا كان الماوس يستطيع تحديد هذا الكيان
    @Inject(method = "canHit", at = @At("HEAD"), cancellable = true)
    private void onCanHit(CallbackInfoReturnable<Boolean> cir) {
        EndCrystalEntity crystal = (EndCrystalEntity) (Object) this;

        // 1. إذا كانت الكريستالة محذوفة رسمياً من عالم اللعبة
        if (crystal.isRemoved()) {
            cir.setReturnValue(false);
            return;
        }

        // 2. إذا تم تعليم الكريستالة كـ "مفجرة/مخفية" عبر Fast Derender في كلاس ICrystalAgeState
        if (crystal instanceof ICrystalAgeState ageState && ageState.unbreaking$isDerendered()) {
            cir.setReturnValue(false);
        }
    }
}