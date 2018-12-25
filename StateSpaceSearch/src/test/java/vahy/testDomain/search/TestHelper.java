package vahy.testDomain.search;

import vahy.api.model.StateRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestHelper {

    public static SearchNode<TestAction, DoubleReward, DoubleVector, TestState, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> createOneLevelTree(boolean playerTurn) {
        TestState initialState = playerTurn ? TestState.getDefaultInitialStatePlayerTurn() : TestState.getDefaultInitialStateOpponentTurn();
        MonteCarloTreeSearchMetadata<DoubleReward> rootMetadata = new MonteCarloTreeSearchMetadata<>(new DoubleReward(0.0), new DoubleReward(0.0), new DoubleReward(0.0));
        rootMetadata.increaseVisitCounter();
        SearchNode<TestAction, DoubleReward, DoubleVector, TestState, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> root = new SearchNodeImpl<>(
            initialState,
            rootMetadata,
            new LinkedHashMap<>());
        Map<TestAction, SearchNode<TestAction, DoubleReward, DoubleVector, TestState, MonteCarloTreeSearchMetadata<DoubleReward>, TestState>> childNodeMap = root.getChildNodeMap();
        for (TestAction playerAction : playerTurn ? TestAction.playerActions : TestAction.opponentActions) {
            StateRewardReturn<TestAction, DoubleReward, DoubleVector, TestState, TestState> rewardReturn = initialState.applyAction(playerAction);
            childNodeMap.put(playerAction, new SearchNodeImpl<>(
                rewardReturn.getState(),
                new MonteCarloTreeSearchMetadata<>(new DoubleReward(rewardReturn.getReward().getValue()), new DoubleReward(rewardReturn.getReward().getValue()), new DoubleReward(rewardReturn.getReward().getValue())),
                new LinkedHashMap<>(),
                root,
                playerAction)
            );
        }
        return root;
    }
}
