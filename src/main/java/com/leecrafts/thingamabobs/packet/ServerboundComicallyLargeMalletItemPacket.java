package com.leecrafts.thingamabobs.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

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

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                ItemStack offhandItem = sender.getOffhandItem();
                if (!offhandItem.isEmpty()) {
                    offhandItem.shrink(offhandItem.getCount());
                }
//                sender.addItem(this.itemStack);

            }
        });
        ctx.setPacketHandled(true);
    }

}
