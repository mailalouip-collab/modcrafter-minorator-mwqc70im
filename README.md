# Minorator

A Minecraft Java 1.21.1 mod for NeoForge 21.1.172 or newer compatible 21.1 releases. Requires Java 21. Install the mod on both the client and server.

## Building

Use JDK 21 and Gradle 8.10.2:

    gradle build

The mod JAR is written to `build/libs/minorator-1.0.0.jar`.

Development commands:

    gradle runClient
    gradle runServer

A Gradle wrapper binary is not included. Optionally generate one with an installed Gradle:

    gradle wrapper --gradle-version 8.10.2

## Crafting

    I R I
    C F C
    I H I

I = iron ingot, R = redstone dust, C = coal, F = furnace, H = hopper.

The recipe unlocks when you obtain a furnace. The block is also in the Functional Blocks creative tab.

## Usage

1. Place the Minorator directly above a hopper. Point the hopper into a chest.
2. Right-click the Minorator with coal or charcoal to insert as much of the held stack as fits. Alternatively, feed fuel with a hopper above it or pointing into a side.
3. One resource is generated every 400 game ticks: 20 seconds at normal server speed. One coal or charcoal powers 1,600 ticks, or four cycles.
4. The bottom hopper extracts only generated resources, never the fuel supply.

Production chances per cycle:

- Iron ingot: 60%
- Coal: 20%
- Gold ingot: 18%
- Diamond: 2%

Generated coal is not automatically used as fuel. On average, four cycles return 0.8 coal, so generated coal alone does not reliably sustain the machine.

There is one 64-item fuel slot and four dedicated 64-item output slots. Without a hopper, resources remain safely buffered inside the block rather than becoming loose item entities. Empty-hand right-click retrieves the first nonempty output stack; repeat to collect the others. Sneak-right-click with an empty hand retrieves unconsumed fuel. When no output is available, empty-hand right-click displays the current fuel and cycle status.

The machine pauses without consuming fuel when every output slot is full. If only some slots are full, a roll targeting a full slot is lost; the machine never increases diamond odds by rerolling blocked results. Keep the bottom hopper draining into storage.

The Minorator emits light while running. Mine it with a stone-tier or better pickaxe to recover the block; breaking it also releases stored inventory. Burn time and partial cycle progress survive saving and reloading but are not retained when the block is broken. Production requires a ticking chunk and does not run while the world is offline.

All models reuse vanilla textures, so no external texture files are required.
