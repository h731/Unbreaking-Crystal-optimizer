package com.unbreaking.crystal.mixin;

import net.minecraft.entity.decoration.EndCrystalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCrystalEntity.class)
public class EndCrystalEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        EndCrystalEntity crystal = (EndCrystalEntity) (Object) this;
        // منطق المود الخاص بك لتتبع عمر الكريستالة أو حمايتها يكتب هنا
    }
}