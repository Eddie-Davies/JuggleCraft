# JuggleCraft Roadmap

This roadmap is intentionally ordered around the development priority for the mod:

1. Items and block content first.
2. Power systems and infrastructure second.
3. Gameplay loop systems after the core power/item foundation is stable.
4. Factions, money, and broader progression last.

The goal is to avoid building economy or faction systems before the item and power foundations are strong enough to support them.

## Current Status

### Implemented

- J-Box controller block and menu system
- Coal Generator
- Basic Battery
- Solar Panel
- RS485 Wire control network
- RS485 Terminal placeholder block
- FE to Redstone Transformer
- Core item/block registrations, creative tab entries, recipes, and language support for the current block set
- Floor and ceiling mounting support for RS485 Wire

### In Progress / Recently Added

- Guide and documentation cleanup
- More structured backlog tracking for future content
- RS485 expansion planning for more infrastructure blocks
## Roadmap Summary

| Phase | Focus | Status |
|---|---|---|
| 1 | Item and block content foundation | Active |
| 2 | Power systems and infrastructure | Next |
| 3 | Gameplay loop systems | Planned |
| 4 | Factions, money, and wider world progression | Deferred |

## Phase 1 - Items and Block Content

This is the current priority area.

### Goals

- Add the missing blocks and items that the power system needs.
- Keep everything easy to craft, place, and understand in survival.
- Make the content list feel complete before expanding into broader systems.

### Already Done

- Base generator, battery, solar, controller, wire, terminal, and transformer blocks
- Initial recipes and creative tab coverage

### Still Needed

- Power Cable
- Electrical Map
- Cabinet
- Any missing recipes for planned infrastructure blocks
- Any item or block placeholders needed to support the upcoming industrial tier

### Design Notes

- New items should be added in a way that supports future upgrades and larger systems.
- If a block is expected to become larger than 1 x 1 x 1, the footprint should be documented in blocks from the start.
- Items and blocks should be structured so they can later feed into cabinets, power routing, and map-based inspection tools.

## Phase 2 - Power Systems and Infrastructure

Once the item foundation is in place, the next step is to make power distribution feel like a real system instead of just a few isolated blocks.

### Goals

- Move beyond adjacency-only energy handling where needed.
- Introduce actual power transport blocks.
- Support larger industrial builds and microgrid-style sub-networks.

### Planned Features

- Power Cable for FE transport
- Cabinet for enclosed micro power grids or local service networks
- Electrical Map for visualizing or inspecting power networks
- Improved power routing and distribution rules
- Support for larger industrial items and machines
- Optional distance loss, protection, or routing rules if they fit the design

### Questions To Resolve Later

- Should Power Cable fully replace some adjacency-only transfer behavior, or complement it?
- Should Cabinet act as a contained network hub, storage box, or both?
- Should Electrical Map be a block, item, GUI, or combination of these?

## Phase 3 - Gameplay Loop Systems

This phase should come after the item and power base is stable.

### Goals

- Build the day-to-day loop around energy, automation, and grid management.
- Make the player choose how power is generated, stored, routed, and controlled.
- Add gameplay depth without jumping ahead to factions or currency.

### Planned Systems

- Power prioritization and load management
- Battery charge preference and reserve rules
- Source switching and backup generation behavior
- Warning states for low power or overload conditions
- More advanced automation around J-Box-controlled devices
- PLC-style logic or triggers for energy state changes
- Redstone bridge expansion with FE cost estimation for connected circuits

### Gameplay Loop Targets

- Generate power
- Store power
- Move power through a network
- Control what stays online
- Expand into larger industrial layouts

## Phase 4 - Factions, Money, and Wider Progression

This is deliberately deferred until the core power loop feels solid.

### Why It Is Later

- Factions and money depend on a stable gameplay foundation.
- These systems are easier to design once item progression and power routing already work well.
- Building them too early risks making the mod feel scattered instead of focused.

### Later Goals

- Factions or groups that interact with the energy world
- Currency or trade systems
- Reputation, contracts, or progression tied to power infrastructure
- Broader world gameplay loops built on top of the industrial systems

## Backlog By Area

### Item And Block Backlog

- Power Cable
- Electrical Map
- Cabinet
- Wind Turbine
- Wave Power block or generator
- Advanced Battery
- High-Capacity Battery
- Industrial Battery
- SIM Card
- Online J-Box
- Smart Controller
- 4G Tower

### Power And Network Backlog

- FE transport cables
- Power routing rules
- Microgrid or cabinet-contained power services
- Power loss or routing limits if needed for balance
- Protection blocks such as fuses or breakers

### Gameplay Loop Backlog

- Priority-based device control
- Load shedding
- Power source switching
- Low-power alarms
- Emergency shutdown systems
- Automation triggers for energy thresholds
- Redstone circuit power costing

### Deferred Systems

- Factions
- Money
- Trade/economy systems
- Reputation systems
- Advanced quest or mission loops

## Working Rule For Future Additions

If a new idea is suggested, place it into one of these buckets first:

- Item or block content
- Power systems and infrastructure
- Gameplay loop systems
- Factions, money, or late-game meta systems

If it does not fit one of those buckets, add a short note explaining where it belongs and why.

## Update Rule

When a new block, item, or system is implemented:

- Move it from backlog into the implemented section.
- Add the relevant size, recipe, and behavior notes in the block guide.
- Keep this roadmap focused on sequencing, not detailed mechanics.
