package com.leecrafts.thingamabobs.criterion.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HitByAOEWeaponTrigger extends SimpleCriterionTrigger<HitByAOEWeaponTrigger.TriggerInstance> {

    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer, int numEntities) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(numEntities));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints entities) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<HitByAOEWeaponTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create((p_311996_) -> p_311996_.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "num_entities", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::entities)).apply(p_311996_, TriggerInstance::new));

        public boolean matches(int numEntities) {
            return this.entities.matches(numEntities);
        }

        public @NotNull Optional<ContextAwarePredicate> player() {
            return this.player;
        }

    }

}
