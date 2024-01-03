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
    protected @NotNull TriggerInstance createInstance(@NotNull JsonObject pJson, EntityPredicate.@NotNull Composite pPlayer, @NotNull DeserializationContext pContext) {
        MinMaxBounds.Ints minMaxBounds = MinMaxBounds.Ints.fromJson(pJson.get("num_entities"));
        return new HitByAOEWeaponTrigger.TriggerInstance(pPlayer, minMaxBounds);
    }

    public void trigger(ServerPlayer serverPlayer, int numEntities) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.atLeast(numEntities));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        private final MinMaxBounds.Ints entities;

        public TriggerInstance(EntityPredicate.Composite player, MinMaxBounds.Ints entities) {
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
