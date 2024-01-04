package com.leecrafts.thingamabobs.misc;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.*;

public class FixedDamageExplosion extends Explosion {

    private final float damage;

    public FixedDamageExplosion(Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, float damage, boolean fire, Explosion.BlockInteraction pBlockInteraction) {
        this(pLevel, pSource, null, null, pToBlowX, pToBlowY, pToBlowZ, pRadius, damage, fire, pBlockInteraction);
    }

    public FixedDamageExplosion(Level pLevel, @Nullable Entity pSource, @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pDamageCalculator, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, float damage, boolean pFire, Explosion.BlockInteraction pBlockInteraction) {
        super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, SoundEvents.GENERIC_EXPLODE);
        this.damage = damage;
    }

    @Override
    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new Vec3(this.x, this.y, this.z));
        Set<BlockPos> set = Sets.newHashSet();

        int k;
        int l;
        for(int j = 0; j < 16; ++j) {
            for(k = 0; k < 16; ++k) {
                for(l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
                        double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
                        double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
                        double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F);
                        double d4 = this.x;
                        double d6 = this.y;
                        double d8 = this.z;

                        for(float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPos blockpos = BlockPos.containing(d4, d6, d8);
                            BlockState blockstate = this.level.getBlockState(blockpos);
                            FluidState fluidstate = this.level.getFluidState(blockpos);
                            if (!this.level.isInWorldBounds(blockpos)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
                            if (optional.isPresent()) {
                                f -= ((Float)optional.get() + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, f)) {
                                set.add(blockpos);
                            }

                            d4 += d0 * 0.30000001192092896;
                            d6 += d1 * 0.30000001192092896;
                            d8 += d2 * 0.30000001192092896;
                        }
                    }
                }
            }
        }

        this.toBlow.addAll(set);
        float f2 = this.radius * 2.0F;
        k = Mth.floor(this.x - (double)f2 - 1.0);
        l = Mth.floor(this.x + (double)f2 + 1.0);
        int i2 = Mth.floor(this.y - (double)f2 - 1.0);
        int i1 = Mth.floor(this.y + (double)f2 + 1.0);
        int j2 = Mth.floor(this.z - (double)f2 - 1.0);
        int j1 = Mth.floor(this.z + (double)f2 + 1.0);
        List<Entity> list = this.level.getEntities(this.source, new AABB((double)k, (double)i2, (double)j2, (double)l, (double)i1, (double)j1));
        ForgeEventFactory.onExplosionDetonate(this.level, this, list, (double)f2);
        Vec3 vec3 = new Vec3(this.x, this.y, this.z);
        Iterator var34 = list.iterator();

        while(true) {
            Player player;
            Vec3 vec31;
            do {
                do {
                    Entity entity;
                    do {
                        double d5;
                        double d7;
                        double d9;
                        double d11;
                        double d12;
                        do {
                            do {
                                do {
                                    if (!var34.hasNext()) {
                                        return;
                                    }

                                    entity = (Entity)var34.next();
                                } while(entity.ignoreExplosion(this));

                                d11 = Math.sqrt(entity.distanceToSqr(vec3)) / (double)f2;
                            } while(!(d11 <= 1.0));

                            d5 = entity.getX() - this.x;
                            d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                            d9 = entity.getZ() - this.z;
                            d12 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                        } while(d12 == 0.0);

                        d5 /= d12;
                        d7 /= d12;
                        d9 /= d12;
//                        if (this.damageCalculator.shouldDamageEntity(this, entity)) {
//                            entity.hurt(this.damageSource, this.damageCalculator.getEntityDamageAmount(this, entity));
//                        }
                        entity.hurt(this.damageSource, this.damage);

                        double d13 = (1.0 - d11) * (double)getSeenPercent(vec3, entity);
                        double d10;
                        if (entity instanceof LivingEntity livingentity) {
                            d10 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingentity, d13);
                        } else {
                            d10 = d13;
                        }

                        d5 *= d10;
                        d7 *= d10;
                        d9 *= d10;
                        vec31 = new Vec3(d5, d7, d9);
                        entity.setDeltaMovement(entity.getDeltaMovement().add(vec31));
                    } while(!(entity instanceof Player));

                    player = (Player)entity;
                } while(player.isSpectator());
            } while(player.isCreative() && player.getAbilities().flying);

            this.hitPlayers.put(player, vec31);
        }
    }

}
