package com.leecrafts.thingamabobs.packet;

import com.leecrafts.thingamabobs.item.custom.SpringLoadedBoxingGloveItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ServerboundSpringLoadedBoxingGloveAnimationPacket {

    public final InteractionHand interactionHand;
    public final int shooterId;

    public ServerboundSpringLoadedBoxingGloveAnimationPacket(InteractionHand interactionHand, int shooterId) {
        this.interactionHand = interactionHand;
        this.shooterId = shooterId;
    }

    public ServerboundSpringLoadedBoxingGloveAnimationPacket(FriendlyByteBuf buffer) {
        this(buffer.readEnum(InteractionHand.class), buffer.readInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.interactionHand);
        buffer.writeInt(this.shooterId);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                Level level = sender.level;
                Entity shooter = level.getEntity(this.shooterId);
                if (shooter instanceof LivingEntity livingEntity) {
                    ItemStack weapon = livingEntity.getItemInHand(this.interactionHand);
                    if (weapon.getItem() instanceof SpringLoadedBoxingGloveItem item) {
                        SpringLoadedBoxingGloveItem.setCharged(weapon, true);
                        item.playAnimation(level, shooter, weapon, "charge_idle");
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

}
