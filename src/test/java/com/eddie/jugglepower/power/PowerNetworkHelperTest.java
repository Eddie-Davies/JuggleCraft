package com.eddie.jugglepower.power;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class PowerNetworkHelperTest {

    @Test
    void transfersEnergyToTierMatchedNeighbor() {
        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos targetPos = sourcePos.east();

        NodeFixture source = createNode(PowerTier.DC, 1000, 0, 120, 500);
        NodeFixture target = createNode(PowerTier.DC, 1000, 120, 120, 0);

        PowerNetworkHelper.NeighborLookup lookup = lookupWithNodes(Map.of(targetPos, target.blockEntity));

        int moved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, sourcePos, source.node, 120);

        assertEquals(120, moved);
        assertEquals(380, source.getStoredEnergy());
        assertEquals(120, target.getStoredEnergy());
        assertEquals(0, source.getFaultCount());
    }

    @Test
    void balancesEnergyAcrossTwoReceivers() {
        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos westPos = sourcePos.west();
        BlockPos eastPos = sourcePos.east();

        NodeFixture source = createNode(PowerTier.DC, 1000, 0, 240, 200);
        NodeFixture westReceiver = createNode(PowerTier.DC, 1000, 120, 120, 0);
        NodeFixture eastReceiver = createNode(PowerTier.DC, 1000, 120, 120, 0);

        PowerNetworkHelper.NeighborLookup lookup = lookupWithNodes(Map.of(
                westPos, westReceiver.blockEntity,
                eastPos, eastReceiver.blockEntity
        ));

        int moved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, sourcePos, source.node, 120);

        assertEquals(120, moved);
        assertEquals(80, source.getStoredEnergy());
        assertEquals(60, westReceiver.getStoredEnergy());
        assertEquals(60, eastReceiver.getStoredEnergy());
    }

    @Test
    void redistributesUnusedShareToHigherDemandReceiver() {
        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos westPos = sourcePos.west();
        BlockPos eastPos = sourcePos.east();

        NodeFixture source = createNode(PowerTier.DC, 1000, 0, 240, 500);
        NodeFixture nearlyFullReceiver = createNode(PowerTier.DC, 1000, 120, 120, 980);
        NodeFixture emptyReceiver = createNode(PowerTier.DC, 1000, 120, 120, 0);

        PowerNetworkHelper.NeighborLookup lookup = lookupWithNodes(Map.of(
                westPos, nearlyFullReceiver.blockEntity,
                eastPos, emptyReceiver.blockEntity
        ));

        int moved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, sourcePos, source.node, 100);

        assertEquals(100, moved);
        assertEquals(400, source.getStoredEnergy());
        assertEquals(1000, nearlyFullReceiver.getStoredEnergy());
        assertEquals(80, emptyReceiver.getStoredEnergy());
    }

    @Test
    void chargesBatteryAcrossDcCableWhenCableTicksBeforeSource() {
        BlockPos pvPos = new BlockPos(0, 64, 0);
        BlockPos cablePos = pvPos.east();
        BlockPos batteryPos = cablePos.east();

        NodeFixture pv = createNode(PowerTier.DC, 2000, 0, 120, 240);
        NodeFixture cable = createNode(PowerTier.DC, 2000, 120, 120, 0);
        NodeFixture battery = createNode(PowerTier.DC, 2000, 120, 120, 0);

        PowerNetworkHelper.NeighborLookup lookup = lookupWithNodes(Map.of(
                pvPos, pv.blockEntity,
                cablePos, cable.blockEntity,
                batteryPos, battery.blockEntity
        ));

        int firstCableMoved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, cablePos, cable.node, 120);
        int firstPvMoved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, pvPos, pv.node, 120);
        int secondCableMoved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, cablePos, cable.node, 120);

        assertEquals(0, firstCableMoved);
        assertEquals(120, firstPvMoved);
        assertEquals(120, secondCableMoved);
        assertEquals(120, battery.getStoredEnergy());
    }

    @Test
    void transfersAcrossCableOverTwoTicks() {
        BlockPos pvPos = new BlockPos(0, 64, 0);
        BlockPos cablePos = pvPos.east();
        BlockPos batteryPos = cablePos.east();

        NodeFixture pv = createNode(PowerTier.DC, 2000, 0, 120, 240);
        NodeFixture cable = createNode(PowerTier.DC, 2000, 120, 120, 0);
        NodeFixture battery = createNode(PowerTier.DC, 2000, 120, 120, 0);

        PowerNetworkHelper.NeighborLookup lookup = lookupWithNodes(Map.of(
                pvPos, pv.blockEntity,
                cablePos, cable.blockEntity,
                batteryPos, battery.blockEntity
        ));

        int pvMoved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, pvPos, pv.node, 120);

        assertEquals(120, pvMoved);
        assertEquals(120, pv.getStoredEnergy());
        assertEquals(120, cable.getStoredEnergy());
        assertEquals(0, battery.getStoredEnergy());

        int cableMoved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, cablePos, cable.node, 120);

        assertEquals(120, cableMoved);
        assertEquals(120, pv.getStoredEnergy());
        assertEquals(0, cable.getStoredEnergy());
        assertEquals(120, battery.getStoredEnergy());
    }

    @Test
    void mismatchedTierMarksFaultAndBlocksTransfer() {
        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos targetPos = sourcePos.east();

        NodeFixture source = createNode(PowerTier.DC, 1000, 0, 120, 300);
        NodeFixture lvTarget = createNode(PowerTier.LV, 1000, 120, 120, 0);

        PowerNetworkHelper.NeighborLookup lookup = lookupWithNodes(Map.of(targetPos, lvTarget.blockEntity));

        int moved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, sourcePos, source.node, 120);

        assertEquals(0, moved);
        assertEquals(300, source.getStoredEnergy());
        assertEquals(0, lvTarget.getStoredEnergy());
        assertEquals(1, source.getFaultCount());
        assertEquals(PowerTier.LV, source.getLastFaultNeighborTier());
    }

    @Test
    void faultedSourceDoesNotTransfer() {
        BlockPos sourcePos = new BlockPos(0, 64, 0);
        BlockPos targetPos = sourcePos.east();

        NodeFixture source = createNode(PowerTier.DC, 1000, 0, 120, 300);
        NodeFixture target = createNode(PowerTier.DC, 1000, 120, 120, 0);
        source.setFaulted(true);

        PowerNetworkHelper.NeighborLookup lookup = lookupWithNodes(Map.of(targetPos, target.blockEntity));

        int moved = PowerNetworkHelper.pushToTierMatchedNeighbors(lookup, sourcePos, source.node, 120);

        assertEquals(0, moved);
        assertEquals(300, source.getStoredEnergy());
        assertEquals(0, target.getStoredEnergy());
    }

    private static NodeFixture createNode(PowerTier tier,
                                          int capacity,
                                          int maxReceive,
                                          int maxExtract,
                                          int initialEnergy) {
        TieredEnergyStorage storage = new TieredEnergyStorage(capacity, maxReceive, maxExtract);
        storage.setStoredEnergy(initialEnergy);

        BlockEntity blockEntity = mock(BlockEntity.class, withSettings().extraInterfaces(TieredPowerNode.class));
        TieredPowerNode node = (TieredPowerNode) blockEntity;

        FaultState faultState = new FaultState();

        when(node.getPowerTier()).thenReturn(tier);
        when(node.getTieredEnergyStorage()).thenReturn(storage);
        when(node.isTierFaulted()).thenAnswer(invocation -> faultState.faulted);
        doAnswer(invocation -> {
            faultState.faultCount++;
            faultState.lastFaultNeighborTier = invocation.getArgument(0);
            faultState.faulted = true;
            return null;
        }).when(node).markTierFault(nullable(PowerTier.class));

        return new NodeFixture(blockEntity, node, storage, faultState);
    }

    private static PowerNetworkHelper.NeighborLookup lookupWithNodes(Map<BlockPos, BlockEntity> initialNodes) {
        Map<BlockPos, BlockEntity> nodes = new HashMap<>(initialNodes);
        return nodes::get;
    }

    private static final class NodeFixture {
        private final BlockEntity blockEntity;
        private final TieredPowerNode node;
        private final TieredEnergyStorage energyStorage;
        private final FaultState faultState;

        private NodeFixture(BlockEntity blockEntity,
                            TieredPowerNode node,
                            TieredEnergyStorage energyStorage,
                            FaultState faultState) {
            this.blockEntity = blockEntity;
            this.node = node;
            this.energyStorage = energyStorage;
            this.faultState = faultState;
        }

        private int getStoredEnergy() {
            return energyStorage.getEnergyStored();
        }

        private int getFaultCount() {
            return faultState.faultCount;
        }

        private PowerTier getLastFaultNeighborTier() {
            return faultState.lastFaultNeighborTier;
        }

        private void setFaulted(boolean faulted) {
            faultState.faulted = faulted;
        }
    }

    private static final class FaultState {
        private int faultCount;
        private PowerTier lastFaultNeighborTier;
        private boolean faulted;
    }
}
