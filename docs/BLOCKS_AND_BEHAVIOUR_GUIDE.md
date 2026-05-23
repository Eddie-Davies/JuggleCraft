
# JuggleCraft Blocks And Behaviour Guide

This guide documents what is currently implemented in the mod, how each block behaves in-game, and what is planned next.

Last updated: 2026-05-23

## Quick Status

Implemented blocks currently in the JuggleCraft creative tab:

- J-Box
- Coal Generator
- Basic Battery
- Solar Panel
- RS485 Wire
- RS485 Terminal
- FE to Redstone Transformer

## Size Convention

All block descriptions in this guide should describe size by footprint in blocks.

- Use the format `width x height x depth` in block units.
- If a block is surface-mounted or visually thin, still describe its footprint as the block space it occupies.
- Example: `1 x 1 x 1 block footprint`.
- Example: `1 x 1 x 1 block footprint, surface-mounted`.

This is the standard to use for future oversized blocks, multiblocks, and cabinet-style systems.

## System Overview (Current)

JuggleCraft currently runs on a local RS485 control network and Forge Energy transfer.

- FE transfer is adjacency-based between FE-capable blocks.
- Device control is IP-based via J-Box registrations.
- RS485 network pathfinding is local and graph-limited.
- RS485 currently transports control reachability, not FE.

### Local RS485 Control Graph

- J-Box scans through connected RS485 Wire nodes and directly adjacent positions.
- Reachable controllable devices are:
  - Coal Generator
  - Basic Battery
  - Solar Panel
- Search is breadth-first and capped to 256 visited nodes.
- If multiple candidates share an IP, closest path wins; if tied, earlier placement order wins.
- If multiple J-Boxes can reach the same device/IP, shortest-path J-Box wins (then placement order tie-break).

## Block Behaviours

## J-Box

### Purpose

Local control hub for IP-registered devices on the RS485-reachable graph.

### Size

- 1 x 1 x 1 block footprint, surface-mounted

### Core Behaviour

- Max registered device slots: 16
- Scan interval: every 10 ticks
- Default J-Box IP: 10.172.152.1
- Stores per-slot snapshots (type, online status, control state, flow/energy/capacity metrics)

### What It Can Control

Per selected registered slot, J-Box can issue one action:

- Basic Battery: cycle mode (Charging -> Idle -> Discharging)
- Coal Generator: toggle enabled/disabled
- Solar Panel: toggle enabled/disabled

### Menu Capabilities

- Edit J-Box IP
- Edit pending target IP
- Add pending IP to next free slot (if reachable and not already registered)
- Remove selected slot registration
- Next/previous slot navigation
- View online/offline and device telemetry for selected slot

### Notes

- Registration is by IP, but control still requires live RS485/local reachability.
- Slot snapshots refresh on server tick scans.

## Coal Generator

### Purpose

Burns furnace fuel and pushes FE directly to adjacent FE receivers while enabled.

### Size

- 1 x 1 x 1 block footprint

### Core Behaviour

- Generation rate: 40 FE/t while burning
- Max output push: 40 FE/t
- Internal FE capacity: 0 FE (no FE buffer)
- Fuel slots: 1
- Default device IP: 192.168.1.1
- Starts enabled by default

### Practical Effects

- If no neighbor accepts FE on a tick, that generated FE is effectively lost.
- Mounted transformer interaction can consume burn time faster.

## Basic Battery

### Purpose

Stores FE and exposes mode-gated receive/extract behavior.

### Size

- 1 x 1 x 1 block footprint

### Core Behaviour

- Capacity: 100,000 FE
- Max receive/extract transfer: 200 FE/t
- Default mode: Charging
- Default device IP: 192.168.1.1

### Modes

- Charging: receive only
- Idle: no receive, no extract
- Discharging: extract only and actively pushes to adjacent receivers

### Practical Effects

- External FE capability obeys the selected mode.
- In Discharging mode, it pushes FE to neighbors each server tick if possible.

## Solar Panel

### Purpose

Generates FE from environmental sunlight conditions and pushes FE to adjacent receivers.

### Size

- 1 x 1 x 1 block footprint

### Core Behaviour

- Internal capacity: 12,000 FE
- Max generation: 120 FE/t
- Max output push: 120 FE/t
- Default device IP: 192.168.1.1
- Starts enabled by default

### Generation Factors

Real-time generation uses:

- Daytime sun factor (ramps to midday peak, zero at night)
- Weather factor (rain/thunder penalties)
- Sky access (blocked sky = zero generation)
- Sky light level factor
- Height factor (scales from 70% to 120% based on altitude vs sea level/max build)

## RS485 Wire

### Purpose

Extends local control reachability for J-Box device discovery/control.

### Size

- 1 x 1 x 1 block footprint, floor-mounted or ceiling-mounted

### Latest Behaviour

- Mounting planes supported:
  - Floor-mounted (top face support required)
  - Ceiling-mounted (bottom face support required)
- Auto-connections:
  - Connects to neighboring RS485 Wire in N/S/E/W
  - Connections only form within the same mounting plane (floor-to-floor, ceiling-to-ceiling)
  - Wire visuals now flow into adjacent RS485-aware blocks, including terminals and mounted devices, to avoid a disconnected floating look
- Survival:
  - Breaks if its supporting block face is removed

### Visual Style

- Copper wire material is retained for the network cable itself
- Connection points are intended to visually blend into nearby RS485-aware blocks

### Important Note

- RS485 Wire currently does not carry FE, only control graph connectivity.

## RS485 Terminal

### Purpose

Compact network terminal/switch style block intended as an RS485 endpoint/connection point.

### Size

- 1 x 1 x 1 block footprint

### Current Behaviour

- Registered and obtainable in creative tab
- Has localized name and models
- Acts as a reactive RS485 node with a black terminal body and copper connector pieces
- Visually responds to neighboring RS485 connections so the wire appears to emerge from the block
- No menu/UI
- Now participates in RS485 traversal and connection flow as part of the local control graph

### Current Limitation

- RS485 Terminal is still a visual/control-network node only; it does not transport FE.

## FE to Redstone Transformer

### Purpose

Mounted read block that converts attached device FE state/activity into redstone output.

### Size

- 1 x 1 x 1 block footprint, surface-mounted

### Attachment Rule

- Must be placed onto an FE-capable attached block face.
- If attachment becomes invalid, it breaks.

### Output Rules

- Basic Battery: outputs 0-15 based on fill ratio
- Coal Generator: outputs 15 while actively generating, else 0
- Other FE storages (if any): outputs 0-15 based on fill ratio

### Energy Cost While Active

If computed signal > 0, transformer consumes energy from attached device profile:

- Basic Battery profile: update every 5 ticks, 25 FE per update (100 FE/s)
- Coal Generator profile: update every tick, 40 FE per update (consumes burn time equivalent)
- Default profile: update every 5 ticks, 20 FE per update

If energy cost cannot be paid on an update, output is forced to 0.

## Recipes And Content Coverage

Implemented recipes currently exist for:

- J-Box
- Coal Generator
- Basic Battery
- Solar Panel
- RS485 Wire
- FE to Redstone Transformer

Current content gap:

- RS485 Terminal recipe is not yet present in data/jugglecraft/recipes.

## Known Limits (Current Design)

- No FE cables or distance-loss transmission yet.
- No FE routing graph; FE remains neighbor-push based.
- No remote connectivity (SIM/4G/player handheld) yet.
- No PLC blocks/automation rule engine yet.
- No redstone network complexity costing yet.
- RS485 Terminal exists visually/functionally as a block, but is not part of traversal logic yet.

## To Be Implemented (Backlog)

This section is split into near-term engineering tasks and larger feature ideas. Keep adding new ideas under the appropriate group.

## A) Near-Term Implementation Tasks

- [ ] Refactor device menus to be more usable (from TODO.txt)
- [ ] Add crafting recipe for RS485 Terminal
- [ ] Decide and implement RS485 Terminal network semantics (traversable node, endpoint rules, or bridge logic)
- [ ] Improve in-game docs/tooltips for IP registration and slot control flow
- [ ] Add Cabinet item / block for micro power grids inside a larger cabinet footprint
- [ ] Add Power Cable as a true FE transport block for the power system
- [ ] Add Electrical Map for network visibility, routing, or grid inspection

## B) Planned System Expansions (From plan.md)

- [ ] Core registry and networking refinements
- [ ] Cabinet-based microgrid system and internal service networks
- [ ] Progression expansion:
  - Wind turbine generation
  - Advanced batteries
  - Industrial batteries
- [ ] J-Box routing expansion:
  - Source selection
  - Battery charge preference
  - Load shedding
  - Low-power warnings
  - Manual system-wide enable/disable
- [ ] Redstone bridge expansion:
  - FE-powered redstone cost estimation by connected component complexity
- [ ] PLC layer:
  - Trigger logic blocks/modules tied to J-Box state
- [ ] Remote connectivity:
  - SIM card items
  - Online J-Box or upgrade path
  - Smart controller handheld
  - 4G towers with FE power and range checks
- [ ] Communication operations:
  - Action energy costs
  - Offline handling when towers lose power
  - Communication-state readouts
- [ ] Balance and polish pass:
  - Capacities, rates, burn values, UX, and assets
- [ ] Power Cable network infrastructure for FE distribution
- [ ] Electrical Map UI and/or block for visualizing power networks

## C) Idea Backlog (From JuggleEnergyMinecraftModIdea.md)

### Generation And Storage

- [ ] Wind Turbine generation affected by environment/height
- [ ] Wave Power generation near water/oceans
- [ ] Additional battery tiers:
  - Advanced Battery
  - High-Capacity Battery
  - Industrial Battery
- [ ] Optional generator upgrades for efficiency/output

### Energy Infrastructure

- [ ] Power cables and transfer lines with distance loss
- [ ] Transformers for stepping voltage/power up/down (gameplay abstraction)
- [ ] Fuses and breakers for circuit protection
- [ ] Energy alarms and emergency shutdown systems
- [ ] Cabinet-based enclosed microgrid systems

### Industrial / Infrastructure Items

- [ ] Power Cable
- [ ] Electrical Map
- [ ] Cabinet

### Control And Automation

- [ ] Priority-driven load management and power reservation policies
- [ ] Automatic source switching and load balancing
- [ ] PLC triggers for low battery, reserve mode, non-essential shutdown
- [ ] Manual override controls for operators

### Redstone Integration

- [ ] Dynamic redstone network detection and weighted power costing
- [ ] Device/circuit complexity scaling for FE demand
- [ ] Better balancing between simple and complex contraptions

### Remote Connectivity (4G/SIM Concept)

- [ ] SIM card item for authentication/linking
- [ ] Online J-Box variant with remote command/data support
- [ ] Smart Controller handheld for remote monitoring and control
- [ ] 4G tower coverage networks requiring continuous FE power
- [ ] Remote action energy costs and redundancy/backup gameplay

## Suggested Format For Future Additions

When you add new ideas, use this template so the backlog stays easy to sort:

- [ ] Feature Name
  - Group: Generation | Storage | Control | Redstone | Remote | UX | Balance
  - Priority: P1 | P2 | P3
  - Status: Idea | Planned | In Progress | Implemented
  - Notes: short gameplay intent and constraints

## Quick Validation Checklist

When updating this guide after code changes, verify:

- Block is registered in ModBlocks and ModItems
- Block appears in ModCreativeTabs if intended
- Lang keys exist for block/item names
- Recipe exists if survival-obtainable
- Guide constants match block entity code values
- Known limitations section still accurate
