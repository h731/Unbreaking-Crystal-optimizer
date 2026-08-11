package com.unbreaking.crystal.mixin;

import com.unbreaking.crystal.config.CrystalConfig;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow private int itemUseCooldown;
    @Shadow public HitResult crosshairTarget;
    @Shadow public ClientPlayerEntity player;
    @Shadow public ClientWorld world;

    @Unique private long unbreaking$lastGlowstoneTime = 0L;
    @Unique private long unbreaking$lastAnchorTime = 0L;
    @Unique private BlockPos unbreaking$lastExplodedPos = null;

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void onDoItemUse(CallbackInfo ci) {
        if (!CrystalConfig.enableZeroDelayClicks) {
            return;
        }

        if (player == null || world == null) return;

        long currentTime = System.currentTimeMillis();

        boolean isHoldingGlowstone = player.getMainHandStack().isOf(Items.GLOWSTONE) ||
                player.getOffHandStack().isOf(Items.GLOWSTONE);

        // تم تصحيح الخطأ الإملائي هنا (RESPAWN_ANCHOR)
        boolean isHoldingAnchor = player.getMainHandStack().isOf(Blocks.RESPAWN_ANCHOR.asItem()) ||
                player.getOffHandStack().isOf(Blocks.RESPAWN_ANCHOR.asItem());

        if (crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) crosshairTarget;
            BlockPos targetPos = blockHit.getBlockPos();
            BlockState state = world.getBlockState(targetPos);

            // 1. تفاعل الغلوستون مع الأنكر (الشحن والتفجير)
            if (state.isOf(Blocks.RESPAWN_ANCHOR) && isHoldingGlowstone) {
                int charges = state.get(RespawnAnchorBlock.CHARGES);

                if (charges >= 4) {
                    unbreaking$lastExplodedPos = targetPos;
                    this.itemUseCooldown = 5;
                } else {
                    if (currentTime - unbreaking$lastGlowstoneTime < 200L) {
                        ci.cancel();

                        int nextCharges = Math.min(charges + 1, 4);
                        world.setBlockState(targetPos, state.with(RespawnAnchorBlock.CHARGES, nextCharges), 11);

                        return;
                    }
                    unbreaking$lastGlowstoneTime = currentTime;
                    this.itemUseCooldown = 4;
                }
                return;
            }

            // 2. وضع الأنكر (بناء طبيعي أو Air Anchor)
            if (isHoldingAnchor) {
                if (unbreaking$lastExplodedPos != null &&
                        (unbreaking$lastExplodedPos.equals(targetPos) || unbreaking$lastExplodedPos.equals(targetPos.offset(blockHit.getSide())))) {

                    this.itemUseCooldown = 0;
                    unbreaking$lastExplodedPos = null;
                    return;
                }

                if (currentTime - unbreaking$lastAnchorTime < 200L) {
                    ci.cancel();
                    return;
                }
                unbreaking$lastAnchorTime = currentTime;
                this.itemUseCooldown = 4;
            }
        }
    }
}