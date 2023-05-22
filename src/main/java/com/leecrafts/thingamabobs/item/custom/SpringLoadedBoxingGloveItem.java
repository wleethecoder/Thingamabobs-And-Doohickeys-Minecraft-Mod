package com.leecrafts.thingamabobs.item.custom;

import com.leecrafts.thingamabobs.item.client.SpringLoadedBoxingGloveRenderer;
import com.leecrafts.thingamabobs.packet.PacketHandler;
import com.leecrafts.thingamabobs.packet.ServerboundSpringLoadedBoxingGloveAttackPacket;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class SpringLoadedBoxingGloveItem extends CrossbowItem implements Vanishable, IForgeItem, GeoItem {

    private static final int MAX_CHARGE_DURATION = 25;
    private static final float PROJECTILE_DAMAGE = 10.0f;
    public static final float PROJECTILE_DISTANCE = 10.0f;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, true);

    public SpringLoadedBoxingGloveItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @NotNull Predicate<ItemStack> getSupportedHeldProjectiles() {
        return (itemStack) -> false;
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return (itemStack) -> false;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, @NotNull Player pPlayer, @NotNull InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (isCharged(itemStack)) {
            System.out.println("get walloped!");
            shootProjectile(pLevel, pPlayer, pUsedHand);
            setCharged(itemStack, false);
        }
        else {
            System.out.println("start using item");
            pPlayer.startUsingItem(pUsedHand);
        }
        return InteractionResultHolder.consume(itemStack);
    }

    // TODO pTimeLeft is not always the same for client and server side
    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pLivingEntity, int pTimeLeft) {
        System.out.println("released. time charged (in ticks): " + (MAX_CHARGE_DURATION - pTimeLeft));
        if (pTimeLeft <= 0 && !isCharged(pStack)) {
            System.out.println("\tpunchy glove is now charged");
            setCharged(pStack, true);
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

    private static void shootProjectile(Level level, LivingEntity shooter, InteractionHand interactionHand) {
        if (level.isClientSide) {
            Vec3 vec3 = shooter.getDeltaMovement();
            // The shooter's speed adds to the projectile's initial speed and maximum distance
            // The server does not handle player movement logic, so a packet must be sent from the client to the server
            PacketHandler.INSTANCE.sendToServer(new ServerboundSpringLoadedBoxingGloveAttackPacket(
                    shooter.getId(), interactionHand, vec3.x, vec3.y, vec3.z, PROJECTILE_DAMAGE, PROJECTILE_DISTANCE));
        }
    }

    // TODO to make it have crossbow sounds?
    // TODO to make it have quick charge enchantment?
    @Override
    public void onUseTick(@NotNull Level pLevel, @NotNull LivingEntity pLivingEntity, @NotNull ItemStack pStack, int pCount) {
//        super.onUseTick(pLevel, pLivingEntity, pStack, pCount);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack) {
        return MAX_CHARGE_DURATION;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, @Nullable Level pLevel, @NotNull List<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        if (isCharged(pStack)) {
            pTooltip.add(Component.translatable("item.thingamabobs.punchy_glove.charged"));
        }
    }

    // TODO charging and attack animation
    private PlayState predicate(AnimationState<GeoItem> animationState) {
//        animationState.getController().setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
//        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
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
}
