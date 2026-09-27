package com.riftmod;

import com.riftmod.dimension.ModDimensions;
import com.riftmod.item.ModItems;
import com.riftmod.network.ModNetwork;
import com.riftmod.event.ModEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RiftDimensionMod.MOD_ID)
public class RiftDimensionMod {
    public static final String MOD_ID = "riftdimension";
    public static final Logger LOGGER = LogManager.getLogger();

    public RiftDimensionMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModDimensions.register();

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(new ModEvents());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            ModDimensions.init();
        });
        LOGGER.info("Rift Dimension Mod common setup complete");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            com.riftmod.client.ClientSetup.init();
        });
        LOGGER.info("Rift Dimension Mod client setup complete");
    }
}
