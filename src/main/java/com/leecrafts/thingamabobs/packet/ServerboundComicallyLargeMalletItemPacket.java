package com.leecrafts.thingamabobs.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundComicallyLargeMalletItemPacket {

    public final ItemStack itemStack;
    public final int slot;

    public ServerboundComicallyLargeMalletItemPacket(ItemStack itemStack, int slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }

    public ServerboundComicallyLargeMalletItemPacket(FriendlyByteBuf buffer) {
        this(buffer.readItem(), buffer.readInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeItem(this.itemStack);
        buffer.writeInt(this.slot);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                ItemStack offhandItem = sender.getOffhandItem();
                if (!offhandItem.isEmpty()) {
                    offhandItem.shrink(offhandItem.getCount());
                }
//                sender.addItem(this.itemStack);

            }
        });
        ctx.get().setPacketHandled(true);
    }

}
