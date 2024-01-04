package com.leecrafts.thingamabobs.criterion.custom;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HitByAOEWeaponTrigger extends SimpleCriterionTrigger<HitByAOEWeaponTrigger.TriggerInstance> {

    @Override
    protected @NotNull TriggerInstance createInstance(@NotNull JsonObject p_66248_, @NotNull Optional<ContextAwarePredicate> p_286603_, @NotNull DeserializationContext p_66250_) {
        MinMaxBounds.Ints minMaxBounds = MinMaxBounds.Ints.fromJson(p_66248_.get("num_entities"));
        return new HitByAOEWeaponTrigger.TriggerInstance(p_286603_, minMaxBounds);
    }

    public void trigger(ServerPlayer serverPlayer, int numEntities) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.atLeast(numEntities));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final MinMaxBounds.Ints entities;

        public TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints entities) {
            super(player);
            this.entities = entities;
        }

        public boolean atLeast(int numEntities) {
            return this.entities.matches(numEntities);
        }

        @Override
        public @NotNull JsonObject serializeToJson() {
            JsonObject jsonObject = super.serializeToJson();
            jsonObject.add("num_entities", this.entities.serializeToJson());
            return jsonObject;
        }
    }

}
