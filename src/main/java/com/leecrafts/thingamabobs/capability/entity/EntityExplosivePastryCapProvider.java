package com.leecrafts.thingamabobs.capability.entity;

import com.leecrafts.thingamabobs.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityExplosivePastryCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final EntityExplosivePastryCap entityExplosivePastryCap = new EntityExplosivePastryCap();

    private final LazyOptional<IEntityExplosivePastryCap> entityExplosivePastryCapLazyOptional = LazyOptional.of(() -> entityExplosivePastryCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.ENTITY_EXPLOSIVE_PASTRY_CAPABILITY) {
            return entityExplosivePastryCapLazyOptional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.ENTITY_EXPLOSIVE_PASTRY_CAPABILITY == null) return nbt;
        nbt.putInt("explosion_timestamp", entityExplosivePastryCap.explosionTimestamp);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.ENTITY_EXPLOSIVE_PASTRY_CAPABILITY != null) {
            entityExplosivePastryCap.explosionTimestamp = nbt.getInt("explosion_timestamp");
        }
    }

    public void invalidate() {
        entityExplosivePastryCapLazyOptional.invalidate();
    }

}
