package com.riftmod.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight server-side storage of player's original dimension + position
 * before entering The Interworld. Avoids NPE on return.
 */
public class PlayerRiftData {

    private static final Map<UUID, OriginData> ORIGINS = new HashMap<>();

    public static void setOrigin(ServerPlayerEntity player, RegistryKey<World> dim, BlockPos pos) {
        ORIGINS.put(player.getUUID(), new OriginData(dim, pos.immutable()));
    }

    public static OriginData getOrigin(ServerPlayerEntity player) {
        return ORIGINS.get(player.getUUID());
    }

    public static void clearOrigin(ServerPlayerEntity player) {
        ORIGINS.remove(player.getUUID());
    }

    public static class OriginData {
        public final RegistryKey<World> dimension;
        public final BlockPos pos;

        public OriginData(RegistryKey<World> dimension, BlockPos pos) {
            this.dimension = dimension;
            this.pos = pos;
        }
    }
}
