package com.leecrafts.thingamabobs.packet;

import com.leecrafts.thingamabobs.sound.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.leecrafts.thingamabobs.item.custom.ComicallyLargeMalletItem.CHARGE_TIME;

public class ServerboundComicallyLargeMalletAttackPacket {

    public final int strength;
    public final String targetsString;

    public ServerboundComicallyLargeMalletAttackPacket(int strength, String targetsString) {
        this.strength = strength;
        this.targetsString = targetsString;
    }

    public ServerboundComicallyLargeMalletAttackPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readUtf());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.strength);
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
                        sender.attackStrengthTicker = this.strength;
                        sender.attack(entity);
                    }
                }
                // TODO what about non-attackable entities?
                if (list.length > 0) {
                    sender.level.playSound(null, sender, ModSounds.COMICALLY_LARGE_MALLET_WHAM.get(), SoundSource.PLAYERS, 5.0f, (sender.getRandom().nextFloat() - sender.getRandom().nextFloat()) * 0.2F + 1.0F);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
