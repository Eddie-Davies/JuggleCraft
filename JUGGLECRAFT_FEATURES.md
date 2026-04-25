# JuggleCraft Current Features and How They Work

This document describes what is currently implemented in the mod and where the code lives.

## At a Glance

JuggleCraft currently implements a small energy and control stack built around Forge Energy:

- A controller block (J-Box) that tracks and controls addressable devices on an RS485 wire network.
- Energy producer and storage machines (Coal Generator, Solar Panel, Basic Battery).
- An FE to Redstone transformer that converts attached device state into redstone output while consuming energy.
- Menus and custom screens for each major machine.
- One networking packet for J-Box IP configuration updates.
- Recipes, block models, blockstates, and base localization for the shipped blocks.

## Core Registration and Mod Bootstrap

Main entry and registration:

- `src/main/java/com/eddie/jugglecraft/JuggleCraft.java`
- `src/main/java/com/eddie/jugglecraft/registry/ModBlocks.java`
- `src/main/java/com/eddie/jugglecraft/registry/ModItems.java`
- `src/main/java/com/eddie/jugglecraft/registry/ModBlockEntities.java`
- `src/main/java/com/eddie/jugglecraft/registry/ModMenuTypes.java`
- `src/main/java/com/eddie/jugglecraft/registry/ModCreativeTabs.java`

Registered blocks/items:

- jbox
- coal_generator
- basic_battery
- solar_panel
- fe_to_redstone_transformer
- rs485_wire

## Energy Foundation

Shared FE behavior:

- `src/main/java/com/eddie/jugglecraft/block/entity/AbstractEnergyBlockEntity.java`
- `src/main/java/com/eddie/jugglecraft/energy/ModEnergyStorage.java`
- `src/main/java/com/eddie/jugglecraft/energy/EnergyTransferHelper.java`

How it works:

- `AbstractEnergyBlockEntity` provides a persistent FE capability and NBT save/load of stored energy.
- `ModEnergyStorage` wraps `EnergyStorage` and triggers `onEnergyChanged` hooks so block entities mark themselves dirty.
- `EnergyTransferHelper` pushes FE to adjacent blocks that expose `ForgeCapabilities.ENERGY` and can receive.

## Machine Features

## Coal Generator

Code:

- Block: `src/main/java/com/eddie/jugglecraft/block/custom/CoalGeneratorBlock.java`
- Block entity: `src/main/java/com/eddie/jugglecraft/block/entity/CoalGeneratorBlockEntity.java`
- Menu/screen: `src/main/java/com/eddie/jugglecraft/menu/CoalGeneratorMenu.java`, `src/main/java/com/eddie/jugglecraft/client/screen/CoalGeneratorScreen.java`

Behavior:

- Has one fuel slot using Forge burn time lookup.
- Burns valid fuel and outputs 40 FE/t while burning.
- Pushes generated FE to neighboring energy handlers.
- Can be toggled on or off.
- Supports an editable IP address used by J-Box discovery/control.

## Basic Battery

Code:

- Block: `src/main/java/com/eddie/jugglecraft/block/custom/BasicBatteryBlock.java`
- Block entity: `src/main/java/com/eddie/jugglecraft/block/entity/BasicBatteryBlockEntity.java`
- Menu/screen: `src/main/java/com/eddie/jugglecraft/menu/BasicBatteryMenu.java`, `src/main/java/com/eddie/jugglecraft/client/screen/BasicBatteryScreen.java`

Behavior:

- Capacity: 100000 FE.
- Max receive/extract: 200 FE/t.
- Three charge modes:
  - CHARGING: accepts FE, no extraction.
  - IDLE: neither receive nor extract through exposed capability.
  - DISCHARGING: extracts FE and pushes to neighbors.
- Tracks reported power flow for controller status.
- Supports editable IP address.

## Solar Panel

Code:

- Block: `src/main/java/com/eddie/jugglecraft/block/custom/SolarPanelBlock.java`
- Block entity: `src/main/java/com/eddie/jugglecraft/block/entity/SolarPanelBlockEntity.java`
- Menu/screen: `src/main/java/com/eddie/jugglecraft/menu/SolarPanelMenu.java`, `src/main/java/com/eddie/jugglecraft/client/screen/SolarPanelScreen.java`

Behavior:

- Capacity: 12000 FE.
- Max generation: 120 FE/t.
- Generation is dynamically calculated from:
  - day time sun factor
  - weather factor
  - sky visibility and sky light
  - height factor relative to sea level and build height
- Stores generated FE and pushes to neighbors.
- Can be enabled or disabled.
- Supports editable IP address.

## J-Box Controller and RS485 Network

Code:

- Block: `src/main/java/com/eddie/jugglecraft/block/custom/JboxBlock.java`
- Block entity: `src/main/java/com/eddie/jugglecraft/block/entity/JboxBlockEntity.java`
- Menu/screen: `src/main/java/com/eddie/jugglecraft/menu/JboxMenu.java`, `src/main/java/com/eddie/jugglecraft/client/screen/JboxScreen.java`
- Network helper: `src/main/java/com/eddie/jugglecraft/logic/Rs485NetworkHelper.java`
- Addressing: `src/main/java/com/eddie/jugglecraft/logic/DeviceAddress.java`, `src/main/java/com/eddie/jugglecraft/logic/IpAddressableDevice.java`

Behavior:

- J-Box maintains up to 16 configured device slots.
- Each slot stores a device IP and a snapshot of current status:
  - device type
  - online/offline
  - control state
  - power flow
  - stored energy/capacity
  - extra metrics (fuel, efficiency factors)
- Every scan interval it refreshes slot snapshots by resolving devices over connected RS485 wire graph.
- Device control actions from J-Box:
  - battery: cycle charge mode
  - coal generator: toggle enable
  - solar panel: toggle enable
- Includes local controller IP and pending target IP editing in UI.

RS485 resolution details:

- BFS-style search over connected RS485 wire blocks.
- Candidate selection prefers:
  - shortest network distance
  - earlier placement order
  - lower block position long value as final tiebreak
- Limits traversal to a bounded node count for safety.

## FE to Redstone Transformer

Code:

- Block: `src/main/java/com/eddie/jugglecraft/block/custom/FeToRedstoneTransformerBlock.java`
- Block entity: `src/main/java/com/eddie/jugglecraft/block/entity/FeToRedstoneTransformerBlockEntity.java`
- Mounted lookup: `src/main/java/com/eddie/jugglecraft/util/MountedDeviceHelper.java`

Behavior:

- Must be placed attached to an energy-capable block.
- Periodically computes redstone output from attached source:
  - coal generator: binary style output based on generation state
  - energy storage devices: scaled from FE fill ratio to 0..15 signal
- Consumes energy from attached source to sustain updates.
- Emits direct and indirect redstone signal using its `POWER` blockstate.

## Menus, Screens, and Client Wiring

Code:

- Client menu registration: `src/main/java/com/eddie/jugglecraft/client/ClientModEvents.java`
- Menus: `src/main/java/com/eddie/jugglecraft/menu`
- Screens: `src/main/java/com/eddie/jugglecraft/client/screen`

Implemented UI capabilities:

- Basic machine status bars and labels.
- Button-based control for enable/disable and battery mode cycling.
- IP octet increment/decrement controls on machine screens.
- J-Box card UI with scrolling through device slots, online state, and slot-level control buttons.
- Editable text IP fields on J-Box screen.

## Network Packets

Code:

- Channel bootstrap: `src/main/java/com/eddie/jugglecraft/network/ModMessages.java`
- Packet: `src/main/java/com/eddie/jugglecraft/network/JboxSetIpPacket.java`

Current packet functionality:

- `JboxSetIpPacket` sends J-Box local IP or slot IP updates from client UI to server.
- Server validates sender/menu/block position before applying update.

## Data and Content Assets

Resources:

- Blockstates/models/textures/lang under `src/main/resources/assets/jugglecraft`
- Recipes under `src/main/resources/data/jugglecraft/recipes`
- Loot tables under `src/main/resources/data/jugglecraft/loot_tables/blocks`

Current recipes exist for:

- jbox
- coal_generator
- basic_battery
- solar_panel
- fe_to_redstone_transformer
- rs485_wire

Localization file:

- `src/main/resources/assets/jugglecraft/lang/en_us.json`

## Notes on Current Coverage

Observed from current resources:

- Loot tables currently exist for jbox, coal_generator, basic_battery, and fe_to_redstone_transformer.
- `mineable/pickaxe` currently lists jbox, coal_generator, basic_battery, and fe_to_redstone_transformer.

If additional behavior is added, update this file and include the exact class paths where that behavior is implemented.
