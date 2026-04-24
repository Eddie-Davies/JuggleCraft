# JuggleCraft Blocks And Behaviour Guide

This document describes the gameplay-facing changes currently implemented in the project, how each block works, how the blocks interact with each other, and the current limits of the system.

## Overview Of Recent Changes

The current JuggleCraft feature set now includes:

- A Coal Generator with a fuel slot and an on or off control.
- A Basic Battery with explicit charge states.
- A J-Box that can register and control up to 16 connected devices by IP.
- An RS485 Wire block that extends a local controller network.
- An FE to Redstone Transformer that reads FE from an attached device and outputs a proportional redstone signal.
- Right-click menu interfaces for the Coal Generator, Basic Battery, and J-Box.

At this stage, the design is local-network and IP-based.

- The J-Box can be placed freely and discovers devices through direct adjacency or connected RS485 wire paths.
- Each J-Box, Coal Generator, and Basic Battery has a configurable IP address in its menu.
- A J-Box can only control devices whose IP has been registered into one of its 16 device slots.
- The FE to Redstone Transformer must also be placed directly onto an FE-capable device.
- There is no remote routing system outside the local RS485-connected graph yet.

## Current Block List

The currently relevant gameplay blocks are:

- `J-Box`
- `Coal Generator`
- `Basic Battery`
- `RS485 Wire`
- `FE to Redstone Transformer`

All of them appear in the JuggleCraft creative tab.

## Coal Generator

### Purpose

The Coal Generator burns furnace fuel and pushes FE directly into adjacent FE receivers when enabled.

### Core Behaviour

- Internal capacity: `0 FE`
- Generation rate while burning: `40 FE/t`
- Maximum push rate: `40 FE/t`
- Fuel slot count: `1`
- Fuel type: any item accepted by Forge burn-time logic for smelting fuel

### How To Use It

1. Place the Coal Generator in the world.
2. Right-click it to open its menu.
3. Put coal or another valid fuel in the fuel slot.
4. Use the menu button to turn the generator on or off.

### Enabled And Disabled States

- `On`: the generator consumes fuel, produces `40 FE/t`, and immediately pushes that FE to adjacent FE receivers.
- `Off`: the generator does not burn fuel and does not generate or push FE.

If no adjacent receiver accepts FE while the generator is burning, that tick's generated FE is lost because the generator no longer buffers energy internally.

### Generator Menu

The generator menu currently provides:

- A fuel slot
- A burn indicator
- An FE bar and FE tooltip
- A button to toggle the generator on or off
- IP address controls

### Interaction With Other Blocks

- It can charge a `Basic Battery` placed adjacent to it, but only if the battery is in `Charging` mode.
- A `J-Box` mounted on the generator can toggle the generator on or off.
- An `FE to Redstone Transformer` mounted on the generator now reads live generation state instead of a stored FE buffer.

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
- IP address controls

### Interaction With Other Blocks

- If the battery is next to an enabled `Coal Generator`, it will only fill while set to `Charging`.
- If the battery is set to `Discharging`, it can supply adjacent FE receivers.
- A `J-Box` mounted on the battery can cycle the battery mode without opening the battery directly.
- An `FE to Redstone Transformer` mounted on the battery can output redstone strength based on how full the battery is.

## J-Box

### Purpose

The J-Box is now a local IP-based controller. It can manage up to 16 devices that are either directly adjacent to it or reachable through connected RS485 wire.

### Network Rule

The J-Box forms a local control network over:

- Directly adjacent controllable devices
- Chains of connected `RS485 Wire` blocks

Compatible controllable devices are currently:

- `Coal Generator`
- `Basic Battery`

Only devices that are physically reachable through that local graph and whose IP has been added to the J-Box can be controlled from the J-Box menu.

### IP Addressing

- Default J-Box IP: `10.172.152.1`
- Default generator IP: `192.168.1.1`
- Default battery IP: `192.168.1.1`

Because generators and batteries share the same default IP, you must usually change device IPs before adding multiple devices to one J-Box.

### What The J-Box Controls

The behaviour depends on the currently selected registered device slot.

#### Selected Basic Battery

The J-Box reads:

- Battery mode
- Stored FE
- FE capacity

Its control button cycles the battery through:

- `Idle`
- `Charging`
- `Discharging`

#### Selected Coal Generator

The J-Box reads:

- Generator on or off state
- Stored FE
- FE capacity

Its control button toggles:

- `Generator On`
- `Generator Off`

### J-Box Menu

The J-Box menu currently provides:

- The J-Box's own IP address controls
- A target IP editor for adding a device
- Previous and next slot buttons for up to `16` registered devices
- The selected slot's stored IP and online or offline status
- The selected device's current type, control state, FE, and capacity or output
- A context-sensitive control button for the selected online device
- An `Add Device` button that registers a reachable matching IP into the next free slot

### Important Limitation

The J-Box only controls devices that are both reachable on the local RS485 graph and explicitly registered by IP.

When multiple reachable devices share the same IP:

- The J-Box picks the device with the shortest direct or wire path first.
- If two matching devices are at the same distance, it falls back to the one placed earlier.

When multiple J-Boxes compete for the same reachable device IP:

- Control priority goes to the J-Box with the shortest path to that device.
- If two J-Boxes are at the same distance, it falls back to the one placed earlier.

It does not currently:

- control multiple devices at once with one button press
- rebalance energy between blocks
- prioritize one battery over another
- route FE through an abstract controller graph

## RS485 Wire

### Purpose

RS485 Wire extends the local J-Box control graph so the controller does not have to sit directly on top of a battery or generator.

### How It Works

- A J-Box scans through touching RS485 wire blocks.
- Any generator or battery touching that connected wire graph is considered reachable.
- Reachable devices can be added to the J-Box by entering their IP and pressing `Add Device`.
- The J-Box stores up to `16` device IP registrations.

### Practical Note

RS485 Wire handles control connectivity only. It does not transport FE.

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

- The transformer checks the attached device on a device-specific tick rate.
- It consumes energy over time from the attached device while outputting a signal.
- It outputs a redstone signal based on the attached device type.

Examples:

- `Basic Battery at 0% full` -> redstone `0`
- `Basic Battery at 50% full` -> roughly redstone `7`
- `Basic Battery at 100% full` -> redstone `15`
- `Coal Generator while actively burning` -> redstone `15`
- `Coal Generator while idle or disabled` -> redstone `0`

### Important Behaviour Notes

- The transformer now consumes FE over time while it is producing a non-zero redstone signal.
- On a `Basic Battery`, it updates every `5` ticks and drains `25 FE` per update, which is `100 FE/s`.
- On a `Coal Generator`, it updates every tick and drains the equivalent of `40 FE/t` by consuming the generator's remaining burn time faster.
- On FE storage blocks, the redstone level still reflects the device fill ratio.

That means:

- On a `Basic Battery`, the signal reflects how full the battery is, but keeping that signal active slowly drains the battery.
- On a `Coal Generator`, the signal reflects whether the generator is actively producing FE, and the attached transformer makes the fuel burn faster.

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

### Setup 3: Wired Battery Control With J-Box

1. Place a `Basic Battery`.
2. Change the battery's IP if needed.
3. Place a `J-Box` nearby.
4. Connect the J-Box to the battery directly or with `RS485 Wire`.
5. Enter the battery IP in the J-Box and add it.
6. Select the device slot and use the control button to cycle the battery mode.

Result:

- The J-Box can control that battery without being mounted directly on it.
- The selected slot shows the battery's current FE and mode when the battery is reachable.

### Setup 4: Multi-Device Generator Control With J-Box

1. Place a `Coal Generator`.
2. Optionally place `RS485 Wire` between the generator and the `J-Box`.
3. Set a unique generator IP if you plan to control more than one device.
4. Enter that IP in the J-Box and add it.
5. Use the slot selector to move between registered devices.
6. Use the control button to toggle the selected generator on or off.

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