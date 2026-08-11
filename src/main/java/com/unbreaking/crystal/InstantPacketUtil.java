package com.unbreaking.crystal;

import com.unbreaking.crystal.mixin.ClientConnectionAccessor;
import io.netty.channel.Channel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;

public class InstantPacketUtil {

    // إجبار الباكت على الخروج فوراً لتجنب التأخير المحلي
    public static void sendInstant(Packet<?> packet) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) {
            ClientConnection connection = client.getNetworkHandler().getConnection();
            if (connection != null) {
                // استخدام الـ Accessor للوصول الآمن للقناة
                Channel channel = ((ClientConnectionAccessor) connection).getChannel();
                if (channel != null && channel.isOpen()) {
                    channel.writeAndFlush(packet);
                }
            }
        }
    }
}