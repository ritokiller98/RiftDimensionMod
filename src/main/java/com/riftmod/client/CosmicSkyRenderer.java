package com.riftmod.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.ISkyRenderHandler;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lightweight client-side sky renderer for The Interworld.
 * - Pure black void
 * - Distant twinkling stars
 * - Occasional huge cosmic worm / monster silhouettes that fly past far away
 * Optimized: no heavy entity logic, pure vertex rendering, limited particle count.
 */
public class CosmicSkyRenderer implements ISkyRenderHandler {

    private static final int STAR_COUNT = 800;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starZ = new float[STAR_COUNT];
    private final float[] starBright = new float[STAR_COUNT];

    private final List<CosmicMonster> monsters = new ArrayList<>();
    private long lastMonsterSpawn = 0;
    private final Random rand = new Random();

    public CosmicSkyRenderer() {
        // Pre-generate stars once
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = (rand.nextFloat() * 2f - 1f) * 100f;
            starY[i] = (rand.nextFloat() * 2f - 1f) * 100f;
            starZ[i] = (rand.nextFloat() * 2f - 1f) * 100f;
            starBright[i] = 0.3f + rand.nextFloat() * 0.7f;
        }
    }

    @Override
    public void render(int ticks, float partialTicks, MatrixStack matrixStack, ClientWorld world, Minecraft mc) {
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Pure black clear is already done by dimension; we just draw stars + monsters

        // Draw stars
        drawStars(matrixStack, ticks + partialTicks);

        // Cosmic monsters (rare, far away, client-only)
        updateAndDrawMonsters(matrixStack, ticks + partialTicks, mc);

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private void drawStars(MatrixStack ms, float time) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        Matrix4f mat = ms.last().pose();

        buf.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < STAR_COUNT; i++) {
            float twinkle = 0.5f + 0.5f * (float) Math.sin(time * 0.05f + i);
            float a = starBright[i] * twinkle * 0.9f;
            buf.vertex(mat, starX[i], starY[i], starZ[i])
                    .color(0.7f, 0.6f, 1.0f, a)
                    .endVertex();
        }
        tess.end();
    }

    private void updateAndDrawMonsters(MatrixStack ms, float time, Minecraft mc) {
        long now = System.currentTimeMillis();

        // Spawn new monster rarely (every 25-60 seconds)
        if (now - lastMonsterSpawn > 25000 + rand.nextInt(35000)) {
            monsters.add(new CosmicMonster(rand));
            lastMonsterSpawn = now;
            // Keep list small
            if (monsters.size() > 3) {
                monsters.remove(0);
            }
        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        Matrix4f mat = ms.last().pose();

        List<CosmicMonster> toRemove = new ArrayList<>();
        for (CosmicMonster m : monsters) {
            m.update(time);
            if (m.isDead()) {
                toRemove.add(m);
                continue;
            }
            // Draw simple elongated silhouette (worm / tentacle shape)
            drawMonsterSilhouette(buf, mat, m);
        }
        monsters.removeAll(toRemove);

        if (!monsters.isEmpty()) {
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            // re-draw all (simple)
            for (CosmicMonster m : monsters) {
                drawMonsterSilhouette(buf, mat, m);
            }
            tess.end();
        }
    }

    private void drawMonsterSilhouette(BufferBuilder buf, Matrix4f mat, CosmicMonster m) {
        // Very simple dark purple elongated quad far in the sky
        float x = m.x;
        float y = m.y;
        float z = m.z;
        float len = m.length;
        float w = m.width;

        float r = 0.08f, g = 0.0f, b = 0.15f, a = m.alpha;

        // Main body
        buf.vertex(mat, x - w, y, z).color(r, g, b, a).endVertex();
        buf.vertex(mat, x + w, y, z).color(r, g, b, a).endVertex();
        buf.vertex(mat, x + w * 0.6f, y + len, z).color(r, g, b, a * 0.3f).endVertex();
        buf.vertex(mat, x - w * 0.6f, y + len, z).color(r, g, b, a * 0.3f).endVertex();
    }

    private static class CosmicMonster {
        float x, y, z;
        float length, width;
        float speed;
        float alpha = 0.7f;
        boolean goingUp;
        float life;

        CosmicMonster(Random rand) {
            // Spawn far away on the side of the sky
            x = (rand.nextFloat() * 2f - 1f) * 80f;
            z = (rand.nextFloat() * 2f - 1f) * 80f;
            goingUp = rand.nextBoolean();
            y = goingUp ? -90f : 90f;
            length = 25f + rand.nextFloat() * 40f;
            width = 3f + rand.nextFloat() * 6f;
            speed = 0.15f + rand.nextFloat() * 0.25f;
            life = 0f;
        }

        void update(float time) {
            life += 0.016f;
            if (goingUp) {
                y += speed;
            } else {
                y -= speed;
            }
            // Fade in/out
            if (life < 1.5f) alpha = life / 1.5f * 0.75f;
            else if (life > 8f) alpha = Math.max(0f, 0.75f - (life - 8f) * 0.3f);
        }

        boolean isDead() {
            return (goingUp && y > 100f) || (!goingUp && y < -100f) || alpha <= 0f;
        }
    }
              }
