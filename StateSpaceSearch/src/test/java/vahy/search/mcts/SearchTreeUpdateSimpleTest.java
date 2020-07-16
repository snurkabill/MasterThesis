package vahy.search.mcts;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyMode;
import vahy.api.search.node.SearchNode;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.mcts.MCTSMetadata;
import vahy.impl.policy.mcts.MCTSMetadataFactory;
import vahy.impl.policy.mcts.MCTSPredictionEvaluator;
import vahy.impl.policy.mcts.MCTSTreeUpdater;
import vahy.impl.predictor.EmptyPredictor;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;

import java.util.EnumMap;
import java.util.SplittableRandom;

public class SearchTreeUpdateSimpleTest {

    private MCTSMetadataFactory<SHAction, DoubleVector, SHState> metadataFactory;
    private SearchNodeBaseFactoryImpl<SHAction, DoubleVector, MCTSMetadata, SHState> nodeFactory;
    private MCTSTreeUpdater<SHAction, DoubleVector, SHState> updater;
    private MCTSPredictionEvaluator<SHAction, DoubleVector, MCTSMetadata, SHState> evaluator;

    @BeforeEach
    private void init() {
        metadataFactory = new MCTSMetadataFactory<SHAction, DoubleVector, SHState>(2);
        nodeFactory = new SearchNodeBaseFactoryImpl<>(SHAction.class, metadataFactory);
        updater = new MCTSTreeUpdater<SHAction, DoubleVector, SHState>();
        var emtpyPredictor = new EmptyPredictor(new double[]{0.0, 0.0});
        evaluator = new MCTSPredictionEvaluator<>(nodeFactory, emtpyPredictor);
    }

    private SHState getState(SHInstance instance, double stepPenalty, double trapProbability) {
        return new SHInstanceSupplier(
            new SHConfigBuilder()
                .isModelKnown(true)
                .reward(100)
                .gameStringRepresentation(instance)
                .maximalStepCountBound(100)
                .stepPenalty(stepPenalty)
                .trapProbability(trapProbability)
                .buildConfig(),
            new SplittableRandom(0)
        ).createInitialState(PolicyMode.INFERENCE);
    }

    private SearchNode<SHAction, DoubleVector, MCTSMetadata, SHState> applyAction(SHAction action, SearchNode<SHAction, DoubleVector, MCTSMetadata, SHState> node) {
        var stateReward = node.applyAction(action);
        var newNode = nodeFactory.createNode(stateReward, node, action);
        node.getChildNodeMap().put(action, newNode);
        return newNode;
    }

    private void assertNodeMetadata(MCTSMetadata metadata, int expectedVisitCounter, double[] expectedTotalEstimations, double[] expectedGainedReward, double[] expectedCumulativeReward, double[] expectedExpectedReward) {
        Assertions.assertEquals(expectedVisitCounter, metadata.getVisitCounter());
        Assertions.assertArrayEquals(expectedTotalEstimations, metadata.getSumOfTotalEstimations(), Math.pow(10, -10));
        Assertions.assertArrayEquals(expectedGainedReward, metadata.getGainedReward(), Math.pow(10, -10));
        Assertions.assertArrayEquals(expectedCumulativeReward, metadata.getCumulativeReward(), Math.pow(10, -10));
        Assertions.assertArrayEquals(expectedExpectedReward, metadata.getExpectedReward(), Math.pow(10, -10));
    }

    @Test
    public void SHSearchTreeZeroLevelTest() {
        var state = getState(SHInstance.TEST_00, 1, 0.1);
        var rootNode = nodeFactory.createNode(new StateWrapper<>(1, state), metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(SHAction.class));

        evaluator.evaluateNode(rootNode);

        updater.updateTree(rootNode);
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 1, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0});

        updater.updateTree(rootNode);
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 2, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0});

        updater.updateTree(rootNode);
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 3, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0});
    }

    @Test
    public void SHSearchTreeOneLevelTest() {

        var stepPenalty = 1.0;

        var state = getState(SHInstance.TEST_00, stepPenalty, 0.1);
        var rootNode = nodeFactory.createNode(new StateWrapper<>(1, state), metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(SHAction.class));

        evaluator.evaluateNode(rootNode);
        updater.updateTree(rootNode);

        var child = applyAction(SHAction.UP, rootNode);

        evaluator.evaluateNode(child);

        updater.updateTree(child);
        assertNodeMetadata(child.getSearchNodeMetadata(), 1, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 2, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty / 2.0});

        updater.updateTree(child);
        assertNodeMetadata(child.getSearchNodeMetadata(), 2, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 3, new double[]{0.0, 2 * -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, ( 2 * -stepPenalty) / 3.0});

        updater.updateTree(child);
        assertNodeMetadata(child.getSearchNodeMetadata(), 3, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 4, new double[]{0.0, 3 * -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, (3 * -stepPenalty) / 4.0});
    }

    @Test
    public void SHSearchTreeTwoLevelTest() {

        var stepPenalty = 1.0;

        var state = getState(SHInstance.TEST_00, stepPenalty, 0.1);
        var rootNode = nodeFactory.createNode(new StateWrapper<>(1, state), metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(SHAction.class));

        evaluator.evaluateNode(rootNode);
        updater.updateTree(rootNode);

        var child = applyAction(SHAction.UP, rootNode);

        evaluator.evaluateNode(child);
        updater.updateTree(child);

        var child2 = applyAction(SHAction.NO_ACTION, child);

        evaluator.evaluateNode(child2);
        updater.updateTree(child2);
        assertNodeMetadata(child2.getSearchNodeMetadata(), 1, new double[] {0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(child.getSearchNodeMetadata(), 2, new double[] {0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 3, new double[] {0.0, 2 * -stepPenalty}, new double[] {0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, (2 * -stepPenalty) / 3.0});

        updater.updateTree(child2);
        assertNodeMetadata(child2.getSearchNodeMetadata(), 2, new double[] {0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(child.getSearchNodeMetadata(), 3, new double[] {0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 4, new double[] {0.0, 3 * -stepPenalty}, new double[] {0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, (3 * -stepPenalty) / 4.0});

        updater.updateTree(child2);
        assertNodeMetadata(child2.getSearchNodeMetadata(), 3, new double[] {0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(child.getSearchNodeMetadata(), 4, new double[] {0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 5, new double[] {0.0, 4 * -stepPenalty}, new double[] {0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, (4 * -stepPenalty) / 5.0});
    }


    @Test
    public void SHSearchTreeThreeLevelTest() {

        var stepPenalty = 1.0;

        var state = getState(SHInstance.TEST_00, stepPenalty, 0.1);
        var rootNode = nodeFactory.createNode(new StateWrapper<>(1, state), metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(SHAction.class));

        evaluator.evaluateNode(rootNode);
        updater.updateTree(rootNode);

        var child = applyAction(SHAction.UP, rootNode);

        evaluator.evaluateNode(child);
        updater.updateTree(child);

        var child2 = applyAction(SHAction.NO_ACTION, child);

        evaluator.evaluateNode(child2);
        updater.updateTree(child2);

        var child3 = applyAction(SHAction.UP, child2);

        evaluator.evaluateNode(child3);

        updater.updateTree(child3);
        assertNodeMetadata(child3.getSearchNodeMetadata(), 1, new double[] {0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 2 * -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(child2.getSearchNodeMetadata(), 2, new double[] {0.0, -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, (-stepPenalty) / 2});
        assertNodeMetadata(child.getSearchNodeMetadata(), 3, new double[] {0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, (-stepPenalty) / 3});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 4, new double[] {0.0, 4 * -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, (4 * - stepPenalty) / 4} );

        updater.updateTree(child3);
        assertNodeMetadata(child3.getSearchNodeMetadata(), 2, new double[] {0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 2 * -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(child2.getSearchNodeMetadata(), 3, new double[] {0.0, 2 * -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, (2 * -stepPenalty) / 3});
        assertNodeMetadata(child.getSearchNodeMetadata(), 4, new double[] {0.0, 2 * -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, (2 * -stepPenalty) / 4});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 5, new double[] {0.0, 6 * -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, (6 * - stepPenalty) / 5} );

        updater.updateTree(child3);
        assertNodeMetadata(child3.getSearchNodeMetadata(), 3, new double[] {0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, 2 * -stepPenalty}, new double[]{0.0, 0.0});
        assertNodeMetadata(child2.getSearchNodeMetadata(), 4, new double[] {0.0, 3 * -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, -stepPenalty}, new double[]{0.0, (3 * -stepPenalty) / 4});
        assertNodeMetadata(child.getSearchNodeMetadata(), 5, new double[] {0.0, 3 * -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, -stepPenalty}, new double[]{0.0, (3 * -stepPenalty) / 5});
        assertNodeMetadata(rootNode.getSearchNodeMetadata(), 6, new double[] {0.0, 8 * -stepPenalty}, new double[]{0.0, 0.0}, new double[]{0.0, 0.0}, new double[]{0.0, (8 * - stepPenalty) / 6} );
    }


}
