package com.unbreaking.crystal.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class ExplosionRenderFixMixin {

    @Inject(method = "setBlockState", at = @At("HEAD"))
    private void onExplosionBlockRemove(BlockPos pos, BlockState state, int flags, int maxDepth, CallbackInfoReturnable<Boolean> cir) {
        ClientWorld world = (ClientWorld) (Object) this;

        // عند اختفاء البلوك المدمّر فوراً محلياً
        if (state.isOf(Blocks.AIR) && !world.isAir(pos)) {
            // تحديث الأوجه الـ 6 المباشرة فقط للبلوكات الملتصقة بالانفجار
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.offset(direction);
                BlockState neighborState = world.getBlockState(neighborPos);

                if (!neighborState.isAir()) {
                    world.scheduleBlockRerenderIfNeeded(neighborPos, neighborState, neighborState);
                }
            }
        }
    }
}