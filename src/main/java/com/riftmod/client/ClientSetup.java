package com.riftmod.client;

import com.riftmod.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ISkyRenderHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class ClientSetup {

    public static void init() {
        // Register custom DimensionRenderInfo for Interworld
        DimensionRenderInfo.EFFECTS.put(
                new ResourceLocation("riftdimension", "interworld"),
                new InterworldRenderInfo()
        );
    }

    /**
     * Custom sky + fog for The Interworld.
     * Absolute black void + dense purple/black cosmic fog + distant stars.
     * Optimized for both high and low render distance.
     */
    public static class InterworldRenderInfo extends DimensionRenderInfo {
        public InterworldRenderInfo() {
            super(Float.NaN, false, DimensionRenderInfo.FogType.NONE, false, true);
            this.setSkyRenderHandler(new CosmicSkyRenderer());
        }

        @Override
        public Vector3d getBrightnessDependentFogColor(Vector3d biomeFogColor, float daylight) {
            // Deep purple / cosmic black fog
            return new Vector3d(0.05, 0.0, 0.12);
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return true; // Always dense fog
        }
    }
}
