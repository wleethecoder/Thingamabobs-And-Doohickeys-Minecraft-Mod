package com.leecrafts.thingamabobs.capability.player;

import com.leecrafts.thingamabobs.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final PlayerCap playerCap = new PlayerCap();

    private final LazyOptional<IPlayerCap> playerCapLazyOptional = LazyOptional.of(() -> playerCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.PLAYER_CAPABILITY.orEmpty(cap, playerCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.PLAYER_CAPABILITY == null) return nbt;
        nbt.putInt("mallet_charge", playerCap.malletCharge);
        nbt.putInt("mallet_equip_animation", playerCap.malletEquipAnim);
        nbt.putInt("mallet_swing_animation", playerCap.malletSwingAnim);
        nbt.putInt("mallet_pickup_animation", playerCap.malletPickupAnim);
        nbt.putBoolean("was_holding_mallet", playerCap.wasHoldingMallet);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.PLAYER_CAPABILITY != null) {
            playerCap.malletCharge = nbt.getInt("mallet_charge");
            playerCap.malletEquipAnim = nbt.getInt("mallet_equip_animation");
            playerCap.malletSwingAnim = nbt.getInt("mallet_swing_animation");
            playerCap.malletPickupAnim = nbt.getInt("mallet_pickup_animation");
            playerCap.wasHoldingMallet = nbt.getBoolean("was_holding_mallet");
        }
    }

}
