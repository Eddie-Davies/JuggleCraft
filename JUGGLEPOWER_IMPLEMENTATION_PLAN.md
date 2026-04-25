# JugglePower Implementation Plan

## Goal

Create a separate power-focused mod layer named JugglePower that lives in this repository now, starts with strict tiered electricity foundations, and can integrate with J-Box later without refactoring.

## Scope Decisions

- Mod boundary: separate mod id in same project (staged separation).
- Mod id: jugglepower.
- Base energy unit: FE internally.
- Tiers: strict HV, LV, DC, and Signal behavior.
- First scope includes:
  - Tiered cable core
  - Battery cell
  - PV cell
  - Wind turbine
  - Steam chain (boiler + furnace steam path + steam turbine)
  - Electric furnace
  - MOSFET gate
- J-Box integration: deferred, add seams later.

## Phase Plan

1. Mod boundary and bootstrap
   - Add second mod declaration in mods metadata.
   - Add separate bootstrap class and isolated registries.

2. Electrical model core
   - Add tier enum and strict transfer rules.
   - Add tiered block entity base with FE capability and NBT persistence.

3. Cable primitives
   - Add LV/HV/DC cable blocks.
   - Add Signal cable block for enhanced signal transport.
   - Add strict mismatch fault handling and reset flow.

4. Generation and storage baseline
   - Add Battery Cell (DC storage).
   - Add PV Cell (DC generation).
   - Add Wind Turbine (HV generation).

5. Steam transport and generation
   - Add internal steam-unit transport and steam pipe network.
   - Add Boiler and furnace-based steam source path.
   - Add Steam Turbine (HV output).

6. Power consumers and gating
   - Add Electric Furnace (LV, no fuel).
   - Add MOSFET binary gate controlled by redstone/signal.

7. Content and UX pass
   - Add recipes, loot tables, mining tags, lang keys, blockstates, models.
   - Add menus/screens where needed.

8. Stabilization
   - Tune transfer rates, capacities, and conversion ratios.
   - Validate mismatch and fault behavior.

## Verification

- Compile gate after each implementation slice: ./gradlew compileJava
- Runtime validation: ./gradlew runClient
- Confirm each new block has: recipe, loot, mining tag, localization, model, blockstate.
- Confirm strict tier mismatch behavior and recovery path.
