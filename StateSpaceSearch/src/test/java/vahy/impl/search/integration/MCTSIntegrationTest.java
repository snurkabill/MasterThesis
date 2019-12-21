package vahy.impl.search.integration;

import org.testng.annotations.Test;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.api.search.nodeEvaluator.NodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.search.MCTS.MonteCarloEvaluator;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadataFactory;
import vahy.impl.search.MCTS.MonteCarloTreeSearchUpdater;
import vahy.impl.search.MCTS.ucb1.Ucb1NodeSelector;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.testdomain.model.TestAction;
import vahy.impl.testdomain.model.TestState;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class MCTSIntegrationTest {

    @Test
    public void testMCTSAlgorithm() {

        SplittableRandom random = new SplittableRandom(0);

        SearchNode<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> root = new SearchNodeImpl<>(
            new TestState(Arrays.asList('Z')),
            new MonteCarloTreeSearchMetadata(0.0, 0.0, 0.0),
            new LinkedHashMap<>()
        );

        SearchNodeMetadataFactory<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> metadataFactory =
            new MonteCarloTreeSearchMetadataFactory<>();
        SearchNodeFactory<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> nodeFactory =
            new SearchNodeBaseFactoryImpl<>(metadataFactory);

        NodeEvaluator<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> nodeEvaluator =
            new MonteCarloEvaluator<>(nodeFactory, random, 1.0, 1);

        SearchTreeImpl<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> searchTree = new SearchTreeImpl<>(
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
