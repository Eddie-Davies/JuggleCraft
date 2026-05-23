# JuggleCraft Development Plan (Ordered Implementation Roadmap)

Current implemented systems include J-Box, Coal Generator, Basic Battery, Solar Panel, RS485 Wire, RS485 Terminal, and FE→Redstone Transformer, with known gaps around recipes, UI consistency, FE behaviour, and expansion hooks.

---

# Phase 1 — Stabilisation And Consistency Pass

**Goal:** Make the current feature set coherent, usable, and internally consistent before expanding systems.

## 1. Menu / UI Refinement

Refactor all existing device menus into a unified UI system.

### Requirements

- Standardise layout, navigation, spacing, and terminology across all blocks.
- Apply one consistent interaction model for:
  - IP editing
  - device registration
  - slot navigation
  - status display
  - enable / disable controls
  - battery mode switching
- Reduce menu complexity and improve discoverability.
- Add clearer labels and status indicators.
- Ensure UI style matches the intended industrial / technical visual direction.

### Target Blocks

- J-Box
- Basic Battery
- Coal Generator
- Solar Panel
- Future Screen UI system

---

## 2. Improve In-Game Documentation / UX

Improve player understanding after UI cleanup.

### Implement

- Tooltips
- Concise block descriptions
- IP registration help
- Slot registration flow explanations
- Control action explanations
- Network status feedback

### Focus Areas

Explain clearly:

- RS485 reachability
- J-Box registration workflow
- FE vs RS485 separation

---

# Phase 2 — Content Completion Pass

**Goal:** Ensure everything currently implemented is obtainable and complete.

## 3. Recipe Coverage Completion

Implement crafting recipes for **every existing obtainable item/block**.

### Checklist

- Verify every registered block has a recipe.
- Verify every creative-tab item is survival obtainable (unless intentionally creative-only).

### Known Missing Recipe

- RS485 Terminal recipe.

### Validation Checklist

For every item/block:

- Registered in `ModBlocks` / `ModItems`
- Appears in creative tab if intended
- Lang entries exist
- Recipe exists
- Textures / models valid
- Documentation updated

---

# Phase 3 — Behaviour Fixes And System Logic Cleanup

**Goal:** Fix existing gameplay logic before adding new infrastructure.

## 4. Solar Panel Power Integration Fix

Audit and repair Solar Panel behaviour.

### Verify

- FE generation calculation correctness
- Sunlight factor handling
- Weather penalties
- Sky visibility handling
- Height scaling
- Enabled / disabled logic
- FE storage updates
- Adjacent FE transfer behaviour

### Ensure

Generation pipeline behaves deterministically:

```text
generation → internal storage → transfer
```

---

## 5. Battery Charge / Discharge Logic Rewrite

Refactor Basic Battery logic to strictly match intended behaviour.

### Required Mode Behaviour

#### Charging

- Accepts FE
- No extraction
- No active push

#### Idle

- No receive
- No extract
- No automatic push

#### Discharging

- Extraction enabled
- Active adjacent push enabled

### Requirements

Remove ambiguous edge cases.

Ensure:

- FE capability exposure matches selected mode.
- Server tick behaviour is predictable.
- Push logic is deterministic.
- UI reflects true internal state.

---

# Phase 4 — Visual Language Standardisation

**Goal:** Establish a clean long-term art and sizing system.

## 6. Block Size Convention Cleanup

Redesign sizing standards.

### Adopt One Convention

Use:

```text
width x height x depth
```

in block footprint units.

### Requirements

- Update descriptions
- Update internal documentation
- Ensure future oversized systems follow the same standard

### Prepare For

- Cabinets
- Screens
- Larger devices
- Multi-block systems

---

## 7. Texture And Asset Consistency Pass

Redesign visuals across existing content.

### Goals

- Clearer visual hierarchy
- Consistent industrial styling
- Modular appearance
- Readable device identity

### Review Assets

- J-Box
- Battery
- Solar Panel
- Coal Generator
- RS485 Wire
- RS485 Terminal
- FE Transformer

### Improve

- Status indicators
- Power state readability
- Texture scale consistency
- Colour language
- Material consistency

### Target Style

Industrial, technical, modular, clean design language.

---

# Phase 5 — Infrastructure Expansion Foundations

**Goal:** Add architectural hooks needed for larger future systems.

## 8. Add Access Point System

Create a generalized **Access Point** concept for larger devices.

### Purpose

Provide UI / connection interfaces for systems larger than simple 1-block devices.

### Must Support Future

- Cabinets
- Power cables
- Large batteries
- Multi-block machines
- Routing infrastructure
- Advanced controllers

### Define

- Attachment rules
- UI interaction rules
- Capability exposure
- Network visibility rules

---

## 9. Screen Block Implementation

Implement a dedicated **Screen Block**.

### Purpose

Provide shared UI access for UI-capable devices.

### Supported Targets

- J-Box
- Batteries
- Solar systems
- Future cabinets
- Future power networks

### Requirements

- Reusable UI host
- Block-agnostic display layer
- Remote data display support
- Scalable dashboard system

---

# Phase 6 — Network And Infrastructure Preparation

**Goal:** Prepare architecture for upcoming larger systems.

## 10. Finalise RS485 Terminal Semantics

Define exactly what the RS485 Terminal is.

### Choose One Model

- Traversable node
- Endpoint
- Bridge
- Hybrid logic

### Update

- Traversal logic
- Discovery rules
- Connection visuals
- Documentation

---

## 11. Introduce FE Transport Infrastructure Hook

Prepare groundwork for future FE networking.

### Prepare Architecture For

- Power Cable system
- Distance transport
- FE routing
- Cabinet internals
- Larger grids

### Scope

Do **not** necessarily implement full cable networks yet.

Instead:

Create interfaces and abstraction layers so current adjacency FE can evolve cleanly later.

---

# Phase 7 — Future Expansion Layer

**Only begin after Phases 1–6 are stable.**

## Power Infrastructure

- Power Cable network
- Transformers
- Fuses / breakers
- Transmission loss
- Enclosed cabinet microgrids

---

## Storage Progression

- Advanced Battery
- High-Capacity Battery
- Industrial Battery

---

## Generation Expansion

- Wind Turbine
- Wave Power
- Generator upgrades

---

## Control Expansion

- Routing policies
- Source switching
- Battery priorities
- Load shedding
- Low-power warnings
- Automation rules
- PLC logic layer

---

## Redstone Integration Expansion

- Dynamic circuit detection
- Complexity-based FE costs
- Weighted power scaling

---

## Remote Connectivity Layer

- SIM cards
- Online J-Box
- Smart Controller handheld
- 4G towers
- Signal coverage
- Communication energy costs
- Remote automation

---

# Recommended Implementation Order Summary

1. Menu / UI standardisation  
2. In-game docs / usability improvements  
3. Recipe completion  
4. Solar behaviour fixes  
5. Battery logic fixes  
6. Size convention cleanup  
7. Texture / visual consistency pass  
8. Access Point system  
9. Screen Block  
10. RS485 Terminal semantics resolution  
11. FE infrastructure abstraction layer  
12. Large roadmap systems

---

# Design Principles

All future work should follow these rules:

- Prioritise consistency before expansion.
- Stabilise gameplay logic before adding new systems.
- Maintain modular, industrial visual language.
- Keep UI behaviour unified across all controllable blocks.
- Design new infrastructure with future scalability in mind.
- Prefer abstraction layers over hardcoded one-off systems.
- Ensure every implemented item is fully obtainable, documented, and integrated.