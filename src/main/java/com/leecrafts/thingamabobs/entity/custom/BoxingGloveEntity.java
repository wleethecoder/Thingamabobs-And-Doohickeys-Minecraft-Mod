package com.leecrafts.thingamabobs.entity.custom;

import com.google.common.collect.Lists;
import com.leecrafts.thingamabobs.capability.ModCapabilities;
import com.leecrafts.thingamabobs.capability.entity.EntityStickyBoxingGloveCap;
import com.leecrafts.thingamabobs.enchantment.ModEnchantments;
import com.leecrafts.thingamabobs.entity.ModEntityTypes;
import com.leecrafts.thingamabobs.item.custom.SpringLoadedBoxingGloveItem;
import com.leecrafts.thingamabobs.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class BoxingGloveEntity extends Projectile implements GeoAnimatable {

    private static final EntityDataAccessor<ItemStack> DATA_WEAPON = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_INITIAL_SPEED = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_BASE_MAXIMUM_DISTANCE = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ACCELERATION = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_IS_DEFLECTED = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_QUICK_CHARGE_LEVEL = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_STICKY = SynchedEntityData.defineId(BoxingGloveEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int TIME_BEFORE_REBOUND = (int) (0.75 * TICKS_PER_SECOND);
    private static final double LIFE_SPAN_IN_SECONDS = 3;
    protected boolean inGround;
    protected boolean isRebounding;
    protected boolean initialized;
    protected int damageToItem;
    private int knockback;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BoxingGloveEntity(EntityType<? extends BoxingGloveEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.inGround = false;
        this.isRebounding = false;
        this.initialized = false;
        this.damageToItem = 0;
        this.noCulling = true;
    }

    public BoxingGloveEntity(Level level, LivingEntity shooter, ItemStack weapon, float damage) {
        this(ModEntityTypes.BOXING_GLOVE.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
        this.setWeapon(weapon);
        this.setDamage(damage);
        this.knockback = weapon.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
        this.setQuickChargeLevel(weapon.getEnchantmentLevel(Enchantments.QUICK_CHARGE));
        this.setSticky(weapon.getEnchantmentLevel(ModEnchantments.STICKY.get()) > 0);
    }

    public void shoot(LivingEntity shooter, Vec3 shooterVelocity, float baseMaxDistance) {
        this.setBaseMaxDistance(baseMaxDistance);
        Vec3 vec31 = shooter.getUpVector(1.0F);
        Quaternionf quaternionf = (new Quaternionf()).setAngleAxis(0, vec31.x, vec31.y, vec31.z);
        Vec3 vec3 = shooter.getViewVector(1.0F);
        Vector3f vector3f = vec3.toVector3f().rotate(quaternionf);
        double shooterSpeed = shooterVelocity.dot(vec3) / vec3.length();
        float maxDistance = (float) (TIME_BEFORE_REBOUND * shooterSpeed + baseMaxDistance);
        float speed = 2 * maxDistance / TIME_BEFORE_REBOUND;
        this.shoot(vector3f.x(), vector3f.y(), vector3f.z(), speed, 0.0f);
        this.setInitialSpeed(speed);
        this.setAcceleration(-speed / TIME_BEFORE_REBOUND);
        System.out.println("shooterSpeed: " + shooterSpeed + "; speed: " + speed + "; maxDistance: " + maxDistance + "; acceleration: " + this.getAcceleration());
    }

    @Override
    public void tick() {
        super.tick();

        Entity shooter = this.getOwner();
        if (this.tickCount >= LIFE_SPAN_IN_SECONDS * TICKS_PER_SECOND) {
            this.returnToShooter();
        }
        BlockState blockState = this.level.getBlockState(this.blockPosition());
        if (this.isInWaterOrRain() || blockState.is(Blocks.POWDER_SNOW) || this.isInFluidType((fluidType, height) -> this.canFluidExtinguish(fluidType))) {
            this.clearFire();
        }

        if (!this.inGround) {
            Vec3 vec3 = this.getDeltaMovement();
            if (!this.isRebounding) {
//                HitResult hitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);

                // block hit result
                Vec3 vec31 = this.position();
                Vec3 vec32 = vec31.add(vec3);
                BlockHitResult blockHitResult = this.level.clip(new ClipContext(vec31, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    if (!ForgeEventFactory.onProjectileImpact(this, blockHitResult)) {
                        Level level = this.level;
                        BlockPos blockPos = blockHitResult.getBlockPos();
                        BlockState blockState1 = level.getBlockState(blockPos);
                        Block block = blockState1.getBlock();
                        float hardness = block.defaultDestroyTime();
                        if (block instanceof SlimeBlock || block instanceof BedBlock) {
                            this.setDeflected(true);
                        }
                        else if (blockState1.getMaterial() != Material.LEAVES && hardness >= 0.0f && hardness <= 0.3f && this.mayInteract(level, blockPos)) {
                            if (!level.isClientSide) {
                                level.destroyBlock(blockPos, false, this);
                                ItemStack weapon = this.getWeapon();
                                if (weapon != null && weapon.getItem() instanceof SpringLoadedBoxingGloveItem) {
                                    Block.dropResources(blockState1, level, blockPos, blockState1.hasBlockEntity() ? level.getBlockEntity(blockPos) : null, shooter, weapon);
                                    if (shooter instanceof LivingEntity) {
                                        if (shooter instanceof Player player && !player.isCreative()) {
                                            player.awardStat(Stats.BLOCK_MINED.get(block));
                                        }
                                        weapon.hurtAndBreak(hardness > 0.0f ? 1 : 0, (LivingEntity) shooter, (livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND)));
                                    }
                                }
                            }
                        }
                        else {
                            vec32 = blockHitResult.getLocation();
                            this.onHit(blockHitResult);
                            this.hasImpulse = true;
                            vec3 = this.getDeltaMovement(); // this.getDeltamovment() has been changed from onHitBlock()
                        }
                    }
                }

                // entity hit result(s)
                // Unlike ProjectileUtil::getEntityHitResult, BoxingGloveEntity::getEntityHitResults enables hitting multiple
                // mobs crammed together in a single block
                for (EntityHitResult entityHitResult : this.getEntityHitResults(vec31, vec32)) {
                    this.onHitEntity(entityHitResult);
                }
                if (!this.level.isClientSide && this.damageToItem > 0) {
                    ItemStack weapon = this.getWeapon();
                    if (weapon != null && weapon.getItem() instanceof SpringLoadedBoxingGloveItem && shooter instanceof LivingEntity) {
                        weapon.hurtAndBreak(this.damageToItem, (LivingEntity) shooter, (livingEntity -> livingEntity.broadcastBreakEvent(EquipmentSlot.MAINHAND)));
                    }
                    this.damageToItem = 0;
                }

                vec3 = vec3.normalize().scale(Math.max(0, vec3.length() + this.getAcceleration())); // v = v0 + at (t = 1 tick)
                if (vec3.length() == 0 ||
                        this.tickCount >= TIME_BEFORE_REBOUND ||
                        this.isDeflected()) {
                    this.setAcceleration(this.getReboundAcceleration());
                    this.isRebounding = true;
                }
            }
            else {
                if (shooter != null) {
                    double yOffset = shooter.getBbHeight() * 2.0/3;
                    double minSpeed = 0;
                    int sign = -1;
                    if (this.isDeflected()) {
                        yOffset = shooter.getEyeHeight() - 0.1;
                        minSpeed = this.getInitialSpeed();
                        sign = 1;
                    }
                    Vec3 vec33 = shooter.position().add(0, yOffset, 0).subtract(this.position());

                    // https://bugs.mojang.com/browse/MC-122335?focusedId=779506&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-779506
                    // Due to a bug, the shooter will occasionally not be hit by the rebounding projectile.
                    // Therefore, the lines below will check if the rebounding projectile is close enough to the shooter.
                    if (vec33.length() < vec3.length()) {
                        System.out.println("manually calling onHitEntity");
                        this.onHitEntity(new EntityHitResult(shooter));
                    }

                    if (this.tickCount >= TIME_BEFORE_REBOUND * 2) {
                        System.out.println("speeding up");
                        this.setAcceleration(this.getAcceleration() + 0.05f);
                    }
                    vec3 = vec33.normalize().scale(
                            Mth.clamp(vec3.length() + this.getAcceleration(), minSpeed, 2.5));
                    Vec3 vec34 = vec3.scale(sign);
                    double d0 = vec34.horizontalDistance();
                    this.setXRot(lerpRotation(this.xRotO, (float)(Mth.atan2(vec34.y, d0) * (double)(180F / (float)Math.PI))));
                    this.setYRot(lerpRotation(this.yRotO, (float)(Mth.atan2(vec34.x, vec34.z) * (double)(180F / (float)Math.PI))));
                }
            }
            this.setDeltaMovement(vec3);
            this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
            this.checkInsideBlocks();
        }
        else {
            if (this.tickCount >= TIME_BEFORE_REBOUND || this.isDeflected()) {
                System.out.println("rebounding after being stuck on a block (speed was " + this.getDeltaMovement().length() + ")");
                this.setAcceleration(this.getReboundAcceleration());
                this.inGround = false;
                this.isRebounding = true;
            }
        }
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity pTarget) {
        Entity shooter = this.getOwner();
        boolean targetIsShooter = shooter != null && pTarget.is(shooter);
        return (pTarget.canBeHitByProjectile() && !this.isRebounding && !targetIsShooter) || (this.isRebounding && targetIsShooter);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        super.onHitEntity(pResult);
        Entity shooter = this.getOwner();
        Entity target = pResult.getEntity();
        DamageSource damageSource = this.damageSources().mobProjectile(this, shooter instanceof LivingEntity ? (LivingEntity) shooter : null);
//        if (shooter instanceof Player) {
//            damageSource = this.damageSources().playerAttack((Player) shooter);
//        }
//        else {
//            damageSource = this.damageSources().mobProjectile(this, shooter instanceof LivingEntity ? (LivingEntity) shooter : null);
//        }
        boolean isEnderMan = target.getType() == EntityType.ENDERMAN;
        if (this.isOnFire() && !isEnderMan) {
            target.setSecondsOnFire(5);
        }
        if (shooter instanceof LivingEntity) {
            ((LivingEntity) shooter).setLastHurtMob(target);
        }
        boolean targetIsShooter = shooter != null && target.is(shooter);

        // When projectile returns to shooter, it is discarded
        if (targetIsShooter) {
            if (this.isDeflected()) {
                // Shooters hit by deflected projectiles have their invincibility frames reset to receive full damage and knockback
                shooter.invulnerableTime = 0;

                this.punch(shooter, target, damageSource, isEnderMan, true, false);
            }
            this.returnToShooter();
        }
        else {
            this.punch(shooter, target, damageSource, isEnderMan, false, this.isSticky());
        }
    }

    private void punch(Entity shooter, Entity target, DamageSource damageSource, boolean isEnderMan, boolean isDeflected, boolean isSticky) {
        if ((target.hurt(damageSource, this.getDamage()) || isSticky) && !isEnderMan) {
            target.stopRiding();
            if (isSticky) {
                // TODO riding or capabilities?
                // TODO if riding, then change pose and enable non-living entities to ride
//                target.getCapability(ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY).ifPresent(iEntityStickyBoxingGloveCap -> {
//                    EntityStickyBoxingGloveCap entityStickyBoxingGloveCap = (EntityStickyBoxingGloveCap) iEntityStickyBoxingGloveCap;
//                    entityStickyBoxingGloveCap.boxingGloveId = this.getId();
//                });
                target.setPose(Pose.STANDING);
                target.startRiding(this, true);
            }
            if (target instanceof LivingEntity livingEntity) {
                if (!isSticky) {
                    double knockbackFactor = Math.max(0.5, 1 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    Vec3 vec3 = this.getDeltaMovement().multiply(1, 0, 1).normalize().scale((5 + this.knockback * 2) * 0.3 * knockbackFactor);
                    livingEntity.push(vec3.x, 0.4 + this.knockback * 0.1, vec3.z);
                }
                if (shooter instanceof LivingEntity livingEntity1) {
                    if (!this.level.isClientSide) {
                        this.damageToItem++;
                        this.doEnchantDamageEffects(livingEntity1, livingEntity);
                    }
                    if (isDeflected) {
                        ItemStack weapon = this.getWeapon();
                        if (weapon != null && weapon.getItem() instanceof SpringLoadedBoxingGloveItem) {
                            weapon.hurtAndBreak(1, livingEntity1, (livingEntity2 -> livingEntity2.broadcastBreakEvent(EquipmentSlot.MAINHAND)));
                        }
                        this.playSound(ModSounds.SPRING_LOADED_BOXING_GLOVE_BACKFIRE.get(), 5.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                    }
                }
            }
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        super.onHitBlock(pResult);
        Vec3 vec3 = pResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
        this.setDeltaMovement(vec3);
        Vec3 vec31 = vec3.normalize().scale(0.05F);
        this.setPosRaw(this.getX() - vec31.x, this.getY() - vec31.y, this.getZ() - vec31.z);
        this.playSound(SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, 2.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        this.inGround = true;
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

    public void returnToShooter() {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        this.discard();
    }

    @Override
    public double getPassengersRidingOffset() {
        return this.getBbHeight() * 0.5;
    }

    @Override
    public void positionRider(@NotNull Entity pPassenger) {
        if (this.hasPassenger(pPassenger)) {
            double d0 = this.getY() + this.getPassengersRidingOffset() - pPassenger.getBbHeight() / 2.0f;
            Entity.MoveFunction callback = Entity::setPos;
            callback.accept(pPassenger, this.getX(), d0, this.getZ());
        }
    }

    @Override
    public void remove(@NotNull RemovalReason pReason) {
        SpringLoadedBoxingGloveItem.resetState(this.level, this.getOwner(), this.getWeapon());
        super.remove(pReason);
    }

    // This will make explosion-caused deflections more consistent
    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_WEAPON, ItemStack.EMPTY);
        this.entityData.define(DATA_DAMAGE, 0.0f);
        this.entityData.define(DATA_INITIAL_SPEED, 0.0f);
        this.entityData.define(DATA_BASE_MAXIMUM_DISTANCE, 0.0f);
        this.entityData.define(DATA_ACCELERATION, 0.0f);
        this.entityData.define(DATA_IS_DEFLECTED, false);
        this.entityData.define(DATA_QUICK_CHARGE_LEVEL, 0);
        this.entityData.define(DATA_IS_STICKY, false);
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

    public float getBaseMaxDistance() {
        return this.entityData.get(DATA_BASE_MAXIMUM_DISTANCE);
    }

    public void setBaseMaxDistance(float baseMaxDistance) {
        this.entityData.set(DATA_BASE_MAXIMUM_DISTANCE, baseMaxDistance);
    }

    public float getAcceleration() {
        return this.entityData.get(DATA_ACCELERATION);
    }

    public void setAcceleration(float acceleration) {
        this.entityData.set(DATA_ACCELERATION, acceleration);
    }

    public boolean isDeflected() {
        return this.entityData.get(DATA_IS_DEFLECTED);
    }

    public void setDeflected(boolean isDeflected) {
        this.entityData.set(DATA_IS_DEFLECTED, isDeflected);
    }

    public int getQuickChargeLevel() {
        return this.entityData.get(DATA_QUICK_CHARGE_LEVEL);
    }

    public void setQuickChargeLevel(int quickChargeLevel) {
        this.entityData.set(DATA_QUICK_CHARGE_LEVEL, quickChargeLevel);
    }

    public boolean isSticky() {
        return this.entityData.get(DATA_IS_STICKY);
    }

    public void setSticky(boolean isSticky) {
        this.entityData.set(DATA_IS_STICKY, isSticky);
    }

    // TODO tweak; maybe consider using this.tickCount?
    private boolean isFar(Entity shooter) {
        return this.distanceTo(shooter) > 1.1 * this.getBaseMaxDistance();
    }

    private float getReboundAcceleration() {
        int reboundSpeedFactor = this.getQuickChargeLevel() + 1;
        return Math.abs(reboundSpeedFactor * reboundSpeedFactor * this.getAcceleration()); // Using Math.abs instead of *(-1) just in case
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
