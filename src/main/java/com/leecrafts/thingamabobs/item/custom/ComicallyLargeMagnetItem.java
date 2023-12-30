package com.leecrafts.thingamabobs.item.custom;

import com.leecrafts.thingamabobs.item.client.ComicallyLargeMagnetRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class ComicallyLargeMagnetItem extends Item implements Vanishable, IForgeItem, GeoItem {

    private static final int REACH = 8;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, true);

    public ComicallyLargeMagnetItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide &&
                pIsSelected &&
                pEntity.tickCount % (TICKS_PER_SECOND / 2) == 0 &&
                (!(pEntity instanceof Player) || !pEntity.isSpectator())) {
            // I tried using BlockPos.withinManhattan and BlockPos.betweenClosedStream, but neither seemed efficient
            int amount = 0;
            for (int x = -REACH; x <= REACH; x++) {
                for (int y = -REACH; y <= REACH; y++) {
                    for (int z = -REACH; z <= REACH; z++) {
                        BlockPos blockPos = pEntity.blockPosition().offset(x, y, z);
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
                            amount++;
                            if (pEntity instanceof Player player && !player.isCreative()) {
                                player.awardStat(Stats.BLOCK_MINED.get(blockState.getBlock()));
                            }
                        }
                    }
                }
            }

            if (amount > 0) {
                if (pEntity instanceof LivingEntity livingEntity) {
                    pStack.hurtAndBreak(amount, livingEntity, (livingEntity1 -> livingEntity1.broadcastBreakEvent(EquipmentSlot.MAINHAND)));
                }
                else {
                    pStack.hurt(amount, pLevel.random, null);
                }
            }
        }
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
