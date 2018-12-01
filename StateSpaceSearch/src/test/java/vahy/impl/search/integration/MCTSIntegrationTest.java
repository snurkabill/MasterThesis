package vahy.impl.search.integration;

import org.testng.annotations.Test;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadataFactory;
import vahy.impl.search.MCTS.MonteCarloEvaluator;
import vahy.impl.search.MCTS.ucb1.Ucb1NodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.MCTS.MonteCarloTreeSearchUpdater;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class MCTSIntegrationTest {

    @Test
    public void testMCTSAlgorithm() {

        SplittableRandom random = new SplittableRandom(0);

        RewardAggregator<DoubleReward> rewardAggregator = new DoubleScalarRewardAggregator();
        SearchNode<TestAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> root = new SearchNodeImpl<>(
            new TestState(Arrays.asList('Z')),
            new MonteCarloTreeSearchMetadata<>(new DoubleReward(0.0), new DoubleReward(0.0), new DoubleReward(0.0)),
            new LinkedHashMap<>()
        );

        SearchNodeMetadataFactory<TestAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> metadataFactory =
            new MonteCarloTreeSearchMetadataFactory<>(rewardAggregator);
        SearchNodeFactory<TestAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> nodeFactory =
            new SearchNodeBaseFactoryImpl<>(metadataFactory);

        NodeEvaluator<TestAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> nodeEvaluator =
            new MonteCarloEvaluator<>(nodeFactory, random, rewardAggregator, 1.0, 1);

        SearchTreeImpl<TestAction, DoubleReward, DoubleVector, MonteCarloTreeSearchMetadata<DoubleReward>, TestState> searchTree = new SearchTreeImpl<>(
            root,
            new Ucb1NodeSelector<>(random, 1.0),
            new MonteCarloTreeSearchUpdater<>(),
            nodeEvaluator
        );

        for (int i = 0; i < 100; i++) {
            searchTree.updateTree();
        }

        System.out.println("asdf");


    }

}
