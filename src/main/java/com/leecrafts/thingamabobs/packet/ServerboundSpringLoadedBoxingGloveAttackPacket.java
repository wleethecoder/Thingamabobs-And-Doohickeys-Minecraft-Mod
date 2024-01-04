package com.leecrafts.thingamabobs.packet;

import com.leecrafts.thingamabobs.entity.custom.BoxingGloveEntity;
import com.leecrafts.thingamabobs.sound.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ServerboundSpringLoadedBoxingGloveAttackPacket {

    public final int shooterId;
    public final InteractionHand interactionHand;
    public final double shooterVelocityX;
    public final double shooterVelocityY;
    public final double shooterVelocityZ;
    public final float projectileDamage;
    public final float projectileBaseMaxDistance;

    public ServerboundSpringLoadedBoxingGloveAttackPacket(int shooterId, InteractionHand interactionHand, double shooterVelocityX, double shooterVelocityY, double shooterVelocityZ, float projectileDamage, float projectileBaseMaxDistance) {
        this.shooterId = shooterId;
        this.interactionHand = interactionHand;
        this.shooterVelocityX = shooterVelocityX;
        this.shooterVelocityY = shooterVelocityY;
        this.shooterVelocityZ = shooterVelocityZ;
        this.projectileDamage = projectileDamage;
        this.projectileBaseMaxDistance = projectileBaseMaxDistance;
    }

    public ServerboundSpringLoadedBoxingGloveAttackPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readEnum(InteractionHand.class), buffer.readDouble(), buffer.readDouble(), buffer.readDouble(), buffer.readFloat(), buffer.readFloat());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.shooterId);
        buffer.writeEnum(this.interactionHand);
        buffer.writeDouble(this.shooterVelocityX);
        buffer.writeDouble(this.shooterVelocityY);
        buffer.writeDouble(this.shooterVelocityZ);
        buffer.writeFloat(this.projectileDamage);
        buffer.writeFloat(this.projectileBaseMaxDistance);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer sender = ctx.getSender();
            if (sender != null) {
                Level level = sender.level;
                if (level.getEntity(this.shooterId) instanceof LivingEntity shooter) {
                    ItemStack weapon = shooter.getItemInHand(this.interactionHand);
                    BoxingGloveEntity boxingGloveEntity = new BoxingGloveEntity(level, shooter, weapon, this.projectileDamage);
                    boxingGloveEntity.shoot(shooter, new Vec3(this.shooterVelocityX, this.shooterVelocityY, this.shooterVelocityZ), this.projectileBaseMaxDistance);
                    level.addFreshEntity(boxingGloveEntity);
                    level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), ModSounds.SPRING_LOADED_BOXING_GLOVE_BOING.get(), SoundSource.PLAYERS, 1.0f, (sender.getRandom().nextFloat() - sender.getRandom().nextFloat()) * 0.2f + 1.0f);
                    if (shooter instanceof ServerPlayer serverPlayer) {
                        serverPlayer.awardStat(Stats.ITEM_USED.get(weapon.getItem()));
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }

}
