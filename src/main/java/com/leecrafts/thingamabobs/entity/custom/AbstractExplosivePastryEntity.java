package com.leecrafts.thingamabobs.entity.custom;

import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.entity.EntityExplosivePastryCap;
import com.leecrafts.thingamabobs.config.ThingamabobsAndDoohickeysServerConfigs;
import com.leecrafts.thingamabobs.entity.ModEntityTypes;
import com.leecrafts.thingamabobs.item.ModItems;
import com.leecrafts.thingamabobs.misc.FixedDamageExplosion;
import com.leecrafts.thingamabobs.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class AbstractExplosivePastryEntity extends ThrowableItemProjectile implements GeoAnimatable {

    private static final EntityDataAccessor<Integer> DATA_STICK_TO_ENTITY_TIMESTAMP = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_STICK_TO_ENTITY_ANGLE = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_STICK_TO_ENTITY_Y_OFFSET = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_LANDMINE = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_LANDMINE_X_ROT = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_LANDMINE_Y_ROT = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_WILL_EXPLODE = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> DATA_LANDMINE_POS = SynchedEntityData.defineId(AbstractExplosivePastryEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final float EXPLODE_TIMER = 3.0f * TICKS_PER_SECOND;
    private static final float BLAST_RADIUS = 3.0f;
    private static final float DAMAGE = 7.0f;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AbstractExplosivePastryEntity(EntityType<? extends AbstractExplosivePastryEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public AbstractExplosivePastryEntity(EntityType<? extends AbstractExplosivePastryEntity> pEntityType, LivingEntity shooter, Level level) {
        super(pEntityType, shooter, level);
    }

    public AbstractExplosivePastryEntity(EntityType<? extends AbstractExplosivePastryEntity> pEntityType, Level level, double x, double y, double z) {
        super(pEntityType, x, y, z, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isLandmine()) {
            BlockState blockState = this.level.getBlockState(this.getLandminePos());
            if (this.getVehicle() == null && !blockState.isAir() && !blockState.getMaterial().isLiquid()) {
                this.updateLandmineRot();
                if (this.level instanceof ServerLevel) {
                    List<Entity> list = this.level.getEntities(this, this.getBoundingBox());
                    if (!list.isEmpty()) {
                        this.explode();
                    }
                }
            }
            else {
                this.setNoGravity(false);
                this.setLandmine(false);
            }
            this.blocksBuilding = true;
        }
        else {
            Entity vehicle = this.getVehicle();
            if (vehicle != null && !vehicle.isRemoved()) {
                if (this.tickCount - this.getStickToEntityTimestamp() >= EXPLODE_TIMER) {
                    vehicle.getCapability(ModCapabilities.ENTITY_EXPLOSIVE_PASTRY_CAPABILITY).ifPresent(iEntityExplosivePastryCap -> {
                        EntityExplosivePastryCap entityExplosivePastryCap = (EntityExplosivePastryCap) iEntityExplosivePastryCap;
                        if (vehicle.tickCount - entityExplosivePastryCap.explosionTimestamp > 10) {
                            entityExplosivePastryCap.explosionTimestamp = vehicle.tickCount;
                            this.explode();
                        }
                    });
                }
            }
            this.blocksBuilding = false;
        }

        if (!this.isRemoved()) {
            BlockState blockState = this.level.getBlockState(this.blockPosition());
            if (blockState.getMaterial().isLiquid()) {
                Item item = this instanceof ExplosivePumpkinPieEntity ? Items.PUMPKIN_PIE : Items.CAKE;
                ItemEntity itemEntity = new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), new ItemStack(item, 1));
                this.level.addFreshEntity(itemEntity);
                this.discard();
            }
        }
    }

    public void explode() {
        if (this.level instanceof ServerLevel serverLevel) {
//        serverLevel.explode(this, this.getX(), this.getY(), this.getZ(), BLAST_RADIUS, Level.ExplosionInteraction.NONE);
            boolean griefing = ThingamabobsAndDoohickeysServerConfigs.EXPLOSIVE_PASTRY_GRIEFING.get();
            FixedDamageExplosion fixedDamageExplosion = new FixedDamageExplosion(
                    serverLevel,
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    BLAST_RADIUS,
                    DAMAGE,
                    griefing ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP);
            if (!ForgeEventFactory.onExplosionStart(serverLevel, fixedDamageExplosion)) {
                this.setWillExplode(true);
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
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        boolean hurt = super.hurt(pSource, pAmount);
        if (!this.willExplode() && this.getVehicle() == null && this.isLandmine()) {
            this.explode();
        }
        return hurt;
    }

    @Override
    public void setPos(double p_20210_, double p_20211_, double p_20212_) {
        Entity vehicle = this.getVehicle();
        if (vehicle != null && !vehicle.isRemoved()) {
            Vec3 vec3 = calculateHorizontalViewVector(vehicle.getYHeadRot());
            Vec3 vec31 = new Vec3(vec3.x * Math.cos(this.getStickToEntityAngle()) + vec3.z * Math.sin(this.getStickToEntityAngle()), 0, -vec3.x * Math.sin(this.getStickToEntityAngle()) + vec3.z * Math.cos(this.getStickToEntityAngle()));
            vec31 = vec31.normalize().scale(vehicle.getBbWidth() / 2);
            super.setPos(vehicle.getX() + vec31.x, vehicle.getY() + this.getStickToEntityYOffset(), vehicle.getZ() + vec31.z);
        }
        else {
            super.setPos(p_20210_, p_20211_, p_20212_);
        }
    }

    @Override
    protected void updateRotation() {
        // I'd say it doesn't really rotate the right way for some reason, but it only latches onto its victim for 3 seconds,
        // so I consider this issue minor.
        Entity vehicle = this.getVehicle();
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
        this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI))));
        this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI))));
//        this.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * (double)(180F / (float)Math.PI)));
//        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
//        this.xRotO = this.getXRot();
//        this.yRotO = this.getYRot();
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return this.getType() == ModEntityTypes.EXPLOSIVE_PUMPKIN_PIE.get() ?
                ModItems.EXPLOSIVE_PUMPKIN_PIE_ITEM.get() : ModItems.EXPLOSIVE_CAKE_ITEM.get();
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            for(int i = 0; i < 8; ++i) {
                double d0 = 0.16D;
                this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5D) * d0, ((double)this.random.nextFloat() - 0.5D) * d0, ((double)this.random.nextFloat() - 0.5D) * d0);
            }
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity entity = pResult.getEntity();
        if (entity instanceof AbstractExplosivePastryEntity && entity.getVehicle() != null) {
            entity = entity.getVehicle();
        }
        if (!(entity instanceof AbstractExplosivePastryEntity) &&
                (this.getOwner() == null || !entity.is(this.getOwner()) || this.tickCount > TICKS_PER_SECOND / 2)) {
            if (entity instanceof LivingEntity && !(entity instanceof EnderDragon)) {
                this.stickToEntity(entity);
            }
            else {
                this.explode();
            }
        }
    }

    public void stickToEntity(Entity entity) {
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), 0);
        this.setStickToEntityTimestamp(this.tickCount);
        this.startRiding(entity, true);
        Vec3 vec3 = this.position().subtract(entity.position()).multiply(1, 0, 1);
        float yRot = entity instanceof Player ? entity.getYHeadRot() : entity.getYRot();
        Vec3 vec31 = calculateHorizontalViewVector(yRot);
        this.setStickToEntityAngle((float) Math.atan2(vec3.x * vec31.z - vec31.x * vec3.z, vec3.dot(vec31)));
        this.setStickToEntityYOffset((float) Mth.clamp(this.getY() - entity.getY(), 0, entity.getBbHeight()));
        this.setNoGravity(false);
        this.setLandmine(false);
        this.playSplatSound();
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
        if (this.getVehicle() == null) {
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO);
            this.setLandmine(true);
            Direction direction = pResult.getDirection();
            this.setPosFromDirection(pResult, direction);
            this.setLandminePos(pResult.getBlockPos());
            boolean freeSpaceFound = true;
            if (this.level.getBlockState(BlockPos.containing(this.position())).getMaterial().isSolid()) {
                freeSpaceFound = false;
                for (Direction direction1 : Direction.values()) {
                    if (!this.level.getBlockState(this.getLandminePos().relative(direction1)).getMaterial().isSolid()) {
                        this.setPosFromDirection(pResult, direction1);
                        freeSpaceFound = true;
                    }
                }
            }
            if (freeSpaceFound) {
                if (this.level.getBlockState(BlockPos.containing(this.position())).getMaterial().isSolid()) {
                    throw new IllegalStateException("Explosive pastry is in a solid block when it should not be (entity id " + this.getId() + ")");
                }
            }

            VoxelShape voxelShape = this.level.getBlockState(this.getLandminePos()).getCollisionShape(this.level, this.getLandminePos());
            this.setPos(this.position().relative(direction, voxelShape.max(direction.getAxis()) - 1));

            this.playSplatSound();
        }
    }

    private void setPosFromDirection(BlockHitResult pResult, Direction direction) {
        this.setPos(Vec3.atCenterOf(pResult.getBlockPos()).relative(direction, 0.125 + 0.5));
        switch (direction) {
            case DOWN -> this.setLandmineRot(-90f, 0f);
            case UP -> this.setLandmineRot(90f, 0f);
            case NORTH -> this.setLandmineRot(0f, 180f);
            case SOUTH -> this.setLandmineRot(0f, 0f);
            case WEST -> this.setLandmineRot(0f, -90f);
            case EAST -> this.setLandmineRot(0f, 90f);
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STICK_TO_ENTITY_TIMESTAMP, 0);
        this.entityData.define(DATA_STICK_TO_ENTITY_ANGLE, 0f);
        this.entityData.define(DATA_STICK_TO_ENTITY_Y_OFFSET, 0f);
        this.entityData.define(DATA_IS_LANDMINE, false);
        this.entityData.define(DATA_LANDMINE_X_ROT, 0f);
        this.entityData.define(DATA_LANDMINE_Y_ROT, 0f);
        this.entityData.define(DATA_WILL_EXPLODE, false);
        this.entityData.define(DATA_LANDMINE_POS, BlockPos.ZERO);
    }

    public int getStickToEntityTimestamp() {
        return this.entityData.get(DATA_STICK_TO_ENTITY_TIMESTAMP);
    }

    public void setStickToEntityTimestamp(int stickToEntityTimestamp) {
        this.entityData.set(DATA_STICK_TO_ENTITY_TIMESTAMP, stickToEntityTimestamp);
    }

    public float getStickToEntityAngle() {
        return this.entityData.get(DATA_STICK_TO_ENTITY_ANGLE);
    }

    public void setStickToEntityAngle(float stickToEntityAngle) {
        this.entityData.set(DATA_STICK_TO_ENTITY_ANGLE, stickToEntityAngle);
    }

    public float getStickToEntityYOffset() {
        return this.entityData.get(DATA_STICK_TO_ENTITY_Y_OFFSET);
    }

    public void setStickToEntityYOffset(float stickToEntityYOffset) {
        this.entityData.set(DATA_STICK_TO_ENTITY_Y_OFFSET, stickToEntityYOffset);
    }

    public boolean isLandmine() {
        return this.entityData.get(DATA_IS_LANDMINE);
    }

    public void setLandmine(boolean isLandmine) {
        this.entityData.set(DATA_IS_LANDMINE, isLandmine);
    }

    public float getLandmineXRot() {
        return this.entityData.get(DATA_LANDMINE_X_ROT);
    }

    public void setLandmineXRot(float landmineXRot) {
        this.entityData.set(DATA_LANDMINE_X_ROT, landmineXRot);
    }

    public float getLandmineYRot() {
        return this.entityData.get(DATA_LANDMINE_Y_ROT);
    }

    public void setLandmineYRot(float landmineYRot) {
        this.entityData.set(DATA_LANDMINE_Y_ROT, landmineYRot);
    }

    public void updateLandmineRot() {
        this.setXRot(this.getLandmineXRot());
        this.setYRot(this.getLandmineYRot());
    }

    public void setLandmineRot(float landmineXRot, float landmineYRot) {
        this.setLandmineXRot(landmineXRot);
        this.setLandmineYRot(landmineYRot);
    }

    public boolean willExplode() {
        return this.entityData.get(DATA_WILL_EXPLODE);
    }

    public void setWillExplode(boolean willExplode) {
        this.entityData.set(DATA_WILL_EXPLODE, willExplode);
    }

    public BlockPos getLandminePos() {
        return this.entityData.get(DATA_LANDMINE_POS);
    }

    public void setLandminePos(BlockPos blockPos) {
        this.entityData.set(DATA_LANDMINE_POS, blockPos);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("StickToEntityTimestamp", this.getStickToEntityTimestamp());
        pCompound.putFloat("StickToEntityAngle", this.getStickToEntityAngle());
        pCompound.putFloat("StickToEntityYOffset", this.getStickToEntityYOffset());
        pCompound.putBoolean("IsLandmine", this.isLandmine());
        pCompound.putFloat("LandmineXRot", this.getLandmineXRot());
        pCompound.putFloat("LandmineYRot", this.getLandmineYRot());
        pCompound.putBoolean("WillExplode", this.willExplode());
        BlockPos blockPos = this.getLandminePos();
        pCompound.putIntArray("LandminePos", new int[] {blockPos.getX(), blockPos.getY(), blockPos.getZ()});
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setStickToEntityTimestamp(pCompound.getInt("StickToEntityTimestamp"));
        this.setStickToEntityAngle(pCompound.getFloat("StickToEntityAngle"));
        this.setStickToEntityYOffset(pCompound.getFloat("StickToEntityYOffset"));
        this.setLandmine(pCompound.getBoolean("IsLandmine"));
        this.setLandmineXRot(pCompound.getFloat("LandmineXRot"));
        this.setLandmineYRot(pCompound.getFloat("LandmineYRot"));
        this.setWillExplode(pCompound.getBoolean("WillExplode"));
        int[] landminePos = pCompound.getIntArray("LandminePos");
        if (landminePos.length == 3) {
            this.setLandminePos(new BlockPos(landminePos[0], landminePos[1], landminePos[2]));
        }
    }

    private void playSplatSound() {
        this.playSound(ModSounds.EXPLOSIVE_PASTRY_SPLAT.get(), 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
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
