package com.leecrafts.thingamabobs.packet;

import com.leecrafts.thingamabobs.ThingamabobsAndDoohickeys;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ThingamabobsAndDoohickeys.MODID, "main"), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private PacketHandler() {
    }

    public static void init() {
        int index = 0;
        INSTANCE.messageBuilder(ServerboundLeftClickPacket.class, index++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerboundLeftClickPacket::encode).decoder(ServerboundLeftClickPacket::new)
                .consumerMainThread(ServerboundLeftClickPacket::handle).add();
    }

}
