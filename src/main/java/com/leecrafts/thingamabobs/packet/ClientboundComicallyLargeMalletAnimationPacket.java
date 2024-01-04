package com.leecrafts.thingamabobs.packet;

import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.AnimationBinary;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

import java.io.IOException;

import static com.leecrafts.thingamabobs.event.ModEvents.ClientForgeEvents.*;

public class ClientboundComicallyLargeMalletAnimationPacket {

    public final int playerId;
    public final float animSpeed;
    public final boolean mirrored;
    public final boolean fade;
    public final ByteBuf animBytes;

    public ClientboundComicallyLargeMalletAnimationPacket(int playerId, float animSpeed, boolean mirrored, boolean fade, ByteBuf animBytes) {
        this.playerId = playerId;
        this.animSpeed = animSpeed;
        this.mirrored = mirrored;
        this.fade = fade;
        this.animBytes = animBytes;
    }

    public ClientboundComicallyLargeMalletAnimationPacket(FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBytes(buffer.readableBytes()));
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.playerId);
        buffer.writeFloat(this.animSpeed);
        buffer.writeBoolean(this.mirrored);
        buffer.writeBoolean(this.fade);
        buffer.writeBytes(this.animBytes);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            LocalPlayer thisPlayer = Minecraft.getInstance().player;
            if (thisPlayer != null) {
                if (thisPlayer.level.getEntity(this.playerId) instanceof AbstractClientPlayer trackedPlayer) {
                    var animation = getAnimation(trackedPlayer);
                    if (animation != null) {
                        int modifiersListSize = animation.size();
                        for (int i = 0; i < modifiersListSize - 1; i++) {
                            animation.removeModifier(animation.size() - 1);
                        }
                        if (this.animSpeed >= 0) {
//                            System.out.println("\tmodify animation speed to " + this.animSpeed);
                            setSpeed(animation, this.animSpeed);
                            if (this.mirrored) {
//                                System.out.println("\tmirrored");
                                animation.addModifierLast(MIRROR);
                            }
                            if (this.animBytes.capacity() > 0) {
//                                System.out.println("\tSet new animation");
                                try {
                                    KeyframeAnimation keyframeAnimation = AnimationBinary.read(this.animBytes.nioBuffer(), AnimationBinary.getCurrentVersion());
                                    if (this.fade) {
//                                        System.out.println("\tfade");
                                        AbstractFadeModifier abstractFadeModifier = AbstractFadeModifier.standardFadeIn(5, Ease.OUTSINE);
                                        animation.replaceAnimationWithFade(abstractFadeModifier, new KeyframeAnimationPlayer(keyframeAnimation), true);
                                    }
                                    else {
                                        animation.setAnimation(new KeyframeAnimationPlayer(keyframeAnimation));
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        else {
                            animation.setAnimation(null);
                        }
                    }
                }
            }
        }));
        ctx.setPacketHandled(true);
    }

}
