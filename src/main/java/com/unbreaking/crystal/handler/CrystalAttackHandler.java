package com.unbreaking.crystal.handler;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;

import java.util.List;

public class CrystalAttackHandler implements PlayerInteractEntityC2SPacket.Handler {
    private final MinecraftClient client;

    public CrystalAttackHandler(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void interact(Hand hand) {}

    @Override
    public void interactAt(Hand hand, net.minecraft.util.math.Vec3d pos) {}

    @Override
    public void attack() {
        HitResult hitResult = this.client.crosshairTarget;
        if (hitResult instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof EndCrystalEntity crystal) {
                ClientPlayerEntity player = this.client.player;
                if (player != null && canDestroyCrystal(player)) {
                    // 1. حذف الكريستال فورياً محلياً مع إلغاء الـ Hitbox الخاص به
                    crystal.setBoundingBox(new Box(0, 0, 0, 0, 0, 0));
                    crystal.remove(Entity.RemovalReason.KILLED);

                    // 2. إعادة توجيه المؤشر فوراً لمنع التزامن الوهمي
                    retargetCrosshair();
                }
            }
        }
    }

    private void retargetCrosshair() {
        ClientPlayerEntity player = this.client.player;
        if (player != null && this.client.world != null) {
            // إعادة توجيه المؤشر مباشرة للبلوكة التي خلف الكريستالة
            HitResult retraced = player.raycast(player.getBlockInteractionRange(), 1.0F, false);

            // تنظيف الكائن المحدد سابوقاً لكي لا يحاول السيرفر تفنيد الضربة
            this.client.targetedEntity = null;
            this.client.crosshairTarget = retraced;
        }
    }

    private boolean canDestroyCrystal(ClientPlayerEntity player) {
        double damage = player.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        return damage > 0.0;
    }
}