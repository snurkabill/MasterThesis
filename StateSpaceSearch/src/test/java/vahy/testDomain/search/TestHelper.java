package vahy.testDomain.search;

import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestHelper {

    public static SearchNode<TestAction, DoubleScalarReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, TestState> createOneLevelTree(boolean playerTurn) {
        TestState initialState = playerTurn ? TestState.getDefaultInitialStatePlayerTurn() : TestState.getDefaultInitialStateOpponentTurn();
        MonteCarloTreeSearchMetadata<DoubleScalarReward> rootMetadata = new MonteCarloTreeSearchMetadata<>(new DoubleScalarReward(0.0), new DoubleScalarReward(0.0), new DoubleScalarReward(0.0));
        rootMetadata.increaseVisitCounter();
        SearchNode<TestAction, DoubleScalarReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, TestState> root = new SearchNodeImpl<>(
            initialState,
            rootMetadata,
            new LinkedHashMap<>());
        Map<TestAction, SearchNode<TestAction, DoubleScalarReward, DoubleVectorialObservation, MonteCarloTreeSearchMetadata<DoubleScalarReward>, TestState>> childNodeMap = root.getChildNodeMap();
        for (TestAction playerAction : playerTurn ? TestAction.playerActions : TestAction.opponentActions) {
            StateRewardReturn<TestAction, DoubleScalarReward, DoubleVectorialObservation, TestState> rewardReturn = initialState.applyAction(playerAction);
            childNodeMap.put(playerAction, new SearchNodeImpl<>(
                rewardReturn.getState(),
                new MonteCarloTreeSearchMetadata<>(new DoubleScalarReward(rewardReturn.getReward().getValue()), new DoubleScalarReward(rewardReturn.getReward().getValue()), new DoubleScalarReward(rewardReturn.getReward().getValue())),
                new LinkedHashMap<>(),
                root,
                playerAction)
            );
        }
        return root;
    }
}
