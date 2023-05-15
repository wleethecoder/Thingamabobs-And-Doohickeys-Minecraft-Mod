package com.leecrafts.thingamabobs.capability.player;

public class PlayerMalletCap implements IPlayerMalletCap {

    public int malletCharge;
    public int firstPersonMalletChargeOffset;
    public int firstPersonMalletEquipAnim;
    public int firstPersonMalletSwingAnim;
    public int firstPersonMalletPickupAnim;
    public boolean wasHoldingMallet;
    public boolean thirdPersonMalletAnimWasIdle;
    public int thirdPersonMalletSwingAnim;
    public boolean thirdPersonMalletWasCharging;
    public boolean thirdPersonMalletAnimWasStopped;
    public boolean thirdPersonMalletAnimWasPaused;

    public PlayerMalletCap() {
        this.malletCharge = 0;
        this.firstPersonMalletChargeOffset = 0;
        this.thirdPersonMalletWasCharging = false;
        this.thirdPersonMalletAnimWasStopped = false;
        this.thirdPersonMalletAnimWasPaused = false;
        this.resetAnim();
    }

    public void resetAnim() {
        this.wasHoldingMallet = false;
        this.firstPersonMalletEquipAnim = 0;
        this.firstPersonMalletSwingAnim = -1;
        this.firstPersonMalletPickupAnim = 0;
        this.thirdPersonMalletAnimWasIdle = false;
        this.thirdPersonMalletSwingAnim = -1;
    }

}
