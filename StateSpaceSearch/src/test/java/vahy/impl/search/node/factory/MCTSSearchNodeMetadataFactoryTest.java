package vahy.impl.search.node.factory;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.AbstractStateSpaceSearchTest;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;
import vahy.testDomain.search.TestSearchNodeImpl;

import java.util.Collections;
import java.util.LinkedHashMap;

public class MCTSSearchNodeMetadataFactoryTest extends AbstractStateSpaceSearchTest {

    @Test
    public void testBaseSearchNodeMetadataFactory() {
        RewardAggregator<DoubleScalarReward> rewardAggregator = new DoubleScalarRewardAggregator();
        SearchNodeMetadataFactory<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState> factory = new MCTSSearchNodeMetadataFactory<>(rewardAggregator);
        double parentCumulativeReward = 50.0;
        TestSearchNodeImpl<MCTSNodeMetadata<DoubleScalarReward>> node = new TestSearchNodeImpl<>(
            new TestState(Collections.singletonList('A')),
            new MCTSNodeMetadata<>(new DoubleScalarReward(parentCumulativeReward), new DoubleScalarReward(1.0d), new DoubleScalarReward(0.0d)),
            new LinkedHashMap<>());

        assertMetadata(factory, parentCumulativeReward, node, TestAction.A);
        assertMetadata(factory, parentCumulativeReward, node, TestAction.Z);
    }

    private void assertMetadata(
        SearchNodeMetadataFactory<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState> factory,
        double parentCumulativeReward,
        TestSearchNodeImpl<MCTSNodeMetadata<DoubleScalarReward>> node,
        TestAction action) {

        StateRewardReturn<TestAction, DoubleScalarReward, DoubleVectorialObservation, TestState> stateRewardReturn = node.applyAction(action);
        MCTSNodeMetadata<DoubleScalarReward> newSearchNodeMetadata = factory.createSearchNodeMetadata(node, stateRewardReturn);

        Assert.assertEquals(newSearchNodeMetadata.getGainedReward().getValue(), action.getReward(), DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getCumulativeReward().getValue(), action.getReward() + parentCumulativeReward, DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getDefaultEstimatedReward().getValue(), 0.0, DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getExpectedReward().getValue(), 0.0, DOUBLE_TOLERANCE);
    }
}
