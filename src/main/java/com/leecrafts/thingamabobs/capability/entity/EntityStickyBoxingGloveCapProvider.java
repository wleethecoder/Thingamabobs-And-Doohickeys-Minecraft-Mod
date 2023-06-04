package com.leecrafts.thingamabobs.capability.entity;

import com.leecrafts.thingamabobs.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityStickyBoxingGloveCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final EntityStickyBoxingGloveCap entityStickyBoxingGloveCap = new EntityStickyBoxingGloveCap();

    private final LazyOptional<IEntityStickyBoxingGloveCap> entityStickyBoxingGloveCapLazyOptional = LazyOptional.of(() -> entityStickyBoxingGloveCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY.orEmpty(cap, entityStickyBoxingGloveCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY == null) return nbt;
        nbt.putInt("boxing_glove_id", entityStickyBoxingGloveCap.boxingGloveId);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.ENTITY_STICKY_BOXING_GLOVE_CAPABILITY != null) {
            entityStickyBoxingGloveCap.boxingGloveId = nbt.getInt("boxing_glove_id");
        }
    }

    public void invalidate() {
        entityStickyBoxingGloveCapLazyOptional.invalidate();
    }

}
