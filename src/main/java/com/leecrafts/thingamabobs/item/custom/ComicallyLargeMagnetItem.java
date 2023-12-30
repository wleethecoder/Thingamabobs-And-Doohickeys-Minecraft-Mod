package com.leecrafts.thingamabobs.item.custom;

import com.leecrafts.thingamabobs.entity.custom.AbstractExplosivePastryEntity;
import com.leecrafts.thingamabobs.item.client.ComicallyLargeMagnetRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ComicallyLargeMagnetItem extends Item implements Vanishable, IForgeItem, GeoItem {

    private static final double REACH = 8;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, true);

    public ComicallyLargeMagnetItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        boolean equipped = pIsSelected || pSlotId == 0;
        if (equipped && (!(pEntity instanceof Player) || !pEntity.isSpectator())) {

            // Destroys nearby metallic ores
            // I tried using BlockPos.withinManhattan and BlockPos.betweenClosedStream, but neither seemed efficient
            int amount = 0;
            boolean piglinsAngered = false;
            if (!pLevel.isClientSide && pEntity.tickCount % (TICKS_PER_SECOND / 2) == 0) {
                for (double x = -REACH; x <= REACH; x++) {
                    for (double y = -REACH; y <= REACH; y++) {
                        for (double z = -REACH; z <= REACH; z++) {
                            BlockPos blockPos = pEntity.blockPosition().offset((int) x, (int) y, (int) z);
                            BlockState blockState = pLevel.getBlockState(blockPos);
                            if (isMetallicOre(blockState.getBlock())) {
                                pLevel.destroyBlock(blockPos, false, pEntity);
                                Block.dropResources(
                                        blockState,
                                        pLevel,
                                        blockPos,
                                        blockState.hasBlockEntity() ? pLevel.getBlockEntity(blockPos) : null,
                                        pEntity,
                                        pStack);
                                if (pEntity instanceof Player player && !player.isCreative()) {
                                    player.awardStat(Stats.BLOCK_MINED.get(blockState.getBlock()));
                                    if (blockState.is(BlockTags.GUARDED_BY_PIGLINS)) {
                                        piglinsAngered = true;
                                    }
                                }
                                amount++;
                            }
                        }
                    }
                }

                if (amount > 0) {
                    if (pEntity instanceof LivingEntity livingEntity) {
                        pStack.hurtAndBreak(amount, livingEntity, (livingEntity1 -> livingEntity1.broadcastBreakEvent(EquipmentSlot.MAINHAND)));
                        if (piglinsAngered && pEntity instanceof Player player) {
                            PiglinAi.angerNearbyPiglins(player, false);
                        }
                    }
                    else {
                        pStack.hurt(amount, pLevel.random, null);
                    }
                }
            }

            if (pEntity instanceof LivingEntity livingEntity) {
                for (Entity entity : pLevel.getEntities(pEntity, pEntity.getBoundingBox().inflate(REACH), entity -> !entity.isRemoved())) {
                    // Steals nearby metallic items (ItemEntities)
                    boolean isCreativeOrSpectatorPlayer = entity instanceof Player player && (player.isCreative() || player.isSpectator());
                    if (entity instanceof ItemEntity itemEntity) {
                        ItemStack itemStack = itemEntity.getItem();
                        if (isMetallicItem(itemStack.getItem())) {
                            if (livingEntity instanceof Player player) {
                                itemEntity.playerTouch(player);
                            }
                            // Non-player entities that pick up items with magnet don't drop them when killed.
                            // I would have to give each non-player a custom inventory, which would be too much trouble.
//                            else {
//                                livingEntity.onItemPickup(itemEntity);
//                                livingEntity.take(itemEntity, itemStack.getCount());
//                                itemEntity.discard();
//                            }
                        }
                    }
                    // Makes nearby entities drop their metallic items (if they are equipped on main/offhand slot)
                    else if (entity instanceof LivingEntity livingEntity1 && !isCreativeOrSpectatorPlayer) {
                        forceDropItemFromSlot(livingEntity1, EquipmentSlot.MAINHAND);
                        forceDropItemFromSlot(livingEntity1, EquipmentSlot.OFFHAND);
                    }

                    if (isMetallicEntity(entity, isCreativeOrSpectatorPlayer) &&
                            !isRidingEntity(entity.getVehicle(), pEntity) &&
                            !isRidingEntity(pEntity.getVehicle(), entity)) {
                        if (!isMetallicEntity(entity.getVehicle(), false)) {
                            entity.stopRiding();
                        }
                        Vec3 vec3 = pEntity.position().subtract(entity.position());
                        boolean isExplosivePastry = entity instanceof AbstractExplosivePastryEntity;
                        if (vec3.length() > entity.getBbWidth() / 2 + pEntity.getBbWidth() / 2 + 0.25) {
                            Vec3 vec31 = entity.getDeltaMovement().add(vec3);
                            double decay = speedDecayFunction(vec31.length(), 3, REACH, 4);
                            entity.setDeltaMovement(vec31.normalize().multiply(decay, pEntity.isOnGround() ? 1 : decay, decay));
                        }
                        if (isExplosivePastry) {
                            ((AbstractExplosivePastryEntity) entity).stickToEntity(pEntity);
                        }
                        entity.resetFallDistance();
                    }
                }
            }
        }
    }

    // Equation that decreases the speed of the pulled-in entity the closer it gets to the magnet user.
    // This is the opposite behavior of how magnets behave in real life, but the pulled-in entity would keep bumping the magnet user otherwise!
    // If you want to see what the equation looks like, try it out in a graphing calculator like Desmos.
    private static double speedDecayFunction(double speed, double max, double a, double b) {
        return Math.min(Math.pow(speed/(Math.pow(b, (a-1)/a)), a), max);
    }

    private static boolean isMetallicOre(Block block) {
        return block == Blocks.IRON_ORE ||
                block == Blocks.COPPER_ORE ||
                block == Blocks.GOLD_ORE ||
                block == Blocks.NETHER_GOLD_ORE ||
                block == Blocks.DEEPSLATE_IRON_ORE ||
                block == Blocks.DEEPSLATE_COPPER_ORE ||
                block == Blocks.DEEPSLATE_GOLD_ORE;
    }

    private static void forceDropItemFromSlot(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
        if (!itemStack.isEmpty() && isMetallicItem(itemStack.getItem())) {
            if (livingEntity instanceof Player player && !player.isCreative()) {
                player.drop(itemStack.copy(), true);
                itemStack.shrink(itemStack.getCount());
            }
            else if (livingEntity instanceof Mob mob) {
                mob.spawnAtLocation(itemStack);
                mob.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        }
    }

    private static boolean isMetallicEntity(Entity entity, boolean isCreativeOrSpectatorPlayer) {
        // TODO config
        boolean allowedToPullInPlayers = true;
        if (!allowedToPullInPlayers || isCreativeOrSpectatorPlayer || entity == null) {
            return false;
        }
        for (ItemStack itemStack : entity.getArmorSlots()) {
            if (itemStack.getItem() instanceof ArmorItem armorItem && (armorItem.getMaterial() == ArmorMaterials.IRON ||
                    armorItem.getMaterial() == ArmorMaterials.CHAIN ||
                    armorItem.getMaterial() == ArmorMaterials.GOLD)) {
                return true;
            }
        }
        return entity instanceof IronGolem || entity instanceof AbstractMinecart || entity instanceof AbstractExplosivePastryEntity;
    }

    private static boolean isRidingEntity(Entity entity1, Entity entity2) {
        if (entity1 == null) {
            return false;
        }
        if (entity1.is(entity2)) {
            return true;
        }
        return isRidingEntity(entity1.getVehicle(), entity2);
    }

    private static boolean isMetallicItem(Item item) {
        String itemName = item.toString();
        return itemName.contains("iron") ||
                itemName.contains("copper") ||
                itemName.contains("gold") ||
                itemName.contains("bucket") ||
                itemName.contains("minecart") ||
                itemName.contains("steel") ||
                itemName.contains("trident") ||
                itemName.contains("compass") ||
                itemName.contains("shears") ||
                itemName.contains("spyglass") ||
                itemName.contains("lightning_rod") ||
                itemName.contains("clock") ||
                itemName.contains("mallet") ||
                itemName.contains("magnet");
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment == Enchantments.BLOCK_FORTUNE;
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {

            private ComicallyLargeMagnetRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new ComicallyLargeMagnetRenderer();
                }
                return this.renderer;
            }

        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}
