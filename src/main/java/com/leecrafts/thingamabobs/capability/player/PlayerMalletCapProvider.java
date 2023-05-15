package com.leecrafts.thingamabobs.capability.player;

import com.leecrafts.thingamabobs.capability.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerMalletCapProvider implements ICapabilitySerializable<CompoundTag> {

    private final PlayerMalletCap playerMalletCap = new PlayerMalletCap();

    private final LazyOptional<IPlayerMalletCap> playerCapLazyOptional = LazyOptional.of(() -> playerMalletCap);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.PLAYER_MALLET_CAPABILITY.orEmpty(cap, playerCapLazyOptional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (ModCapabilities.PLAYER_MALLET_CAPABILITY == null) return nbt;
        nbt.putInt("mallet_charge", playerMalletCap.malletCharge);
        nbt.putInt("first_person_mallet_charge_offset", playerMalletCap.firstPersonMalletChargeOffset);
        nbt.putInt("first_person_mallet_equip_animation", playerMalletCap.firstPersonMalletEquipAnim);
        nbt.putInt("first_person_mallet_swing_animation", playerMalletCap.firstPersonMalletSwingAnim);
        nbt.putInt("first_person_mallet_pickup_animation", playerMalletCap.firstPersonMalletPickupAnim);
        nbt.putBoolean("was_holding_mallet", playerMalletCap.wasHoldingMallet);
        nbt.putBoolean("third_person_mallet_animation_was_idle", playerMalletCap.thirdPersonMalletAnimWasIdle);
        nbt.putInt("third_person_mallet_swing_animation", playerMalletCap.thirdPersonMalletSwingAnim);
        nbt.putBoolean("third_person_mallet_was_charging", playerMalletCap.thirdPersonMalletWasCharging);
        nbt.putBoolean("third_person_mallet_animation_was_stopped", playerMalletCap.thirdPersonMalletAnimWasStopped);
        nbt.putBoolean("third_person_mallet_animation_was_paused", playerMalletCap.thirdPersonMalletAnimWasPaused);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (ModCapabilities.PLAYER_MALLET_CAPABILITY != null) {
            playerMalletCap.malletCharge = nbt.getInt("mallet_charge");
            playerMalletCap.firstPersonMalletChargeOffset = nbt.getInt("first_person_mallet_charge_offset");
            playerMalletCap.firstPersonMalletEquipAnim = nbt.getInt("first_person_mallet_equip_animation");
            playerMalletCap.firstPersonMalletSwingAnim = nbt.getInt("first_person_mallet_swing_animation");
            playerMalletCap.firstPersonMalletPickupAnim = nbt.getInt("first_person_mallet_pickup_animation");
            playerMalletCap.wasHoldingMallet = nbt.getBoolean("was_holding_mallet");
            playerMalletCap.thirdPersonMalletAnimWasIdle = nbt.getBoolean("third_person_mallet_animation_was_idle");
            playerMalletCap.thirdPersonMalletSwingAnim = nbt.getInt("third_person_mallet_swing_animation");
            playerMalletCap.thirdPersonMalletWasCharging = nbt.getBoolean("third_person_mallet_was_charging");
            playerMalletCap.thirdPersonMalletAnimWasStopped = nbt.getBoolean("third_person_mallet_animation_was_stopped");
            playerMalletCap.thirdPersonMalletAnimWasPaused = nbt.getBoolean("third_person_mallet_animation_was_paused");
        }
    }

}
