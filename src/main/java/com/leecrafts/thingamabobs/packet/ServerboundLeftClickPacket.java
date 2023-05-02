package com.leecrafts.thingamabobs.packet;

import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.player.PlayerCap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundLeftClickPacket {

    public final boolean leftClick;

    public ServerboundLeftClickPacket(boolean leftClick) {
        this.leftClick = leftClick;
    }

    public ServerboundLeftClickPacket(FriendlyByteBuf buffer) {
        this(buffer.readBoolean());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.leftClick);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                sender.getCapability(ModCapabilities.PLAYER_CAPABILITY).ifPresent(iPlayerCap -> {
                    System.out.println("packet sent, left click is " + this.leftClick);
                    PlayerCap playerCap = (PlayerCap) iPlayerCap;
                    playerCap.leftClick = this.leftClick;
                    // TODO reset charge if left click is false
                });
            }
        });
    }

}
