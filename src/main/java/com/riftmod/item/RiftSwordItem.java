package com.riftmod.item;

import com.riftmod.dimension.ModDimensions;
import com.riftmod.util.RiftTeleportHelper;
import com.riftmod.util.PlayerRiftData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RiftSwordItem extends SwordItem {

    public RiftSwordItem(IItemTier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (hand != Hand.MAIN_HAND) {
            return ActionResult.pass(stack);
        }

        // Activation: Sneak + Right Click
        if (!player.isShiftKeyDown()) {
            return ActionResult.pass(stack);
        }

        if (world.isClientSide) {
            // Client-side particles & sounds are handled in event / packet
            return ActionResult.success(stack);
        }

        if (!(player instanceof ServerPlayerEntity)) {
            return ActionResult.pass(stack);
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        ServerWorld serverWorld = serverPlayer.getLevel();

        // Play sounds on server (synced)
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GLASS_BREAK, SoundCategory.PLAYERS, 1.0F, 0.8F + world.random.nextFloat() * 0.4F);
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PORTAL_TRIGGER, SoundCategory.PLAYERS, 0.7F, 0.9F);

        // Spawn directional particles (server -> client via vanilla packet)
        spawnRealityCutParticles(serverWorld, serverPlayer);

        // Check if already in a custom dimension -> return home
        if (ModDimensions.isCustomDimension(serverWorld.dimension())) {
            RiftTeleportHelper.returnToOrigin(serverPlayer);
            return ActionResult.success(stack);
        }

        // Spawn temporary vertical rift portal 1 block in front
        Vector3d look = player.getLookAngle();
        BlockPos portalPos = new BlockPos(
                player.getX() + look.x * 1.5,
                player.getY(),
                player.getZ() + look.z * 1.5
        );

        // Save origin before teleport
        PlayerRiftData.setOrigin(serverPlayer, serverWorld.dimension(), player.blockPosition());

        // Create temporary portal frame (2x3 vertical) and teleport after short delay or instantly on enter
        RiftTeleportHelper.createTemporaryRiftAndTeleport(serverPlayer, portalPos);

        return ActionResult.success(stack);
    }

    private void spawnRealityCutParticles(ServerWorld world, ServerPlayerEntity player) {
        Vector3d look = player.getLookAngle();
        double px = player.getX() + look.x;
        double py = player.getY() + player.getEyeHeight() * 0.7;
        double pz = player.getZ() + look.z;

        // Glass shatter burst
        for (int i = 0; i < 40; i++) {
            double ox = (world.random.nextDouble() - 0.5) * 1.5;
            double oy = (world.random.nextDouble() - 0.5) * 1.5;
            double oz = (world.random.nextDouble() - 0.5) * 1.5;
            world.sendParticles(net.minecraft.particles.ParticleTypes.CRIT,
                    px + ox, py + oy, pz + oz,
                    1, 0, 0, 0, 0.05);
        }

        // Transition to portal particles
        for (int i = 0; i < 30; i++) {
            double ox = (world.random.nextDouble() - 0.5) * 2.0;
            double oy = (world.random.nextDouble() - 0.5) * 2.0;
            double oz = (world.random.nextDouble() - 0.5) * 2.0;
            world.sendParticles(net.minecraft.particles.ParticleTypes.PORTAL,
                    px + ox, py + oy, pz + oz,
                    1, look.x * 0.1, look.y * 0.1, look.z * 0.1, 0.1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoil(ItemStack stack) {
        // Permanent enchantment glint for cosmic feel
        return true;
    }
}
