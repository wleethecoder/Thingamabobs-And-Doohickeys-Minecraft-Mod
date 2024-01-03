package com.leecrafts.thingamabobs.criterion.custom;

import com.google.gson.JsonObject;
import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class HitByAOEWeaponTrigger extends SimpleCriterionTrigger<HitByAOEWeaponTrigger.TriggerInstance> {

    static final ResourceLocation ID = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "hit_by_aoe_weapon");

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    protected @NotNull TriggerInstance createInstance(@NotNull JsonObject p_66248_, @NotNull ContextAwarePredicate p_286603_, @NotNull DeserializationContext p_66250_) {
        MinMaxBounds.Ints minMaxBounds = MinMaxBounds.Ints.fromJson(p_66248_.get("num_entities"));
        return new HitByAOEWeaponTrigger.TriggerInstance(p_286603_, minMaxBounds);
    }

    public void trigger(ServerPlayer serverPlayer, int numEntities) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.atLeast(numEntities));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final MinMaxBounds.Ints entities;

        public TriggerInstance(ContextAwarePredicate player, MinMaxBounds.Ints entities) {
            super(HitByAOEWeaponTrigger.ID, player);
            this.entities = entities;
        }

        public boolean atLeast(int numEntities) {
            return this.entities.matches(numEntities);
        }

        @Override
        public @NotNull JsonObject serializeToJson(@NotNull SerializationContext pConditions) {
            JsonObject jsonObject = super.serializeToJson(pConditions);
            jsonObject.add("num_entities", this.entities.serializeToJson());
            return jsonObject;
        }
    }

}
