package vahy.impl.search.integration;

import org.testng.annotations.Test;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.factory.Ucb1SearchNodeMetadataFactory;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;
import vahy.impl.search.nodeEvaluator.OriginMonteCarloEvaluator;
import vahy.impl.search.nodeSelector.treeTraversing.ucb1.Ucb1NodeSelector;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.update.MCTSTransitionUpdater;
import vahy.impl.search.update.TraversingTreeUpdater;
import vahy.testDomain.model.TestAction;
import vahy.testDomain.model.TestState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class MCTSIntegrationTest {

    @Test
    public void testMCTSAlgorithm() {

        SplittableRandom random = new SplittableRandom(0);

        RewardAggregator<DoubleScalarReward> rewardAggregator = new DoubleScalarRewardAggregator();
        SearchNode<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState> root = new SearchNodeImpl<>(
            new TestState(new ArrayList<>('Z')),
            new MCTSNodeMetadata<>(new DoubleScalarReward(0.0), new DoubleScalarReward(0.0), new DoubleScalarReward(0.0)),
            new LinkedHashMap<>()
        );

        SearchNodeMetadataFactory<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState> metadataFactory =
            new Ucb1SearchNodeMetadataFactory<>(rewardAggregator);
        SearchNodeFactory<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState> nodeFactory =
            new SearchNodeBaseFactoryImpl<>(metadataFactory);

        NodeEvaluator<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState> nodeEvaluator =
            new OriginMonteCarloEvaluator<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState>(
                nodeFactory,
                random,
                rewardAggregator);

        SearchTreeImpl<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState> searchTree = new SearchTreeImpl<>(
            root,
            new Ucb1NodeSelector<>(random, 1.0),
            new TraversingTreeUpdater<TestAction, DoubleScalarReward, DoubleVectorialObservation, MCTSNodeMetadata<DoubleScalarReward>, TestState>(new MCTSTransitionUpdater<>()),
            nodeEvaluator
        );

        searchTree.updateTree();


    }

}
