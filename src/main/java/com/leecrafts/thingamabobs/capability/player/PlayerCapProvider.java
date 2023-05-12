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
        nbt.putInt("first_person_mallet_charge_offset", playerCap.firstPersonMalletChargeOffset);
        nbt.putInt("first_person_mallet_equip_animation", playerCap.firstPersonMalletEquipAnim);
        nbt.putInt("first_person_mallet_swing_animation", playerCap.firstPersonMalletSwingAnim);
        nbt.putInt("first_person_mallet_pickup_animation", playerCap.firstPersonMalletPickupAnim);
        nbt.putBoolean("was_holding_mallet", playerCap.wasHoldingMallet);
        nbt.putBoolean("third_person_mallet_animation_was_reset", playerCap.thirdPersonMalletAnimWasReset);
        nbt.putInt("third_person_mallet_swing_animation", playerCap.thirdPersonMalletSwingAnim);
        nbt.putBoolean("third_person_mallet_was_swinging", playerCap.thirdPersonMalletWasSwinging);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.PLAYER_CAPABILITY != null) {
            playerCap.malletCharge = nbt.getInt("mallet_charge");
            playerCap.firstPersonMalletChargeOffset = nbt.getInt("first_person_mallet_charge_offset");
            playerCap.firstPersonMalletEquipAnim = nbt.getInt("first_person_mallet_equip_animation");
            playerCap.firstPersonMalletSwingAnim = nbt.getInt("first_person_mallet_swing_animation");
            playerCap.firstPersonMalletPickupAnim = nbt.getInt("first_person_mallet_pickup_animation");
            playerCap.wasHoldingMallet = nbt.getBoolean("was_holding_mallet");
            playerCap.thirdPersonMalletAnimWasReset = nbt.getBoolean("third_person_mallet_animation_was_reset");
            playerCap.thirdPersonMalletSwingAnim = nbt.getInt("third_person_mallet_swing_animation");
            playerCap.thirdPersonMalletWasSwinging = nbt.getBoolean("third_person_mallet_was_swinging");
        }
    }

}
