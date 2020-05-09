package vahy.impl.search.integration;

import org.testng.annotations.Test;
import vahy.api.policy.PolicyMode;
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
import vahy.impl.testdomain.tictactoe.TicTacToeAction;
import vahy.impl.testdomain.tictactoe.TicTacToeConfig;
import vahy.impl.testdomain.tictactoe.TicTacToeState;
import vahy.impl.testdomain.tictactoe.TicTacToeStateInitializer;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SplittableRandom;

public class MCTSIntegrationTest {

    @Test
    public void testMCTSOnTicTacToe() {
        SplittableRandom random = new SplittableRandom(0);

        TicTacToeStateInitializer initializer = new TicTacToeStateInitializer(new TicTacToeConfig(), random);
        var state = initializer.createInitialState(PolicyMode.INFERENCE);

        SearchNode<TicTacToeAction, DoubleVector, TicTacToeState, MonteCarloTreeSearchMetadata, TicTacToeState> root = new SearchNodeImpl<>(
            state,
            new MonteCarloTreeSearchMetadata(0.0, 0.0, 0.0),
            new LinkedHashMap<>()
        );

        SearchNodeMetadataFactory<TicTacToeAction, DoubleVector, TicTacToeState, MonteCarloTreeSearchMetadata, TicTacToeState> metadataFactory = new MonteCarloTreeSearchMetadataFactory<>();
        SearchNodeFactory<TicTacToeAction, DoubleVector, TicTacToeState, MonteCarloTreeSearchMetadata, TicTacToeState> nodeFactory = new SearchNodeBaseFactoryImpl<>(TicTacToeAction.class, metadataFactory);

        NodeEvaluator<TicTacToeAction, DoubleVector, TicTacToeState, MonteCarloTreeSearchMetadata, TicTacToeState> nodeEvaluator = new MonteCarloEvaluator<>(nodeFactory, random, 1.0, 10);
        SearchTreeImpl<TicTacToeAction, DoubleVector, TicTacToeState, MonteCarloTreeSearchMetadata, TicTacToeState> searchTree = new SearchTreeImpl<>(
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

//    @Test
//    public void testMCTSAlgorithm() {
//
//        SplittableRandom random = new SplittableRandom(0);
//
//        SearchNode<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> root = new SearchNodeImpl<>(
//            new TestState(Arrays.asList('Z')),
//            new MonteCarloTreeSearchMetadata(0.0, 0.0, 0.0),
//            new LinkedHashMap<>()
//        );
//
//        SearchNodeMetadataFactory<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> metadataFactory =
//            new MonteCarloTreeSearchMetadataFactory<>();
//        SearchNodeFactory<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> nodeFactory =
//            new SearchNodeBaseFactoryImpl<>(TestAction.class, metadataFactory);
//
//        NodeEvaluator<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> nodeEvaluator =
//            new MonteCarloEvaluator<>(nodeFactory, random, 1.0, 1);
//
//        SearchTreeImpl<TestAction, DoubleVector, TestState, MonteCarloTreeSearchMetadata, TestState> searchTree = new SearchTreeImpl<>(
//            root,
//            new Ucb1NodeSelector<>(random, 1.0),
//            new MonteCarloTreeSearchUpdater<>(),
//            nodeEvaluator
//        );
//
//        for (int i = 0; i < 100; i++) {
//            searchTree.updateTree();
//        }
//
//        System.out.println("asdf");
//
//
//    }

}
