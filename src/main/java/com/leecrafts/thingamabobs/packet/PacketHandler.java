package com.leecrafts.thingamabobs.packet;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {

    public static final ResourceLocation MAIN = new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "main");

    public static final SimpleChannel INSTANCE = ChannelBuilder
            .named(MAIN)
            .optional()
            .networkProtocolVersion(0)
            .simpleChannel();

    private PacketHandler() {
    }

    public static void init() {
        int index = 0;
        INSTANCE.messageBuilder(ServerboundComicallyLargeMalletAttackPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundComicallyLargeMalletAttackPacket::encode).decoder(ServerboundComicallyLargeMalletAttackPacket::new)
                .consumerMainThread(ServerboundComicallyLargeMalletAttackPacket::handle).add();
        INSTANCE.messageBuilder(ServerboundComicallyLargeMalletAnimationPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundComicallyLargeMalletAnimationPacket::encode).decoder(ServerboundComicallyLargeMalletAnimationPacket::new)
                .consumerMainThread(ServerboundComicallyLargeMalletAnimationPacket::handle).add();
        INSTANCE.messageBuilder(ServerboundComicallyLargeMalletItemPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundComicallyLargeMalletItemPacket::encode).decoder(ServerboundComicallyLargeMalletItemPacket::new)
                .consumerMainThread(ServerboundComicallyLargeMalletItemPacket::handle).add();
        INSTANCE.messageBuilder(ClientboundComicallyLargeMalletAnimationPacket.class, index++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundComicallyLargeMalletAnimationPacket::encode).decoder(ClientboundComicallyLargeMalletAnimationPacket::new)
                .consumerMainThread(ClientboundComicallyLargeMalletAnimationPacket::handle).add();
        INSTANCE.messageBuilder(ServerboundSpringLoadedBoxingGloveAttackPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundSpringLoadedBoxingGloveAttackPacket::encode).decoder(ServerboundSpringLoadedBoxingGloveAttackPacket::new)
                .consumerMainThread(ServerboundSpringLoadedBoxingGloveAttackPacket::handle).add();
        INSTANCE.messageBuilder(ServerboundSpringLoadedBoxingGloveAnimationPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundSpringLoadedBoxingGloveAnimationPacket::encode).decoder(ServerboundSpringLoadedBoxingGloveAnimationPacket::new)
                .consumerMainThread(ServerboundSpringLoadedBoxingGloveAnimationPacket::handle).add();
    }

}
