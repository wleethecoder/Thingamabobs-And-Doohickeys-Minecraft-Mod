package com.leecrafts.thingamabobs.capability;

import com.leecrafts.thingamabobs.capability.player.IPlayerMalletCap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {

    public static final Capability<IPlayerMalletCap> PLAYER_MALLET_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

}
