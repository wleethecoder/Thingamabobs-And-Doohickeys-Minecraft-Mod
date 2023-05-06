package com.leecrafts.thingamabobs.capability.player;

public class PlayerCap implements IPlayerCap {

    public int malletCharge;
    public int malletEquipAnim;
    public int malletSwingAnim;
    public int malletPickupAnim;
    public boolean wasHoldingMallet;

    public PlayerCap() {
        this.malletCharge = 0;
        this.malletEquipAnim = 0;
        this.malletSwingAnim = -1;
        this.malletPickupAnim = 0;
        this.wasHoldingMallet = false;
    }

}
