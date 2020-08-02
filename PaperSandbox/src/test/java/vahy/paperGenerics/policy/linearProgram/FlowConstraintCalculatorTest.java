package vahy.paperGenerics.policy.linearProgram;

import junit.framework.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.EmptyPredictor;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.testdomain.emptySpace.EmptySpaceAction;
import vahy.impl.testdomain.emptySpace.EmptySpaceState;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.evaluator.PaperNodeEvaluator;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.paperGenerics.policy.linearProgram.deprecated.MinimalRiskReachAbilityCalculatorDeprecated;
import vahy.paperGenerics.policy.linearProgram.deprecated.OptimalFlowHardConstraintCalculatorDeprecated;
import vahy.paperGenerics.policy.linearProgram.deprecated.OptimalFlowSoftConstraintDeprecated;
import vahy.paperGenerics.policy.riskSubtree.MinimalRiskReachAbilityCalculator;
import vahy.paperGenerics.selector.PaperNodeSelector;
import vahy.paperGenerics.testDomain.EmptySpaceRiskState;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.SplittableRandom;

public class FlowConstraintCalculatorTest {

    private static final Logger logger = LoggerFactory.getLogger(FlowConstraintCalculatorTest.class.getName());
    private static final double TOLERANCE = Math.pow(10, -10);

    private static final int TREE_UPDATE_COUNT = 500;
    private static final int TREE_COUNT_PER_UPDATE = 20;


    @Test
    public void optimalFlowHardConstraintComparisonTest() {
        var random = new SplittableRandom(0);

        var risk = 0.25;
        EmptySpaceState innerState = new EmptySpaceState(true, true);

        var actionCount = EmptySpaceAction.values().length;
        var paperNodeSelector = new PaperNodeSelector<EmptySpaceAction, DoubleVector, EmptySpaceRiskState>(random.split(), true, 1.0, actionCount);

        var metadataFactory = new PaperMetadataFactory<EmptySpaceAction, DoubleVector, EmptySpaceRiskState>(EmptySpaceAction.class, innerState.getTotalEntityCount());
        var nodeFactory = new SearchNodeBaseFactoryImpl<>(EmptySpaceAction.class, metadataFactory);
        var nodeEvaluator = new PaperNodeEvaluator<>(nodeFactory, new EmptyPredictor(new double[2 * 2 + actionCount]), true);

        var noiseStrategy = NoiseStrategy.NONE;

        var total = 0;
        var sum_UCB = 0.0;
        var sum_BUILD_CALC_1 = 0.0;
        var sum_BUILD_CALC_2 = 0.0;
        var sum_BUILD_SOLVE_1 = 0.0;
        var sum_BUILD_SOLVE_2 = 0.0;

        for (int expandIterationCount = 1; expandIterationCount < TREE_UPDATE_COUNT; expandIterationCount++) {
            for (int j = 0; j < TREE_COUNT_PER_UPDATE; j++) {
                var state = new PaperStateWrapper<>(0, new EmptySpaceRiskState(new EmptySpaceState(random.nextBoolean(), random.nextBoolean()), random.split(),false, 0.05));
                var searchTree = initializeTree(paperNodeSelector, state, nodeEvaluator, nodeFactory, metadataFactory);
                sum_UCB = buildTree(sum_UCB, expandIterationCount, searchTree);

                long start = System.currentTimeMillis();
                var calculator = new OptimalFlowHardConstraintCalculator<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState>(risk, null, noiseStrategy);
                sum_BUILD_CALC_1 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                var calculator2 = new OptimalFlowHardConstraintCalculatorDeprecated<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState>(EmptySpaceAction.class, risk, null, noiseStrategy);
                sum_BUILD_CALC_2 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                boolean firstSolvable = calculator.optimizeFlow(searchTree.getRoot());
                sum_BUILD_SOLVE_1 += System.currentTimeMillis() - start;

                double[] values = fillArray(searchTree.getRoot(), actionCount);

                start = System.currentTimeMillis();
                boolean secondSolvable = calculator2.optimizeFlow(searchTree.getRoot());
                sum_BUILD_SOLVE_2 += System.currentTimeMillis() - start;

                double[] values2 = fillArray(searchTree.getRoot(), actionCount);

                assertResults(firstSolvable, values, secondSolvable, values2);

                total++;
                if (total % 100 == 0) {
                    printStatistics(total, sum_UCB, sum_BUILD_CALC_1, sum_BUILD_CALC_2, sum_BUILD_SOLVE_1, sum_BUILD_SOLVE_2, expandIterationCount, j);
                }
            }
        }
    }


    @Test
    public void optimalFlowSoftConstraintComparisonTest() {
        var random = new SplittableRandom(0);

        var risk = 1.0;
        EmptySpaceState innerState = new EmptySpaceState(true, true);

        var actionCount = EmptySpaceAction.values().length;

        var paperNodeSelector = new PaperNodeSelector<EmptySpaceAction, DoubleVector, EmptySpaceRiskState>(random.split(), true, 1.0, actionCount);
        var metadataFactory = new PaperMetadataFactory<EmptySpaceAction, DoubleVector, EmptySpaceRiskState>(EmptySpaceAction.class, innerState.getTotalEntityCount());
        var nodeFactory = new SearchNodeBaseFactoryImpl<>(EmptySpaceAction.class, metadataFactory);
        var nodeEvaluator = new PaperNodeEvaluator<>(nodeFactory, new EmptyPredictor(new double[2 * 2 + actionCount]), true);

        var noiseStrategy = NoiseStrategy.NONE;

        var total = 0;
        var sum_UCB = 0.0;
        var sum_BUILD_CALC_1 = 0.0;
        var sum_BUILD_CALC_2 = 0.0;
        var sum_BUILD_SOLVE_1 = 0.0;
        var sum_BUILD_SOLVE_2 = 0.0;

        for (int k = 1; k < TREE_UPDATE_COUNT; k++) {
            for (int j = 0; j < TREE_COUNT_PER_UPDATE; j++) {

                var state = new PaperStateWrapper<>(0, new EmptySpaceRiskState(new EmptySpaceState(random.nextBoolean(), random.nextBoolean()), random.split(),false, 0.05));
                var searchTree = initializeTree(paperNodeSelector, state, nodeEvaluator, nodeFactory, metadataFactory);
                sum_UCB = buildTree(sum_UCB, k, searchTree);

                long start = System.currentTimeMillis();
                var calculator = new OptimalFlowSoftConstraintCalculator<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState>(risk, null, noiseStrategy);
                sum_BUILD_CALC_1 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                var calculator2 = new OptimalFlowSoftConstraintDeprecated<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState>(EmptySpaceAction.class, risk, null, noiseStrategy);
                sum_BUILD_CALC_2 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                boolean firstSolvable = calculator.optimizeFlow(searchTree.getRoot());
                sum_BUILD_SOLVE_1 += System.currentTimeMillis() - start;

                double[] values = fillArray(searchTree.getRoot(), actionCount);

                start = System.currentTimeMillis();
                boolean secondSolvable = calculator2.optimizeFlow(searchTree.getRoot());
                sum_BUILD_SOLVE_2 += System.currentTimeMillis() - start;

                double[] values2 = fillArray(searchTree.getRoot(), actionCount);

                assertResults(firstSolvable, values, secondSolvable, values2);

                total++;
                if (total % 304 == 0) {
                    printStatistics(total, sum_UCB, sum_BUILD_CALC_1, sum_BUILD_CALC_2, sum_BUILD_SOLVE_1, sum_BUILD_SOLVE_2, k, j);
                }
            }
        }
    }

    @Test
    public void minimalRiskReachabilityCalculatorTest() {
        var random = new SplittableRandom(0);

        EmptySpaceState innerState = new EmptySpaceState(true, true);

        var actionCount = EmptySpaceAction.values().length;

        var paperNodeSelector = new PaperNodeSelector<EmptySpaceAction, DoubleVector, EmptySpaceRiskState>(random.split(), true, 1.0, actionCount);
        var metadataFactory = new PaperMetadataFactory<EmptySpaceAction, DoubleVector, EmptySpaceRiskState>(EmptySpaceAction.class, innerState.getTotalEntityCount());
        var nodeFactory = new SearchNodeBaseFactoryImpl<>(EmptySpaceAction.class, metadataFactory);
        var nodeEvaluator = new PaperNodeEvaluator<>(nodeFactory, new EmptyPredictor(new double[2 * 2 + actionCount]), true);

        var total = 0;
        var sum_UCB = 0.0;
        var sum_BUILD_CALC_1 = 0.0;
        var sum_BUILD_CALC_2 = 0.0;
        var sum_BUILD_SOLVE_1 = 0.0;
        var sum_BUILD_SOLVE_2 = 0.0;

        for (int k = 0; k < TREE_UPDATE_COUNT; k++) {
            for (int j = 0; j < TREE_COUNT_PER_UPDATE; j++) {
                var state = new PaperStateWrapper<>(0, new EmptySpaceRiskState(new EmptySpaceState(random.nextBoolean(), random.nextBoolean()), random.split(),false, 0.05));
                var searchTree = initializeTree(paperNodeSelector, state, nodeEvaluator, nodeFactory, metadataFactory);
                sum_UCB = buildTree(sum_UCB, k, searchTree);

//                if(j % 2 == 0) {
//                    searchTree.applyAction(EmptySpaceAction.A);
//                }


                long start = System.currentTimeMillis();
                var calculator = new MinimalRiskReachAbilityCalculator<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState>();
                sum_BUILD_CALC_1 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                double solution1 = calculator.calculateRisk(searchTree.getRoot());
                sum_BUILD_SOLVE_1 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                var calculator2 = new MinimalRiskReachAbilityCalculatorDeprecated<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState>(EmptySpaceAction.class);
                sum_BUILD_CALC_2 += System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                double solution2 = calculator2.calculateRisk(searchTree.getRoot());
                sum_BUILD_SOLVE_2 += System.currentTimeMillis() - start;

                Assert.assertEquals("Solutions are different", solution1, solution2, TOLERANCE);

                total++;
                if (total % 100 == 0) {
                    printStatistics(total, sum_UCB, sum_BUILD_CALC_1, sum_BUILD_CALC_2, sum_BUILD_SOLVE_1, sum_BUILD_SOLVE_2, k, j);
                }
            }
        }
    }

    private static double[] fillArray(SearchNode<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState> root, int actionCount) {
        double[] values = new double[actionCount];
        int index = 0;
        for (var entry : root.getChildNodeMap().entrySet()) {
            values[index] = entry.getValue().getSearchNodeMetadata().getFlow();
            index++;
        }
        return values;
    }

    private SearchTreeImpl<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState> initializeTree(
        PaperNodeSelector<EmptySpaceAction, DoubleVector, EmptySpaceRiskState> paperNodeSelector,
        PaperStateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceRiskState> state,
        PaperNodeEvaluator<EmptySpaceAction, EmptySpaceRiskState> nodeEvaluator,
        SearchNodeFactory<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState> searchNodeFactory,
        PaperMetadataFactory<EmptySpaceAction, DoubleVector, EmptySpaceRiskState> searchNodeMetadataFactory
    )
    {
        var root = new SearchNodeImpl<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState>(
            state,
            searchNodeMetadataFactory.createEmptyNodeMetadata(),
            new EnumMap<>(EmptySpaceAction.class));
        return new SearchTreeImpl<>(
            searchNodeFactory,
            root,
            paperNodeSelector,
            new PaperTreeUpdater<>(),
            nodeEvaluator
        );
    }

    private double buildTree(double sum_UCB, int expandIterationCount, SearchTreeImpl<EmptySpaceAction, DoubleVector, PaperMetadata<EmptySpaceAction>, EmptySpaceRiskState> searchTree) {
        var start = System.currentTimeMillis();
        for (int i = 0; i < expandIterationCount; i++) {
            searchTree.expandTree();
        }
        sum_UCB += System.currentTimeMillis() - start;
        return sum_UCB;
    }

    private void printStatistics(int total, double sum_UCB, double sum_BUILD_CALC_1, double sum_BUILD_CALC_2, double sum_BUILD_SOLVE_1, double sum_BUILD_SOLVE_2, int expandIterationCount, int j) {
        logger.debug("------------------------" + expandIterationCount + "------------------------");
        logger.debug("------------------------" + j + "------------------------");
        logger.debug("------------------------" + total + "------------------------");
        logger.debug("Tree update took: [{}] milliseconds", sum_UCB / total);
        logger.debug("Calculator creation took: [{}] milliseconds", sum_BUILD_CALC_1 / total);
        logger.debug("Calculator_2 creation took: [{}] milliseconds", sum_BUILD_CALC_2 / total);
        logger.debug("Optimizing FLOW_1 took: [{}] milliseconds", sum_BUILD_SOLVE_1 / total);
        logger.debug("Optimizing FLOW_2 took: [{}] milliseconds", sum_BUILD_SOLVE_2 / total);
    }

    private void assertResults(boolean firstSolvable, double[] values, boolean secondSolvable, double[] values2) {
        Assertions.assertEquals(secondSolvable, firstSolvable, "Solutions differs");
        if(firstSolvable) {
            for (int i = 0; i < 2; i++) {
                Assertions.assertEquals(values[i], values2[i], TOLERANCE, "Different results. First: " + Arrays.toString(values) + " second: " + Arrays.toString(values2)+ ".");
            }
        } else {
            logger.info("Unsolvable problem.");
        }
    }

}
