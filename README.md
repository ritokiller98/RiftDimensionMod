# The Rift Dimension Mod
**Minecraft Java 1.16.5 — Forge 36.2.39**

## Features
- **Rift Sword** — Shift + Right Click activates "Reality Cut"
  - Glass shatter + portal sounds
  - Directional particles
  - Spawns temporary vertical rift (2×3) 1 block in front
  - Teleports to **The Interworld**
- **The Interworld**
  - Absolute black void + dense cosmic purple fog
  - Distant twinkling stars
  - Client-side huge cosmic worm/monster silhouettes flying in the distance
  - World border 10 000 × 10 000
  - Central TON-618 inspired black hole (jump in → Black Hole Core)
  - Scattered unique rifts with HUD tooltips (coordinates + dimension)
- **Black Hole Core**
  - Ultra-dense 50×4 room
  - Beds work (spawn point can be set)
- **Return Rule**
  - Using Rift Sword inside Interworld or Core opens extraction portal back to exact original coordinates & dimension

## How to build & run

1. Install **JDK 8** (required for 1.16.5)
2. Open terminal in this folder
3. Run:
   ```
   ./gradlew genEclipseRuns
   ./gradlew runClient
   ```
   or for IDEA:
   ```
   ./gradlew genIntellijRuns
   ```

4. Put a texture file at:
   `src/main/resources/assets/riftdimension/textures/item/rift_sword.png`
   (recommended size 16×16 or 32×32).  
   For the animated End-Portal-like look you can use any purple/magenta animated texture or create one with Blockbench / GIMP.

## Important notes
- Dimensions are registered via datapack JSONs (standard 1.16.5 way).
- Fog & sky are fully client-side and optimized.
- Cosmic monsters are pure visual (no entities, no lag).
- All teleports are null-safe.
- Code is written for maximum stability on Forge 1.16.5.

Enjoy the cosmic horror.
— RiftMod
