package com.unbreaking.crystal.mixin;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(ClientCommonNetworkHandler.class)
public class PacketBarrierMixin {

    @Unique private static int unbreaking$interactPacketsThisTick = 0;
    @Unique private static long unbreaking$lastTickTime = 0L;

    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        long currentMillis = System.currentTimeMillis();

        // إعادة ضبط العداد كل 50 ملي ثانية (كل Tick)
        if (currentMillis - unbreaking$lastTickTime > 50) {
            unbreaking$interactPacketsThisTick = 0;
            unbreaking$lastTickTime = currentMillis;
        }

        // تنظيم حزم التفاعل مع البلوكات للحد من الحزم الزائدة ومنع الـ Kick
        if (packet instanceof PlayerInteractBlockC2SPacket) {
            if (unbreaking$interactPacketsThisTick >= 2) {
                ci.cancel();
                return;
            }
            unbreaking$interactPacketsThisTick++;
        }
    }
}