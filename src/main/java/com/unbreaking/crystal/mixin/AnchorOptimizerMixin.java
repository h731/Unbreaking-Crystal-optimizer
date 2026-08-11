package com.unbreaking.crystal.mixin;

import com.unbreaking.crystal.config.CrystalConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class AnchorOptimizerMixin {

    @Unique
    private static long lastChargeTime = 0L;

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void onAnchorInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (!CrystalConfig.enableAnchorOptimizer) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;

        if (world != null && client.player != null) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.isOf(Blocks.RESPAWN_ANCHOR)) {
                boolean holdingGlowstone = player.getMainHandStack().isOf(Items.GLOWSTONE) || player.getOffHandStack().isOf(Items.GLOWSTONE);
                boolean isSneaking = player.isSneaking();
                int charges = state.get(RespawnAnchorBlock.CHARGES);
                double cx = pos.getX() + 0.5;
                double cy = pos.getY() + 0.5;
                double cz = pos.getZ() + 0.5;

                long now = System.currentTimeMillis();

                // 1. شحن الأنكر: فاصل زمني آمن (100ms) بين الشحنات لمنع السيرفر من رفض الشحنة أو إعطاء بان
                if (holdingGlowstone && charges < 4) {
                    if (now - lastChargeTime < 100L) return;
                    lastChargeTime = now;
                    world.playSound(cx, cy, cz, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
                    world.addParticle(ParticleTypes.REVERSE_PORTAL, cx, cy + 0.5, cz, 0.0, 0.2, 0.0);
                }
                // 2. تفجير الأنكر: تفجير وتفريغ المكان محلياً فوراً
                else if (!holdingGlowstone && !isSneaking && charges > 0) {
                    world.addParticle(ParticleTypes.EXPLOSION, cx, cy, cz, 0.0, 0.0, 0.0);
                    world.playSound(cx, cy, cz, SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.BLOCKS, 4.0F, 0.7F, false);
                    world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
    }
}