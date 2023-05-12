package com.leecrafts.thingamabobs.capability.player;

public class PlayerCap implements IPlayerCap {

    public int malletCharge;
    public int firstPersonMalletChargeOffset;
    public int firstPersonMalletEquipAnim;
    public int firstPersonMalletSwingAnim;
    public int firstPersonMalletPickupAnim;
    public boolean wasHoldingMallet;
    public boolean thirdPersonMalletAnimWasReset;
    public int thirdPersonMalletSwingAnim;
    public boolean thirdPersonMalletWasSwinging;

    public PlayerCap() {
        this.malletCharge = 0;
        this.firstPersonMalletChargeOffset = 0;
        this.firstPersonMalletEquipAnim = 0;
        this.wasHoldingMallet = false;
        this.thirdPersonMalletAnimWasReset = false;
        this.thirdPersonMalletWasSwinging = false;
        this.resetAnim();
    }

    public void resetAnim() {
        this.firstPersonMalletSwingAnim = -1;
        this.firstPersonMalletPickupAnim = 0;
        this.thirdPersonMalletSwingAnim = -1;
    }

}
