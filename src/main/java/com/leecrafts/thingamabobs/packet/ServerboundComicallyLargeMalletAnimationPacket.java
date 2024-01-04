package com.leecrafts.thingamabobs.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerboundComicallyLargeMalletAnimationPacket {

    public final float animSpeed;
    public final boolean mirrored;
    public final boolean fade;
    public final ByteBuf animBytes;

    public ServerboundComicallyLargeMalletAnimationPacket(float animSpeed, boolean mirrored, boolean fade, ByteBuf animBytes) {
        this.animSpeed = animSpeed;
        this.mirrored = mirrored;
        this.fade = fade;
        this.animBytes = animBytes;
    }

    public ServerboundComicallyLargeMalletAnimationPacket(FriendlyByteBuf buffer) {
        this(buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBytes(buffer.readableBytes()));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.animSpeed);
        buffer.writeBoolean(this.mirrored);
        buffer.writeBoolean(this.fade);
        buffer.writeBytes(this.animBytes);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
//                System.out.println("packet is sent to the server. now another one will be sent to the client. (player " + sender.getId() + ")");
                PacketHandler.INSTANCE.send(
                        new ClientboundComicallyLargeMalletAnimationPacket(sender.getId(), this.animSpeed, this.mirrored, this.fade, this.animBytes),
                        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(sender));
            }
        });
        ctx.setPacketHandled(true);
    }

}
