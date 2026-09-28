package com.riftmod.event;

import com.riftmod.dimension.ModDimensions;
import com.riftmod.util.RiftTeleportHelper;
import com.riftmod.world.InterworldRiftManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ModEvents {

    // World border for Interworld: 10000 x 10000
    private static final int BORDER_SIZE = 10000;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level.isClientSide) return;
        if (!(event.player instanceof ServerPlayerEntity)) return;

        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        ServerWorld world = player.getLevel();

        if (ModDimensions.isInterworld(world.dimension())) {
            // Enforce world border
            enforceBorder(player);

            // Check if player fell into the central black hole (near 0,0)
            checkBlackHoleEntry(player);

            // Handle scattered rifts tooltips & interaction
            InterworldRiftManager.tickPlayer(player);
        }
    }

    private void enforceBorder(ServerPlayerEntity player) {
        double x = player.getX();
        double z = player.getZ();
        double half = BORDER_SIZE / 2.0;

        if (Math.abs(x) > half || Math.abs(z) > half) {
            // Push back
            double nx = Math.max(-half + 5, Math.min(half - 5, x));
            double nz = Math.max(-half + 5, Math.min(half - 5, z));
            player.teleportTo(nx, player.getY(), nz);
            player.displayClientMessage(
                    new StringTextComponent("The void boundary rejects you...").withStyle(TextFormatting.DARK_PURPLE),
                    true);
        }
    }

    private void checkBlackHoleEntry(ServerPlayerEntity player) {
        // Central black hole region roughly |x| < 40 && |z| < 40 && y < 40
        if (Math.abs(player.getX()) < 45 && Math.abs(player.getZ()) < 45 && player.getY() < 50) {
            // Simple gravity pull visual is client, but teleport when close enough
            if (Math.abs(player.getX()) < 12 && Math.abs(player.getZ()) < 12 && player.getY() < 30) {
                RiftTeleportHelper.teleportToBlackHoleCore(player);
            }
        }
    }

    // Allow beds in Black Hole Core
    @SubscribeEvent
    public void onSleep(PlayerEvent.PlayerChangeDimensionEvent event) {
        // handled by dimension_type bed_works = true
    }

    // Clean up temporary portals / entry portal disappears on arrival
    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() == ModDimensions.INTERWORLD_KEY) {
            // Entry portal already temporary; nothing permanent to clean
            if (event.getPlayer() instanceof ServerPlayerEntity) {
                ServerPlayerEntity p = (ServerPlayerEntity) event.getPlayer();
                p.displayClientMessage(
                        new StringTextComponent("You have entered The Interworld...").withStyle(TextFormatting.DARK_PURPLE),
                        false);
            }
        }
    }
}
