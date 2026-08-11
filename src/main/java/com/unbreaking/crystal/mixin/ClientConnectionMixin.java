package com.unbreaking.crystal.mixin;

import com.unbreaking.crystal.handler.CrystalAttackHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused") // يمنع IntelliJ من إظهار تحذيرات "Never used"
@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Unique
    private CrystalAttackHandler cachedHandler;

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void onPacketSend(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof PlayerInteractEntityC2SPacket interactionPacket) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null && client.world != null) {
                if (this.cachedHandler == null) {
                    this.cachedHandler = new CrystalAttackHandler(client);
                }
                interactionPacket.handle(this.cachedHandler);
            }
        }
    }
}