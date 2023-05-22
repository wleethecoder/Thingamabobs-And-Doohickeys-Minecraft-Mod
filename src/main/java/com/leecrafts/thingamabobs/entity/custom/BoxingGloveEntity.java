package com.leecrafts.thingamabobs.entity.custom;

import com.google.common.collect.Lists;
import com.leecrafts.thingamabobs.entity.ModEntityTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class BoxingGloveEntity extends Projectile implements GeoAnimatable {

    private static final EntityDataAccessor<ItemStack> DATA_WEAPON = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_INITIAL_SPEED = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MAXIMUM_DISTANCE = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ACCELERATION = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_REBOUNDING = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int TIME_BEFORE_REBOUND = (int) (0.75 * TICKS_PER_SECOND);
    private static final double LIFE_SPAN_IN_SECONDS = 3;
    protected boolean inGround;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BoxingGloveEntity(EntityType<? extends BoxingGloveEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.inGround = false;
    }

    public BoxingGloveEntity(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntityTypes.BOXING_GLOVE.get(), level);
        this.setOwner(shooter);
        Vec3 vec3 = shooter.getViewVector(1);
        this.setPos( shooter.getX() + vec3.x * 0.3, shooter.getEyeY() - 0.1 + vec3.y * 0.3, shooter.getZ() + vec3.z * 0.3);
        this.setWeapon(weapon);
        this.setDamage(damage);
    }

    public void shoot(LivingEntity shooter, Vec3 shooterVelocity, float baseMaxDistance) {
        Vec3 vec31 = shooter.getUpVector(1.0F);
        Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0, vec31.x, vec31.y, vec31.z);
        Vec3 vec3 = shooter.getViewVector(1.0F);
        Vector3f vector3f = vec3.toVector3f().rotate(quaternionf);
        double shooterSpeed = shooterVelocity.dot(vec3) / vec3.length();
        float maxDistance = (float) (TIME_BEFORE_REBOUND * shooterSpeed + baseMaxDistance);
        float speed = 2 * maxDistance / TIME_BEFORE_REBOUND;
        this.shoot(vector3f.x(), vector3f.y(), vector3f.z(), speed, 0.0f);
        this.setInitialSpeed(speed);
        this.setMaxDistance(maxDistance);
        this.setAcceleration(-speed / TIME_BEFORE_REBOUND);
        System.out.println("shooterSpeed: " + shooterSpeed + "; speed: " + speed + "; maxDistance: " + maxDistance + "; acceleration: " + this.getAcceleration());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount >= LIFE_SPAN_IN_SECONDS * TICKS_PER_SECOND) {
            this.discard();
        }
        BlockState blockState = this.level.getBlockState(this.blockPosition());
        if (this.isInWaterOrRain() || blockState.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
            this.clearFire();
        }
//        HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec31 = this.position();
        Vec3 vec32 = vec31.add(vec3);

        // block hit result
        BlockHitResult blockHitResult = this.level.clip(new ClipContext(vec31, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHitResult.getType() != HitResult.Type.MISS) {
            vec32 = blockHitResult.getLocation();
            if (!ForgeEventFactory.onProjectileImpact(this, blockHitResult)) {
                System.out.println("freaking blocks man!");
                this.onHit(blockHitResult);
                this.hasImpulse = true;
            }
        }

        // entity hit result(s)
        // Unlike ProjectileUtil::getEntityHitResult, BoxingGloveEntity::getEntityHitResults enables hitting multiple
        // mobs crammed together in a single block
        for (EntityHitResult entityHitResult : this.getEntityHitResults(vec31, vec32)) {
            this.onHitEntity(entityHitResult);
        }

        Entity shooter = this.getOwner();
        if (!this.isRebounding()) {
            if (!this.inGround) {
                vec3 = vec3.normalize().scale(Math.max(0, vec3.length() + this.getAcceleration())); // v = v0 + at (t = 1 tick)
                if (vec3.length() == 0 || this.tickCount >= TIME_BEFORE_REBOUND || (shooter != null && this.isVeryFar(shooter))) {
                    System.out.println("rebounding (speed was " + vec3.length() + ")");
                    this.setAcceleration(Math.abs(this.getAcceleration())); // Using Math.abs instead of *(-1) just in case
                    this.setRebounding(true);
                }
            }
            else {
                System.out.println("should be stuck");
                if (this.tickCount >= TIME_BEFORE_REBOUND) {
                    System.out.println("rebounding after being stuck on a block (speed was " + vec3.length() + ")");
                    this.setAcceleration(Math.abs(this.getAcceleration())); // Using Math.abs instead of *(-1) just in case
                    this.setRebounding(true);
                    this.inGround = false;
                }
            }
        }
        else {
            if (shooter != null) {
                Vec3 vec33 = shooter.getEyePosition().subtract(0, 0.1, 0).subtract(this.position());

                // https://bugs.mojang.com/browse/MC-122335?focusedId=779506&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-779506
                // Due to a bug, the shooter will occasionally not be hit by the rebounding projectile.
                // Therefore, the lines below will check if the rebounding projectile is close enough to the shooter.
                // TODO do you need this?
                if (vec33.length() < shooter.getBbWidth()) {
                    System.out.println("manually calling onHitEntity");
                    this.onHitEntity(new EntityHitResult(shooter));
                }

                if (this.isVeryFar(shooter)) {
                    System.out.println("speeding up");
                    this.setAcceleration(this.getAcceleration() + 0.01f);
                }
                vec3 = vec33.normalize().scale(Math.min(2.5, vec3.length() + this.getAcceleration()));
                Vec3 vec34 = vec3.scale(-1);
                double d0 = vec34.horizontalDistance();
                this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec34.y, d0) * (double)(180F / (float)Math.PI))));
                this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec34.x, vec34.z) * (double)(180F / (float)Math.PI))));
            }
        }
        this.setDeltaMovement(vec3);
        this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
        this.checkInsideBlocks();
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity pTarget) {
        Entity shooter = this.getOwner();
        return (pTarget.canBeHitByProjectile() && !this.isRebounding() && shooter != null && !pTarget.is(shooter)) || (this.isRebounding() && shooter != null && pTarget.is(shooter));
    }

    // TODO for some reason, the projectile sometimes does not hit the shooter when it is rebounding
    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
//        super.onHitEntity(pResult);
        Entity shooter = this.getOwner();
        Entity target = pResult.getEntity();
        DamageSource damageSource;
        if (shooter instanceof Player) {
            damageSource = this.damageSources().playerAttack((Player) shooter);
        }
        else {
            damageSource = this.damageSources().mobProjectile(this, (LivingEntity) shooter);
        }
        if (shooter != null) {
            ((LivingEntity) shooter).setLastHurtMob(target);

            // When projectile returns to shooter, it is discarded
            if (target.is(shooter) && this.isRebounding()) {
                this.discard();
                System.out.println("projectile returned");
                return;
            }
        }
        boolean isEnderMan = target.getType() == EntityType.ENDERMAN;
        if (this.isOnFire() && !isEnderMan) {
            target.setSecondsOnFire(5);
        }
        if (target.hurt(damageSource, this.getDamage())) {
            if (isEnderMan) {
                return;
            }
            if (target instanceof LivingEntity livingEntity) {
                double knockbackFactor = Math.max(0.3, 1 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                int knockbackLevel = 5;
                Vec3 vec3 = this.getDeltaMovement().multiply(1, 0, 1).normalize().scale(knockbackLevel * 0.6 * knockbackFactor);
                livingEntity.push(vec3.x, 0.4, vec3.z);
                if (!this.level.isClientSide && shooter != null) {
                    this.doEnchantDamageEffects((LivingEntity) shooter, livingEntity);
                }
            }
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        // TODO make glove damage block, "stick" to it for the rest of the bounce duration, and then rebound back to user
        Vec3 vec3 = pResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
//        this.setDeltaMovement(vec3);
        this.setDeltaMovement(Vec3.ZERO);
        System.out.println("clientside: " + this.level.isClientSide + "; speed after hitting block: " + vec3.length());
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 2.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        this.inGround = true;
//        this.discard();
    }

    private List<EntityHitResult> getEntityHitResults(Vec3 pStartVec, Vec3 pEndVec) {
        List<EntityHitResult> list = Lists.newArrayList();
        AABB aabb = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D);

        for(Entity entity1 : this.level.getEntities(this, aabb, this::canHitEntity)) {
            AABB aabb1 = entity1.getBoundingBox().inflate(0.3f);
            Optional<Vec3> optional = aabb1.clip(pStartVec, pEndVec);
            if (optional.isPresent()) {
                list.add(new EntityHitResult(entity1));
            }
        }

        return list;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_WEAPON, ItemStack.EMPTY);
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_INITIAL_SPEED, 0.0f);
        this.entityData.define(DATA_MAXIMUM_DISTANCE, 0.0f);
        this.entityData.define(DATA_ACCELERATION, 0.0f);
        this.entityData.define(DATA_IS_REBOUNDING, false);
    }

    public ItemStack getWeapon() {
        return this.entityData.get(DATA_WEAPON);
    }

    public void setWeapon(ItemStack weapon) {
        this.entityData.set(DATA_WEAPON, weapon);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }

    public float getInitialSpeed() {
        return this.entityData.get(DATA_INITIAL_SPEED);
    }

    public void setInitialSpeed(float initialSpeed) {
        this.entityData.set(DATA_INITIAL_SPEED, initialSpeed);
    }

    public float getMaxDistance() {
        return this.entityData.get(DATA_MAXIMUM_DISTANCE);
    }

    public void setMaxDistance(float maxDistance) {
        this.entityData.set(DATA_MAXIMUM_DISTANCE, maxDistance);
    }

    public float getAcceleration() {
        return this.entityData.get(DATA_ACCELERATION);
    }

    public void setAcceleration(float acceleration) {
        this.entityData.set(DATA_ACCELERATION, acceleration);
    }

    public boolean isRebounding() {
        return this.entityData.get(DATA_IS_REBOUNDING);
    }

    public void setRebounding(boolean isRebounding) {
        this.entityData.set(DATA_IS_REBOUNDING, isRebounding);
    }

    private boolean isVeryFar(Entity shooter) {
        return this.distanceTo(shooter) > 1.5 * this.getMaxDistance();
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
