package com.leecrafts.thingamabobs.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem.CHARGE_TIME;

public class ServerboundComicallyLargeMalletAttackPacket {

    public final String targetsString;

    public ServerboundComicallyLargeMalletAttackPacket(String targetsString) {
        this.targetsString = targetsString;
    }

    public ServerboundComicallyLargeMalletAttackPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.targetsString);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                String[] list = this.targetsString.split(",");
                for (String str : list) {
                    Entity entity = sender.level.getEntity(Integer.parseInt(str));
                    if (entity != null) {
                        sender.attackStrengthTicker = CHARGE_TIME;
                        sender.attack(entity);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
