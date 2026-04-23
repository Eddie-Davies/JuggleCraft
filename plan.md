# Juggle Energy Implementation Plan

Build Juggle Energy in aggressive phases on top of Forge Energy, starting with a working controller-generator-battery loop and then extending that same foundation into renewable generation, priority routing, redstone costing, PLC logic, and remote 4G control.

## Phases

1. Foundation cleanup
   Align project metadata so packaging and generated resources consistently use the `jugglecraft` identity. Replace remaining template Gradle properties, decide whether the current sample `Config` class is removed or repurposed, and add missing registries for block entities, menus, and networking.

2. Registry scaffolding
   Extend the existing registration pattern used by blocks, items, and creative tabs to add block entities, menus, and packet bootstrap code.

3. Core energy abstractions
   Implement Forge Energy backed storage and transfer helpers in the `energy` package, including reusable storage wrappers, sided capability exposure, transfer helpers, and NBT serialization.

4. First playable blocks
   Convert Jbox into a controller host with a block entity, then add a coal generator and a basic battery with ticking logic, persistence, and block drops.

5. First network behavior
   Implement local energy transfer between adjacent FE blocks and a minimal Jbox controller model that can observe connected generator and battery state, track stored energy totals, and expose basic policy flags.

6. Player interaction
   Add menus and screens for Jbox, generator, and battery UIs, with server-authoritative sync for energy, burn time, charge state, and control toggles.

7. Content usability
   Add recipes, loot, lang keys, creative tab entries, and baseline assets. Introduce data generators once the block and item set is stable enough to reduce JSON churn.

8. Progression expansion
   Add solar power, wind turbine generation, advanced batteries, and industrial batteries using the same shared FE base classes and UI patterns.

9. Controller logic expansion
   Extend Jbox into routed power management with source selection, battery charging preference, load shedding, low-power warnings, and manual system enable or disable controls.

10. Redstone bridge
    Introduce FE-powered redstone integration and bounded dynamic circuit-cost estimation, starting with nearby connected-component scanning and weighted component costs rather than a full-world graph solver.

11. PLC layer
    Add PLC blocks or logic modules tied to Jbox state triggers such as low battery threshold, reserve mode, source switching, and non-essential shutdown.

12. Remote connectivity
    Add SIM card items, an online Jbox variant or upgrade path, a handheld smart controller, and 4G towers with FE power requirements and range checks.

13. Network operations rules
    Add remote action energy costs, offline handling when towers lose power, and controller readouts for communication state.

14. Balancing and polish
    Tune FE capacities, burn values, generator outputs, transfer rates, and redstone circuit weights. Add warning states, texture polish, and any missing recipes or loot paths.

## File Targets

- `gradle.properties`
- `build.gradle`
- `src/main/resources/META-INF/mods.toml`
- `src/main/java/com/eddie/jugglecraft/JuggleCraft.java`
- `src/main/java/com/eddie/jugglecraft/registry`
- `src/main/java/com/eddie/jugglecraft/block`
- `src/main/java/com/eddie/jugglecraft/energy`
- `src/main/java/com/eddie/jugglecraft/logic`
- `src/main/java/com/eddie/jugglecraft/network`
- `src/main/java/com/eddie/jugglecraft/client`
- `src/main/resources/assets/jugglecraft`
- `src/generated/resources`

## Validation

1. Run `gradlew.bat compileJava` after each registry or block entity slice.
2. Smoke test placement, ticking, persistence, and FE transfer in a client run after the core loop is in place.
3. Run `gradlew.bat runData` once datagen providers are added.
4. Run `gradlew.bat build` before packaging or sharing the mod.

## Current Status

- Metadata has been aligned with the `jugglecraft` mod identity.
- Jbox has been converted into a block entity host.
- Coal Generator and Basic Battery blocks are implemented and registered.
- Shared Forge Energy storage and neighbor transfer helpers are in place.
- Basic assets, loot tables, recipes, and mining tags exist for the first core blocks.