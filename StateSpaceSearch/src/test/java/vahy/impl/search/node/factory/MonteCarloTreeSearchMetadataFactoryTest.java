package vahy.impl.search.node.factory;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.search.AbstractStateSpaceSearchTest;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadataFactory;
import vahy.impl.testdomain.model.TestAction;
import vahy.impl.testdomain.model.TestState;
import vahy.testDomain.search.TestSearchNodeImpl;

import java.util.Collections;
import java.util.LinkedHashMap;

public class MonteCarloTreeSearchMetadataFactoryTest extends AbstractStateSpaceSearchTest {

    @Test
    public void testBaseSearchNodeMetadataFactory() {
        SearchNodeMetadataFactory<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> factory = new MonteCarloTreeSearchMetadataFactory<>();
        double parentCumulativeReward = 50.0;
        TestSearchNodeImpl<MonteCarloTreeSearchMetadata> node = new TestSearchNodeImpl<>(
            new TestState(Collections.singletonList('A')),
            new MonteCarloTreeSearchMetadata(parentCumulativeReward, 1.0d, 0.0d),
            new LinkedHashMap<>());

        assertMetadata(factory, parentCumulativeReward, node, TestAction.A);
        assertMetadata(factory, parentCumulativeReward, node, TestAction.Z);
    }

    private void assertMetadata(
        SearchNodeMetadataFactory<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> factory,
        double parentCumulativeReward,
        TestSearchNodeImpl<MonteCarloTreeSearchMetadata> node,
        TestAction action) {

        StateRewardReturn<TestAction, DoubleVector, TestState, TestState> stateRewardReturn = node.applyAction(action);
        MonteCarloTreeSearchMetadata newSearchNodeMetadata = factory.createSearchNodeMetadata(node, stateRewardReturn, action);

        Assert.assertEquals(newSearchNodeMetadata.getGainedReward(), action.getReward(), DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getCumulativeReward(), action.getReward() + parentCumulativeReward, DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getPredictedReward(), 0.0, DOUBLE_TOLERANCE);
        Assert.assertEquals(newSearchNodeMetadata.getExpectedReward(), 0.0, DOUBLE_TOLERANCE);
    }
}
