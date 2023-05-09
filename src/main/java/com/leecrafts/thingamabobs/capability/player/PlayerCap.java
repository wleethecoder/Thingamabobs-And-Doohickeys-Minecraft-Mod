package com.leecrafts.thingamabobs.capability.player;

import com.leecrafts.thingamabobs.render.GeckoPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

public class PlayerCap implements IPlayerCap {

    public int malletCharge;
    public int malletEquipAnim;
    public int malletSwingAnim;
    public int malletPickupAnim;
    public boolean wasHoldingMallet;
    private GeckoPlayer geckoPlayer;

    public PlayerCap() {
        this.malletCharge = 0;
        this.malletEquipAnim = 0;
        this.malletSwingAnim = -1;
        this.malletPickupAnim = 0;
        this.wasHoldingMallet = false;
    }

    public void addedToWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            System.out.println("jimmy eat world!");
            Player player = (Player) event.getEntity();
            this.geckoPlayer = new GeckoPlayer(player);
        }
    }

    public GeckoPlayer getGeckoPlayer() {
        return this.geckoPlayer;
    }

}
