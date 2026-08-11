package com.unbreaking.crystal.mixin;

import com.unbreaking.crystal.InstantPacketUtil;
import com.unbreaking.crystal.config.CrystalConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class FastInputMixin {

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMousePress(long window, int button, int action, int mods, CallbackInfo ci) {
        // فحص تفعيل الخيار الجديد أولاً
        if (!CrystalConfig.enableZeroDelayClicks) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null || client.currentScreen != null || client.interactionManager == null) return;

        // التقاط الضغطة الفورية من نظام التشغيل (GLFW)
        if (action == GLFW.GLFW_PRESS) {

            // 1. كليك يسار: تكسير الكريستال الفوري (Instant Attack)
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (client.crosshairTarget instanceof EntityHitResult entityHit) {
                    if (entityHit.getEntity() instanceof EndCrystalEntity crystal) {
                        PlayerInteractEntityC2SPacket attackPacket = PlayerInteractEntityC2SPacket.attack(crystal, client.player.isSneaking());
                        InstantPacketUtil.sendInstant(attackPacket);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }

            // 2. كليك يمين: وضع الكريستال والتفاعل مع الأنكر الفوري (Instant Place & Anchor)
            else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (client.crosshairTarget instanceof BlockHitResult blockHit) {
                    BlockPos pos = blockHit.getBlockPos();
                    BlockState state = client.world.getBlockState(pos);

                    Hand crystalHand = getCrystalHand(client);

                    // أ) إذا كنت تحمل كريستالة: ضعها فوراً
                    if (crystalHand != null) {
                        client.interactionManager.interactBlock(client.player, crystalHand, blockHit);
                        client.player.swingHand(crystalHand);
                    }
                    // ب) إذا كانت البلوكة الأنكر ومفعل خيار Anchor Optimizer
                    else if (CrystalConfig.enableAnchorOptimizer && state.isOf(Blocks.RESPAWN_ANCHOR)) {
                        Hand anchorHand = getAnchorInteractHand(client);
                        client.interactionManager.interactBlock(client.player, anchorHand, blockHit);
                        client.player.swingHand(anchorHand);
                    }
                }
            }
        }
    }

    private Hand getCrystalHand(MinecraftClient client) {
        if (client.player == null) return null;
        if (client.player.getMainHandStack().isOf(Items.END_CRYSTAL)) return Hand.MAIN_HAND;
        if (client.player.getOffHandStack().isOf(Items.END_CRYSTAL)) return Hand.OFF_HAND;
        return null;
    }

    private Hand getAnchorInteractHand(MinecraftClient client) {
        if (client.player == null) return Hand.MAIN_HAND;
        if (client.player.getOffHandStack().isOf(Items.GLOWSTONE)) return Hand.OFF_HAND;
        return Hand.MAIN_HAND;
    }
}