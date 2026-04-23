# JuggleCraft Blocks And Behaviour Guide

This document describes the gameplay-facing changes currently implemented in the project, how each block works, how the blocks interact with each other, and the current limits of the system.

## Overview Of Recent Changes

The current JuggleCraft feature set now includes:

- A Coal Generator with a fuel slot and an on or off control.
- A Basic Battery with explicit charge states.
- A J-Box that acts as a mounted controller for one attached device.
- An FE to Redstone Transformer that reads FE from an attached device and outputs a proportional redstone signal.
- Right-click menu interfaces for the Coal Generator, Basic Battery, and J-Box.

At this stage, the design is device-local and attachment-based.

- The J-Box must be placed directly onto a compatible device.
- The FE to Redstone Transformer must also be placed directly onto an FE-capable device.
- There is no cable network, remote routing system, or multi-device controller graph yet.

## Current Block List

The currently relevant gameplay blocks are:

- `J-Box`
- `Coal Generator`
- `Basic Battery`
- `FE to Redstone Transformer`

All of them appear in the JuggleCraft creative tab.

## Coal Generator

### Purpose

The Coal Generator burns furnace fuel, stores FE internally, and pushes that FE into adjacent FE receivers when enabled.

### Core Behaviour

- Internal capacity: `16000 FE`
- Generation rate while burning: `40 FE/t`
- Maximum push rate: `80 FE/t`
- Fuel slot count: `1`
- Fuel type: any item accepted by Forge burn-time logic for smelting fuel

### How To Use It

1. Place the Coal Generator in the world.
2. Right-click it to open its menu.
3. Put coal or another valid fuel in the fuel slot.
4. Use the menu button to turn the generator on or off.

### Enabled And Disabled States

- `On`: the generator burns fuel, produces FE, and pushes FE to adjacent FE receivers.
- `Off`: the generator does not burn fuel, does not generate FE, and does not push FE out.

If the generator is turned off while it already contains stored FE, that energy remains stored until the block is turned on again or another future mechanic consumes it.

### Generator Menu

The generator menu currently provides:

- A fuel slot
- A burn indicator
- An FE bar and FE tooltip
- A button to toggle the generator on or off

### Interaction With Other Blocks

- It can charge a `Basic Battery` placed adjacent to it, but only if the battery is in `Charging` mode.
- A `J-Box` mounted on the generator can toggle the generator on or off.
- An `FE to Redstone Transformer` mounted on the generator can output redstone strength based on the generator's current FE buffer.

## Basic Battery

### Purpose

The Basic Battery stores FE and now has an explicit operating mode that controls whether it can receive, hold, or output energy.

### Core Behaviour

- Internal capacity: `100000 FE`
- Maximum transfer rate: `200 FE/t`

### Battery Modes

The battery supports three modes:

#### `Charging`

- The battery accepts FE input.
- The battery does not allow FE extraction through its exposed capability.
- This is the default mode when the block is created.

Use this mode when the battery is meant to fill from a generator or another FE source.

#### `Idle`

- The battery does not accept FE input.
- The battery does not output FE.
- The battery acts as isolated stored energy.

Use this mode when you want stored FE preserved without charging or discharging.

#### `Discharging`

- The battery does not accept FE input.
- The battery allows FE extraction.
- The battery actively pushes FE to adjacent FE receivers each server tick cycle, up to its transfer limit.

Use this mode when the battery should behave like an active power source for nearby FE consumers.

### How To Use It

1. Place the Basic Battery in the world.
2. Right-click it to open its menu.
3. Use the mode button to cycle through `Charging`, `Idle`, and `Discharging`.

### Battery Menu

The battery menu currently provides:

- An FE bar and FE tooltip
- A text readout of stored FE and total capacity
- A button that cycles the current battery mode

### Interaction With Other Blocks

- If the battery is next to an enabled `Coal Generator`, it will only fill while set to `Charging`.
- If the battery is set to `Discharging`, it can supply adjacent FE receivers.
- A `J-Box` mounted on the battery can cycle the battery mode without opening the battery directly.
- An `FE to Redstone Transformer` mounted on the battery can output redstone strength based on how full the battery is.

## J-Box

### Purpose

The J-Box is now an attached single-device controller. It does not act as a general nearby network scanner anymore. It reads and controls exactly one device mounted behind it.

### Attachment Rule

The J-Box must be placed directly on a compatible target block.

Compatible targets are currently:

- `Coal Generator`
- `Basic Battery`

If there is no compatible target behind the face it is trying to attach to:

- Placement is rejected.
- If the supporting device is removed later, the J-Box drops because it can no longer survive there.

### What The J-Box Controls

The behaviour depends on what it is mounted on.

#### Mounted On A Basic Battery

The J-Box reads:

- Battery mode
- Stored FE
- FE capacity

Its control button cycles the battery through:

- `Idle`
- `Charging`
- `Discharging`

#### Mounted On A Coal Generator

The J-Box reads:

- Generator on or off state
- Stored FE
- FE capacity

Its control button toggles:

- `Generator On`
- `Generator Off`

### J-Box Menu

The J-Box menu currently provides:

- The type of attached device
- The current control state of that device
- The current FE stored in the attached device
- The current FE capacity of the attached device
- A single context-sensitive control button

### Important Limitation

The J-Box only controls the one block it is mounted on.

It does not currently:

- control multiple devices at once
- scan a local FE network
- rebalance energy between blocks
- prioritize one battery over another
- route FE through an abstract controller graph

## FE To Redstone Transformer

### Purpose

The FE to Redstone Transformer is a mounted read-only block that converts FE storage in an attached device into a redstone signal.

### Attachment Rule

The transformer must be placed directly on an FE-capable block.

Currently, valid practical targets include:

- `Coal Generator`
- `Basic Battery`

If the attached block is removed or is not FE-capable, the transformer cannot remain mounted and will break from that position.

### How It Works

- The transformer checks the FE stored in the attached device.
- It compares the current FE to the device's maximum FE capacity.
- It outputs a redstone signal from `0` to `15` based on that fill ratio.

Examples:

- `0% full` -> redstone `0`
- `50% full` -> roughly redstone `7`
- `100% full` -> redstone `15`

### Important Behaviour Notes

- The transformer does not consume FE.
- It is a sensor, not a converter that burns energy into redstone.
- It reads stored FE from the attached device's energy capability.

That means:

- On a `Basic Battery`, the signal reflects how full the battery is.
- On a `Coal Generator`, the signal reflects how much FE is currently stored inside the generator buffer.

## Recipes

The project currently includes recipes for:

- `J-Box`
- `Coal Generator`
- `Basic Battery`
- `FE to Redstone Transformer`

The FE to Redstone Transformer recipe is:

- Pattern:

```text
IRI
RQR
IRI
```

- Ingredients:
  - `I` = Iron Ingot
  - `R` = Redstone
  - `Q` = Quartz

## Practical Interaction Patterns

### Setup 1: Simple Charging Loop

1. Place a `Coal Generator`.
2. Place a `Basic Battery` adjacent to it.
3. Set the battery to `Charging`.
4. Fuel and enable the generator.

Result:

- The generator burns fuel.
- The generator stores FE.
- The generator pushes FE into the battery.
- The battery fills while in `Charging` mode.

### Setup 2: Controlled Battery Source

1. Charge a `Basic Battery`.
2. Change the battery mode to `Discharging`.
3. Place FE-consuming blocks adjacent to the battery in future expansions.

Result:

- The battery actively pushes FE outward up to its transfer limit.

### Setup 3: Mounted Battery Control With J-Box

1. Place a `Basic Battery`.
2. Place a `J-Box` directly on the battery face.
3. Right-click the J-Box.
4. Use the button to cycle the battery mode.

Result:

- The J-Box acts as a controller for that battery only.
- The J-Box menu shows the battery's current FE and mode.

### Setup 4: Mounted Generator Control With J-Box

1. Place a `Coal Generator`.
2. Place a `J-Box` directly on the generator face.
3. Right-click the J-Box.
4. Use the button to toggle the generator on or off.

Result:

- The J-Box acts as a controller for that generator only.

### Setup 5: Redstone Meter For FE Storage

1. Place a `Basic Battery` or `Coal Generator`.
2. Place an `FE to Redstone Transformer` directly on the target block.
3. Attach vanilla redstone dust, lamps, comparators, or contraptions around the transformer.

Result:

- The transformer outputs stronger redstone as FE stored in the attached block increases.

## Behaviour Summary By Block

### Coal Generator Summary

- Burns fuel when enabled
- Stores FE internally
- Pushes FE to adjacent receivers
- Can be toggled directly or through an attached J-Box

### Basic Battery Summary

- Stores FE internally
- `Charging` means receive only
- `Idle` means hold only
- `Discharging` means output only
- Can be controlled directly or through an attached J-Box

### J-Box Summary

- Must mount on a battery or generator
- Controls only one attached device
- Shows attached device state and FE buffer

### FE To Redstone Transformer Summary

- Must mount on an FE-capable block
- Reads FE fill ratio
- Outputs redstone signal `0-15`
- Does not consume FE

## Known Limitations And Current Design Boundaries

These behaviours are intentionally limited right now:

- The J-Box is not yet a multi-block network controller.
- Battery mode is local to that battery only.
- Generator control is local to that generator only.
- The transformer senses FE level only; it does not control power flow.
- There are no cables, menus for multiple attached targets, or packet-based remote device graphs.
- The current FE logic is adjacency-based.

## Testing Notes

To test these systems in game:

1. Ensure Java 17 is installed and your `JAVA_HOME` points to the correct JDK.
2. From the project root, run:

```powershell
.\gradlew.bat runClient
```

3. Open a Creative world.
4. Pull the blocks from the JuggleCraft creative tab.
5. Build the interaction setups described above.

If Gradle fails before launch, fix the local JDK path first. At the time these changes were documented, the current environment was blocked by an invalid `JAVA_HOME` path rather than by editor-detected source errors.

## Recommended First In-Game Scenarios

If you want a quick functional smoke test, start with these three scenarios:

### Scenario A: Generator To Battery

1. Place a generator and battery side by side.
2. Set the battery to `Charging`.
3. Fuel the generator and turn it on.
4. Confirm the battery FE rises.

### Scenario B: J-Box Mounted On Battery

1. Mount the J-Box on the battery.
2. Open the J-Box.
3. Cycle battery mode.
4. Confirm the battery menu shows the same mode when opened directly.

### Scenario C: Transformer Output

1. Mount the transformer on the battery.
2. Charge the battery.
3. Observe redstone strength increase as battery FE increases.

## Future Expansion Points

The current implementation leaves obvious extension paths:

- J-Box controlling more than one attached device
- grouped local controller networks
- more detailed battery routing policies
- transformer variants with configurable thresholds
- UI polish and textures specific to each machine

For now, this document should be treated as the authoritative description of the currently implemented device interactions.