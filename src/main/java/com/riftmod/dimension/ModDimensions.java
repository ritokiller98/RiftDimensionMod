package com.riftmod.dimension;

import com.riftmod.RiftDimensionMod;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RiftDimensionMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModDimensions {

    public static final RegistryKey<World> INTERWORLD_KEY = RegistryKey.create(Registry.DIMENSION_REGISTRY,
            new ResourceLocation(RiftDimensionMod.MOD_ID, "interworld"));

    public static final RegistryKey<World> BLACK_HOLE_CORE_KEY = RegistryKey.create(Registry.DIMENSION_REGISTRY,
            new ResourceLocation(RiftDimensionMod.MOD_ID, "black_hole_core"));

    public static final RegistryKey<DimensionType> INTERWORLD_TYPE = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY,
            new ResourceLocation(RiftDimensionMod.MOD_ID, "interworld"));

    public static final RegistryKey<DimensionType> BLACK_HOLE_CORE_TYPE = RegistryKey.create(Registry.DIMENSION_TYPE_REGISTRY,
            new ResourceLocation(RiftDimensionMod.MOD_ID, "black_hole_core"));

    public static void register() {
        // Registration happens via datapack / DimensionManager in 1.16.5
    }

    public static void init() {
        // Additional init if needed
    }

    public static boolean isCustomDimension(RegistryKey<World> key) {
        return key == INTERWORLD_KEY || key == BLACK_HOLE_CORE_KEY;
    }

    public static boolean isInterworld(RegistryKey<World> key) {
        return key == INTERWORLD_KEY;
    }

    public static boolean isBlackHoleCore(RegistryKey<World> key) {
        return key == BLACK_HOLE_CORE_KEY;
    }
}
