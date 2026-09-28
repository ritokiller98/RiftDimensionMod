package com.riftmod.util;

import com.riftmod.dimension.ModDimensions;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public class RiftTeleportHelper {

    /**
     * Creates a temporary 2x3 vertical portal frame 1 block in front of the player
     * and immediately teleports the player into The Interworld.
     * The entry portal is removed on arrival (handled in dimension events).
     */
    public static void createTemporaryRiftAndTeleport(ServerPlayerEntity player, BlockPos frontPos) {
        ServerWorld world = player.getLevel();

        // Simple temporary portal blocks (obsidian frame + portal-like)
        // We use a lightweight custom block or just air + particles for performance.
        // For stability we place a few blocks then teleport.

        // Place a minimal visual frame (will be cleaned later if needed)
        placeTempFrame(world, frontPos);

        // Teleport to Interworld at safe spawn near center
        ServerWorld interworld = world.getServer().getLevel(ModDimensions.INTERWORLD_KEY);
        if (interworld == null) {
            return;
        }

        // Spawn near (0, 80, 0) but slightly offset so player doesn't fall into black hole immediately
        BlockPos dest = new BlockPos(32, 80, 32);
        dest = findSafeSpawn(interworld, dest);

        teleportPlayer(player, interworld, dest, player.yRot, player.xRot);
    }

    public static void returnToOrigin(ServerPlayerEntity player) {
        PlayerRiftData.OriginData origin = PlayerRiftData.getOrigin(player);
        if (origin == null) {
            // Fallback to Overworld spawn
            ServerWorld overworld = player.getServer().getLevel(World.OVERWORLD);
            if (overworld != null) {
                BlockPos spawn = overworld.getSharedSpawnPos();
                teleportPlayer(player, overworld, spawn, player.yRot, player.xRot);
            }
            return;
        }

        ServerWorld target = player.getServer().getLevel(origin.dimension);
        if (target == null) {
            target = player.getServer().getLevel(World.OVERWORLD);
        }
        if (target != null) {
            teleportPlayer(player, target, origin.pos, player.yRot, player.xRot);
            PlayerRiftData.clearOrigin(player);
        }
    }

    public static void teleportToBlackHoleCore(ServerPlayerEntity player) {
        ServerWorld core = player.getServer().getLevel(ModDimensions.BLACK_HOLE_CORE_KEY);
        if (core == null) return;

        // Center of the 50x4 room
        BlockPos dest = new BlockPos(25, 2, 25);
        teleportPlayer(player, core, dest, player.yRot, player.xRot);
    }

    private static void teleportPlayer(ServerPlayerEntity player, ServerWorld targetWorld,
                                       BlockPos pos, float yRot, float xRot) {
        player.changeDimension(targetWorld, new ITeleporter() {
            @Override
            public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld,
                                      float yaw, Function<Boolean, Entity> repositionEntity) {
                Entity e = repositionEntity.apply(false);
                e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yRot, xRot);
                return e;
            }
        });
    }

    private static BlockPos findSafeSpawn(ServerWorld world, BlockPos preferred) {
        // Simple scan upward for air
        for (int y = preferred.getY(); y < 120; y++) {
            BlockPos check = new BlockPos(preferred.getX(), y, preferred.getZ());
            if (world.isEmptyBlock(check) && world.isEmptyBlock(check.above())) {
                return check;
            }
        }
        return preferred;
    }

    private static void placeTempFrame(ServerWorld world, BlockPos base) {
        // Minimal 2 wide x 3 high frame using end portal frame or obsidian for visual
        // For performance we place only a few blocks and rely on particles
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                BlockPos p = base.offset(x, y, 0);
                if (world.isEmptyBlock(p)) {
                    world.setBlock(p, Blocks.OBSIDIAN.defaultBlockState(), 3);
                }
            }
        }
        // Center portal-ish
        world.setBlock(base.offset(0, 1, 0), Blocks.END_PORTAL.defaultBlockState(), 3);
        world.setBlock(base.offset(1, 1, 0), Blocks.END_PORTAL.defaultBlockState(), 3);
    }
}
