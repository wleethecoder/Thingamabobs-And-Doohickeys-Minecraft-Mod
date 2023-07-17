package com.leecrafts.thingamabobs.entity.custom;

import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.entity.EntityExplosivePastryCap;
import com.leecrafts.thingamabobs.entity.ModEntityTypes;
import com.leecrafts.thingamabobs.item.ModItems;
import com.leecrafts.thingamabobs.misc.FixedDamageExplosion;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class AbstractExplosivePastryEntity extends ThrowableItemProjectile implements GeoAnimatable {

    private static final float EXPLODE_TIMER = 30.0f * TICKS_PER_SECOND;
    private static final float BLAST_RADIUS = 3.0f;
    private static final float DAMAGE = 7.0f;
    private int stickToEntityTimestamp;
    public double angle;
    public double yOffset;
    private boolean stuckToGround;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbstractExplosivePastryEntity(EntityType<? extends AbstractExplosivePastryEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.stickToEntityTimestamp = 0;
        this.angle = 0;
        this.yOffset = 0;
        this.stuckToGround = false;
    }

    public AbstractExplosivePastryEntity(EntityType<? extends AbstractExplosivePastryEntity> pEntityType, LivingEntity shooter, Level level) {
        super(pEntityType, shooter, level);
    }

    @Override
    public void tick() {
        if (this.stuckToGround) {
            if (this.level instanceof ServerLevel) {
                List<Entity> list = this.level.getEntities(this, this.getBoundingBox());
                if (list.size() > 0) {
                    this.explode();
                }
            }
        }
        else {
            super.tick();
            Entity vehicle = this.getVehicle();
            if (vehicle != null && !vehicle.isRemoved()) {
                if (this.level instanceof ServerLevel && this.tickCount - this.stickToEntityTimestamp >= EXPLODE_TIMER) {
                    vehicle.getCapability(ModCapabilities.ENTITY_EXPLOSIVE_PASTRY_CAPABILITY).ifPresent(iEntityExplosivePastryCap -> {
                        EntityExplosivePastryCap entityExplosivePastryCap = (EntityExplosivePastryCap) iEntityExplosivePastryCap;
                        if (vehicle.tickCount - entityExplosivePastryCap.explosionTimestamp > 10) {
                            entityExplosivePastryCap.explosionTimestamp = vehicle.tickCount;
                            this.explode();
                        }
                    });
                }
            }
        }
    }

    public void explode() {
        ServerLevel serverLevel = (ServerLevel) this.level;
//        serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_RADIUS, Level.ExplosionInteraction.NONE);
        FixedDamageExplosion fixedDamageExplosion = new FixedDamageExplosion(serverLevel, this, this.getX(), this.getY(), this.getZ(), BLAST_RADIUS, DAMAGE, Explosion.BlockInteraction.KEEP);
        if (!ForgeEventFactory.onExplosionStart(serverLevel, fixedDamageExplosion)) {
            fixedDamageExplosion.explode();
            fixedDamageExplosion.finalizeExplosion(true);
        }
        if (!fixedDamageExplosion.interactsWithBlocks()) {
            fixedDamageExplosion.clearToBlow();
        }
        for (ServerPlayer serverPlayer : serverLevel.players()) {
            if (serverPlayer.distanceToSqr(this.getX(), this.getY(), this.getZ()) < 4096.0D) {
                serverPlayer.connection.send(new ClientboundExplodePacket(this.getX(), this.getY(), this.getZ(), BLAST_RADIUS, fixedDamageExplosion.getToBlow(), fixedDamageExplosion.getHitPlayers().get(serverPlayer)));
            }
        }
        this.discard();
    }

    @Override
    public void setPos(double p_20210_, double p_20211_, double p_20212_) {
        Entity vehicle = this.getVehicle();
        if (vehicle != null && !vehicle.isRemoved()) {
            Vec3 vec3 = calculateHorizontalViewVector(vehicle.getYRot());
            Vec3 vec31 = new Vec3(vec3.x * Math.cos(this.angle) + vec3.z * Math.sin(this.angle), 0, -vec3.x * Math.sin(this.angle) + vec3.z * Math.cos(this.angle));
            vec31 = vec31.normalize().scale(vehicle.getBbWidth() / 2);
            super.setPos(vehicle.getX() + vec31.x, vehicle.getY() + this.yOffset, vehicle.getZ() + vec31.z);
        }
        else {
            super.setPos(p_20210_, p_20211_, p_20212_);
        }
    }

    @Override
    protected void updateRotation() {
        Entity vehicle = this.getVehicle();
        // TODO it rotates the wrong way when the entity rotates
        Vec3 vec3;
        if (vehicle != null) {
            double yDiff = vehicle.getY() + vehicle.getBbHeight() / 2 - this.getY();
            double y;
            if (yDiff >= vehicle.getBbHeight() / 4) {
                y = vehicle.getY() + 3 * vehicle.getBbHeight() / 4 - this.getY();
            }
            else if (yDiff <= -vehicle.getBbHeight() / 4) {
                y = vehicle.getY() + vehicle.getBbHeight() / 4 - this.getY();
            }
            else {
                y = 0;
            }
            vec3 = new Vec3(vehicle.getX() - this.getX(),
                    y,
                    vehicle.getZ() - this.getZ());
        }
        else {
            vec3 = this.getDeltaMovement();
        }
//        this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI))));
//        this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI))));
        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI)));
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        // TODO conditional
        if (this.getType() == ModEntityTypes.EXPLOSIVE_PUMPKIN_PIE.get()) {
            return ModItems.EXPLOSIVE_PUMPKIN_PIE_ITEM.get();
        }
        else {
            return Items.SNOWBALL;
        }
    }

    private ParticleOptions getParticle() {
        ItemStack itemStack = this.getItemRaw();
        // TODO change
        return itemStack.isEmpty() ? ParticleTypes.LANDING_HONEY : new ItemParticleOption(ParticleTypes.ITEM, itemStack);
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            ParticleOptions particleoptions = this.getParticle();

            for(int i = 0; i < 8; ++i) {
                this.level.addParticle(particleoptions, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity entity = pResult.getEntity();
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), 0);
        this.stickToEntityTimestamp = this.tickCount;
        // TODO which entities should it not ride?
        this.startRiding(entity, true);
        Vec3 vec3 = this.position().subtract(entity.position()).multiply(1, 0, 1);
        float yRot = entity instanceof Player ? entity.getYHeadRot() : entity.getYRot();
        Vec3 vec31 = calculateHorizontalViewVector(yRot);
        this.angle = Math.atan2(vec3.x * vec31.z - vec31.x * vec3.z, vec3.dot(vec31));
        this.yOffset = Mth.clamp(this.getY() - entity.getY(), 0, entity.getBbHeight());
    }

    private static Vec3 calculateHorizontalViewVector(float yRot) {
        float f1 = -yRot * ((float)Math.PI / 180F);
        return new Vec3(Mth.sin(f1), 0, Mth.cos(f1));
    }

    @Override
    protected void onHit(@NotNull HitResult pResult) {
        super.onHit(pResult);
        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte) 3);
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        // TODO sticks to block and acts like a landmine
        if (this.getVehicle() == null) {
//            this.discard();
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
            this.stuckToGround = true;
//            Direction direction = pResult.getDirection();
//            switch (direction) {
//                case UP ->
//            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object o) {
        return ((Entity) o).tickCount;
    }
}
