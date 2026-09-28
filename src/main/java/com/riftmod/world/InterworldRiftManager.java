package com.riftmod.world;

import com.riftmod.dimension.ModDimensions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

/**
 * Manages scattered dimensional rifts in The Interworld.
 * Rifts are pre-generated far apart and evenly distributed.
 * Tooltips show target coordinates, biome and dimension when player is close and looking.
 */
public class InterworldRiftManager {

    private static final int RIFT_COUNT = 48;           // enough for large world, still sparse
    private static final int MIN_DISTANCE = 800;        // far apart
    private static final double LOOK_RANGE = 4.5;

    private static final List<RiftData> RIFTS = new ArrayList<>();
    private static boolean generated = false;

    public static void ensureGenerated(ServerWorld world) {
        if (generated) return;
        generated = true;

        Random rand = new Random(world.getSeed() ^ 0xDEADBEEFL);
        List<BlockPos> placed = new ArrayList<>();

        for (int i = 0; i < RIFT_COUNT; i++) {
            int attempts = 0;
            BlockPos pos;
            do {
                int x = rand.nextInt(9000) - 4500;
                int z = rand.nextInt(9000) - 4500;
                // Keep away from center black hole
                if (Math.abs(x) < 200 && Math.abs(z) < 200) {
                    x += (x >= 0 ? 300 : -300);
                    z += (z >= 0 ? 300 : -300);
                }
                pos = new BlockPos(x, 70 + rand.nextInt(30), z);
                attempts++;
            } while (attempts < 40 && isTooClose(pos, placed));

            placed.add(pos);

            // Unique destination
            RegistryKey<World> destDim = pickRandomDimension(rand);
            BlockPos destPos = new BlockPos(
                    rand.nextInt(20000) - 10000,
                    64 + rand.nextInt(40),
                    rand.nextInt(20000) - 10000
            );

            String biomeName = "unknown";
            RIFTS.add(new RiftData(pos, destDim, destPos, biomeName));
        }
    }

    private static boolean isTooClose(BlockPos pos, List<BlockPos> placed) {
        for (BlockPos p : placed) {
            if (p.distSqr(pos) < (long) MIN_DISTANCE * MIN_DISTANCE) return true;
        }
        return false;
    }

    private static RegistryKey<World> pickRandomDimension(Random rand) {
        int r = rand.nextInt(3);
        if (r == 0) return World.OVERWORLD;
        if (r == 1) return World.NETHER;
        return World.END;
    }

    public static void tickPlayer(ServerPlayerEntity player) {
        ServerWorld world = player.getLevel();
        if (!ModDimensions.isInterworld(world.dimension())) return;

        ensureGenerated(world);

        Vector3d eye = player.getEyePosition(1.0f);
        Vector3d look = player.getLookAngle();

        RiftData closest = null;
        double closestDist = LOOK_RANGE;

        for (RiftData rift : RIFTS) {
            double dist = Math.sqrt(rift.pos.distSqr(eye.x, eye.y, eye.z, true));
            if (dist > LOOK_RANGE) continue;

            // Check if player is roughly looking at it
            Vector3d toRift = new Vector3d(rift.pos.getX() + 0.5 - eye.x,
                    rift.pos.getY() + 1.0 - eye.y,
                    rift.pos.getZ() + 0.5 - eye.z).normalize();
            double dot = look.dot(toRift);
            if (dot > 0.85 && dist < closestDist) {
                closestDist = dist;
                closest = rift;
            }
        }

        if (closest != null) {
            String dimName = closest.destDim.location().getPath();
            player.displayClientMessage(
                    new StringTextComponent(String.format(
                            "§5Rift Destination§r: %s  §7|§r  %d, %d, %d  §7|§r  Dim: %s",
                            closest.biomeName,
                            closest.destPos.getX(), closest.destPos.getY(), closest.destPos.getZ(),
                            dimName
                    )).withStyle(TextFormatting.LIGHT_PURPLE),
                    true);
        }
    }

    public static class RiftData {
        public final BlockPos pos;
        public final RegistryKey<World> destDim;
        public final BlockPos destPos;
        public final String biomeName;

        public RiftData(BlockPos pos, RegistryKey<World> destDim, BlockPos destPos, String biomeName) {
            this.pos = pos;
            this.destDim = destDim;
            this.destPos = destPos;
            this.biomeName = biomeName;
        }
    }
}
