package vahy.impl.search.node.factory;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.RewardAggregator;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.AbstractStateSpaceSearchTest;
import vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;
import vahy.testDomain.search.TestSearchNodeImpl;

import java.util.Collections;
import java.util.LinkedHashMap;

public class BaseSearchNodeMetadataFactoryTest extends AbstractStateSpaceSearchTest {

    @Test
    public void testBaseSearchNodeMetadataFactory() {
        RewardAggregator<DoubleReward> rewardAggregator = new DoubleScalarRewardAggregator();
        BaseSearchNodeMetadataFactory<TestAction, DoubleReward, DoubleVector, TestState, TestState> factory = new BaseSearchNodeMetadataFactory<>(rewardAggregator);

        double parentCumulativeReward = 50.0;

        TestSearchNodeImpl<BaseSearchNodeMetadata<DoubleReward>> node = new TestSearchNodeImpl<>(
            new TestState(Collections.singletonList('A')),
            new BaseSearchNodeMetadata<>(new DoubleReward(parentCumulativeReward), new DoubleReward(1.0d), new DoubleReward(0.0d)),
            new LinkedHashMap<>());

        assertMetadata(factory, parentCumulativeReward, node, TestAction.A);
        assertMetadata(factory, parentCumulativeReward, node, TestAction.Z);
    }

    private void assertMetadata(
        BaseSearchNodeMetadataFactory<TestAction, DoubleReward, DoubleVector, TestState, TestState> factory,
        double parentCumulativeReward,
        TestSearchNodeImpl<BaseSearchNodeMetadata<DoubleReward>> node,
        TestAction action) {

        StateRewardReturn<TestAction, DoubleReward, DoubleVector, TestState, TestState> stateRewardReturn = node.applyAction(action);
        BaseSearchNodeMetadata<DoubleReward> newSearchNodeMetadata = factory.createSearchNodeMetadata(node, stateRewardReturn, action);

        Assert.assertEquals(newSearchNodeMetadata.getGainedReward().getValue(), action.getReward(), DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getCumulativeReward().getValue(), action.getReward() + parentCumulativeReward, DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getPredictedReward().getValue(), 0.0, DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getExpectedReward().getValue(), 0.0, DOUBLE_TOLERANCE);
    }
}
