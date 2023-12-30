package com.leecrafts.thingamabobs.item.custom;

import com.leecrafts.thingamabobs.config.ThingamabobsAndDoohickeysServerConfigs;
import com.leecrafts.thingamabobs.enchantment.ModEnchantments;
import com.leecrafts.thingamabobs.entity.custom.BoxingGloveEntity;
import com.leecrafts.thingamabobs.item.client.SpringLoadedBoxingGloveRenderer;
import com.leecrafts.thingamabobs.packet.PacketHandler;
import com.leecrafts.thingamabobs.packet.ServerboundSpringLoadedBoxingGloveAnimationPacket;
import com.leecrafts.thingamabobs.packet.ServerboundSpringLoadedBoxingGloveAttackPacket;
import com.leecrafts.thingamabobs.sound.ModSounds;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class SpringLoadedBoxingGloveItem extends CrossbowItem implements Vanishable, IForgeItem, GeoItem {

    private static final float PROJECTILE_DAMAGE = 10.0f;
    private static final RawAnimation IDLE = RawAnimation.begin().thenPlayAndHold("idle");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlayAndHold("charge");
    private static final RawAnimation CHARGE_IDLE = RawAnimation.begin().thenPlayAndHold("charge_idle");
    private static final RawAnimation BOING = RawAnimation.begin().thenPlayAndHold("boing");
    private static final String CONTROLLER_NAME = "controller";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, true);

    public SpringLoadedBoxingGloveItem(Properties pProperties) {
        super(pProperties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public @NotNull Predicate<ItemStack> getSupportedHeldProjectiles() {
        return itemStack -> false;
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return itemStack -> false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
//        System.out.println("clientSide: " + pLevel.isClientSide + "; boing: " + isBoing(itemStack) + "; charged: " + isCharged(itemStack));
        if (isBoing(itemStack)) {
            return InteractionResultHolder.fail(itemStack);
        }
        if (isCharged(itemStack)) {
//            System.out.println("get walloped!");
            setCharged(itemStack, false);
            setBoing(itemStack, true);
            shootProjectile(pLevel, pPlayer, pUsedHand, itemStack);
            this.playAnimation(pLevel, pPlayer, itemStack, "boing");
        }
        else {
//            System.out.println("start using item");
            pPlayer.startUsingItem(pUsedHand);
            this.playAnimation(pLevel, pPlayer, itemStack, "charge");
        }
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pLivingEntity, int pTimeLeft) {
//        System.out.println("clientside: " + pLevel.isClientSide + "; released. time charged (in ticks): " + (this.getUseDuration(pStack) - pTimeLeft));
        if (!pLevel.isClientSide &&
                1.0 * (this.getUseDuration(pStack) - pTimeLeft) / CrossbowItem.getChargeDuration(pStack) >= 1 &&
                !isCharged(pStack)) {
//            System.out.println("\tpunchy glove is now charged");
            setCharged(pStack, true);

            // In case the client and server do not sync well.
            // pTimeLeft can sometimes be different between the client and server sides (i.e. smaller value in client side due to server lag)
            // Therefore, the weapon can appear not charged even when it is, as the server handles the animations.

            // By the way, I am aware that packets can read ItemStacks, but for some reason, the NBT does not save data properly when I pass an itemStack through the packet.
            InteractionHand interactionHand = pLivingEntity.getItemInHand(InteractionHand.MAIN_HAND).is(pStack.getItem()) ?
                    InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            PacketHandler.INSTANCE.sendToServer(new ServerboundSpringLoadedBoxingGloveAnimationPacket(interactionHand, pLivingEntity.getId()));
        }
        else {
            this.playAnimation(pLevel, pLivingEntity, pStack, "idle");
        }
    }

    public static boolean isCharged(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.getBoolean("Charged");
    }

    public static void setCharged(ItemStack itemStack, boolean isCharged) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putBoolean("Charged", isCharged);
    }

    public static boolean isBoing(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.getBoolean("Boing");
    }

    public static void setBoing(ItemStack itemStack, boolean isBoing) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putBoolean("Boing", isBoing);
    }

    public static boolean isBoingFromDispenser(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag != null && compoundTag.getBoolean("BoingFromDispenser");
    }

    public static void setBoingFromDispenser(ItemStack itemStack, boolean isBoingFromDispenser) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putBoolean("BoingFromDispenser", isBoingFromDispenser);
    }

    // Even though pTimeLeft is not always the same server and client side, shootProjectile() sends a packet.
    // Therefore, there are no syncing issues.
    private static void shootProjectile(Level level, LivingEntity shooter, InteractionHand interactionHand, ItemStack itemStack) {
        if (level.isClientSide) {
            Vec3 vec3 = shooter.getDeltaMovement();
            // The shooter's speed adds to the projectile's initial speed and maximum distance
            // The server does not handle player movement logic, so a packet must be sent from the client to the server
            PacketHandler.INSTANCE.sendToServer(new ServerboundSpringLoadedBoxingGloveAttackPacket(
                    shooter.getId(), interactionHand, vec3.x, vec3.y, vec3.z, PROJECTILE_DAMAGE, getBaseMaxProjectileDistance(itemStack)));
        }
    }

    // TODO to make it have crossbow sounds?
    @Override
    public void onUseTick(@NotNull Level pLevel, @NotNull LivingEntity pLivingEntity, @NotNull ItemStack pStack, int pCount) {
//        super.onUseTick(pLevel, pLivingEntity, pStack, pCount);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return CrossbowItem.getChargeDuration(pStack) + 3;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        if (isCharged(pStack)) {
            pTooltip.add(Component.translatable("item.thingamabobs.punchy_glove.charged"));
        }
    }

    // TODO inevitable animation bugs when player drops item? (minor)
    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);

        // In case the weapon gets "stuck"
        if (pEntity.tickCount % TICKS_PER_SECOND == 0 &&
                pIsSelected &&
                isBoing(pStack) &&
                pLevel.getEntitiesOfClass(BoxingGloveEntity.class, pEntity.getBoundingBox().inflate(40)).isEmpty()) {
            resetState(pLevel, pEntity, pStack);
        }

        if (isCharged(pStack)) {
            this.playAnimation(pLevel, pEntity, pStack, "charge_idle");
        }
        else if (!pIsSelected && !isBoing(pStack)) {
            this.playAnimation(pLevel, pEntity, pStack, "idle");
        }
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return (super.canApplyAtEnchantingTable(stack, enchantment) ||
                (enchantment == Enchantments.SILK_TOUCH && ThingamabobsAndDoohickeysServerConfigs.PUNCHY_GLOVE_GRIEFING.get()) ||
                enchantment == Enchantments.PUNCH_ARROWS) &&
                enchantment != Enchantments.MULTISHOT &&
                enchantment != Enchantments.PIERCING;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 14;
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack pStack, @NotNull ItemStack pRepairCandidate) {
        return pRepairCandidate.getItem() == this || super.isValidRepairItem(pStack, pRepairCandidate);
    }

    public static float getBaseMaxProjectileDistance(ItemStack itemStack) {
        return 10.0f + itemStack.getEnchantmentLevel(ModEnchantments.BOING.get()) * 3.0f;
    }

    public static void resetState(Level level, Entity shooter, ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof SpringLoadedBoxingGloveItem item) {
            setBoing(itemStack, false);
            setBoingFromDispenser(itemStack, false);
            if (shooter != null) {
                item.playAnimation(level, shooter, itemStack, "idle");
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, CONTROLLER_NAME, 0, state -> PlayState.STOP)
                .triggerableAnim("idle", IDLE)
                .triggerableAnim("charge", CHARGE)
                .triggerableAnim("charge_idle", CHARGE_IDLE)
                .triggerableAnim("boing", BOING));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public void playAnimation(Level level, Entity shooter, ItemStack itemStack, String name) {
        if (level instanceof ServerLevel serverLevel) {
            long id = GeoItem.getOrAssignId(itemStack, serverLevel);
            var animationController = this.getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get(CONTROLLER_NAME);
            if (!this.animationIsAlreadyPlaying(name, animationController)) {
                animationController.setAnimationSpeed(name.equals("charge") ? 25.0 / CrossbowItem.getChargeDuration(itemStack) : 1);
                this.triggerAnim(shooter, id, CONTROLLER_NAME, name);
            }
        }
    }

    private boolean animationIsAlreadyPlaying(String name, AnimationController<GeoAnimatable> animationController) {
        AnimationProcessor.QueuedAnimation currentAnimation = animationController.getCurrentAnimation();
        if (currentAnimation == null) {
            return false;
        }
        return currentAnimation.animation().name().equals(name);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private SpringLoadedBoxingGloveRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SpringLoadedBoxingGloveRenderer();
                }
                return this.renderer;
            }
        });
    }

    public static class SpringLoadedBoxingGloveDispenseItemBehavior extends OptionalDispenseItemBehavior {

        public SpringLoadedBoxingGloveDispenseItemBehavior() {}

        @Override
        protected @NotNull ItemStack execute(@NotNull BlockSource pSource, @NotNull ItemStack pStack) {
            ServerLevel serverLevel = pSource.getLevel();
            Direction direction = pSource.getBlockState().getValue(DispenserBlock.FACING);
            Vec3 vec3 = Vec3.atLowerCornerOf(direction.getNormal());
            if (isBoingFromDispenser(pStack) &&
                    !serverLevel.getEntitiesOfClass(BoxingGloveEntity.class, new AABB(pSource.getPos()).expandTowards(vec3.scale(30))).isEmpty()) {
                this.setSuccess(false);
            }
            else {
                BoxingGloveEntity boxingGloveEntity =
                        new BoxingGloveEntity(serverLevel, pSource.x(), pSource.y(), pSource.z(), pStack, PROJECTILE_DAMAGE);
                boxingGloveEntity.shoot(vec3, getBaseMaxProjectileDistance(pStack));
                serverLevel.addFreshEntity(boxingGloveEntity);
                serverLevel.playSound(
                        null,
                        pSource.x(),
                        pSource.y(),
                        pSource.z(),
                        ModSounds.SPRING_LOADED_BOXING_GLOVE_BOING.get(),
                        SoundSource.BLOCKS,
                        1.0f,
                        (serverLevel.getRandom().nextFloat() - serverLevel.getRandom().nextFloat()) * 0.2f + 1.0f);
                setBoingFromDispenser(pStack, true);
//            ((SpringLoadedBoxingGloveItem) pStack.getItem()).playAnimation(serverLevel, null, pStack, "boing");
                this.setSuccess(true);
            }
            return pStack;
        }
    }

}
