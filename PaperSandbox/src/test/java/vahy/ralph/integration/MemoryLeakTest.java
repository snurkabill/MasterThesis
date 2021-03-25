package vahy.ralph.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.RiskStateWrapper;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.benchmark.RiskEpisodeStatisticsCalculator;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManRiskInstanceInitializer;
import vahy.examples.bomberman.BomberManRiskState;
import vahy.impl.RoundBuilder;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.ralph.RalphTreeUpdater;
import vahy.ralph.evaluator.RalphNodeEvaluator;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.metadata.RiskSearchMetadataFactory;
import vahy.ralph.policy.RalphPolicy;
import vahy.ralph.policy.RiskAverseSearchTree;
import vahy.ralph.policy.flowOptimizer.FlowOptimizerType;
import vahy.ralph.policy.linearProgram.NoiseStrategy;
import vahy.ralph.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.ralph.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.ralph.reinforcement.RalphDataTablePredictorWithLr;
import vahy.ralph.reinforcement.learning.RalphEpisodeDataMaker_V2;
import vahy.ralph.selector.RalphNodeSelector;
import vahy.test.ConvergenceAssert;
import vahy.utils.StreamUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryLeakTest {

    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState> getRiskPolicy(BomberManConfig config, int policyId) {
        var actionClass = BomberManAction.class;
        var totalActionCount = actionClass.getEnumConstants().length;
        var discountFactor = 1.0;
        var treeExpansionCount = 10;

        var risk = 0.5;
        var riskDecay = 1.0;

        var instance = new BomberManRiskInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = instance.getTotalEntityCount();


        var environmentPolicyCount = config.getEnvironmentPolicyCount();
        var finalPolicyId = policyId + environmentPolicyCount;

        var defaultPrediction = new double[totalEntityCount * 2 + totalActionCount];
        for (int i = totalEntityCount * 2; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / totalActionCount;
        }

        var trainablePredictor = new RalphDataTablePredictorWithLr(defaultPrediction, 0.01, totalActionCount, totalEntityCount);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new RalphEpisodeDataMaker_V2<BomberManAction, BomberManRiskState>(finalPolicyId, totalActionCount, discountFactor, dataAggregator);

        var predictorTrainingSetup = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManRiskState>(
            finalPolicyId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var metadataFactory = new RiskSearchMetadataFactory<BomberManAction, DoubleVector, BomberManRiskState>(actionClass, totalEntityCount);
        var searchNodeFactory = new SearchNodeBaseFactoryImpl<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(actionClass, metadataFactory);

        Supplier<Double> explorationSupplier = () -> 0.05;
        Supplier<Double> temperatureSupplier = () -> 10.0;
        Supplier<Double> trainingRiskSupplier = () -> risk;

        var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeExpansionCount);

        var strategiesProvider = new StrategiesProvider<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
            actionClass,
            InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
            InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
            ExplorationExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
            FlowOptimizerType.HARD_HARD,
            SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
            NoiseStrategy.NOISY_10_11);

        var updater = new RalphTreeUpdater<BomberManAction, DoubleVector, BomberManRiskState>();
        var nodeEvaluator = new RalphNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor, config.isModelKnown());

        return new PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>(
            finalPolicyId,
            1,
            (initialState_, policyMode_, policyId_, random_) -> {
                Supplier<RalphNodeSelector<BomberManAction, DoubleVector, BomberManRiskState>> nodeSelectorSupplier = () -> new RalphNodeSelector<BomberManAction, DoubleVector, BomberManRiskState>(random_, config.isModelKnown(), 1.0, totalActionCount);
                var node = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
                return switch (policyMode_) {
                    case INFERENCE -> new RalphPolicy<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                        policyId_,
                        random_,
                        treeUpdateConditionFactory.create(),
                        new RiskAverseSearchTree<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(searchNodeFactory, node, nodeSelectorSupplier.get(), updater, nodeEvaluator, random_, risk, riskDecay, strategiesProvider));
                    case TRAINING -> new RalphPolicy<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                        policyId_,
                        random_,
                        treeUpdateConditionFactory.create(),
                        new RiskAverseSearchTree<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(searchNodeFactory, node, nodeSelectorSupplier.get(), updater, nodeEvaluator, random_, trainingRiskSupplier.get(), riskDecay, strategiesProvider),
                        explorationSupplier.get(),
                        temperatureSupplier.get());
                };
            },
            List.of(predictorTrainingSetup)
        );
    }


    protected static Stream<Arguments> params() throws IOException, InvalidInstanceSetupException {
        var config = new BomberManConfig(20, true, 100, 1, 0, 3, 3, 1, 2, 0.1, BomberManInstance.BM_01, PolicyShuffleStrategy.NO_SHUFFLE);

        return Stream.of(
            Arguments.of((Supplier<Object>)() -> getRiskPolicy(config, 0), (Supplier<Object>)() -> getRiskPolicy(config, 1), config)
        );
    }

    private List<Double> calculateResults(PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState> playerOne,
                                    PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState> playerTwo,
                                    BomberManConfig config,
                                    long seed) throws IOException, InvalidInstanceSetupException {
        var systemConfig = new SystemConfig(
            seed,
            false,
            20,
            false,
            1_000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"));

        var algorithmConfig = new CommonAlgorithmConfigBase(2, 20);

        var policyArgumentsList = List.of(
            playerOne,
            playerTwo
        );

        var roundBuilder = RoundBuilder.getRoundBuilder(
            "BomberManRisk01",
            config,
            systemConfig,
            algorithmConfig,
            policyArgumentsList,
            null,
            BomberManRiskInstanceInitializer::new,
            RiskStateWrapper::new,
            new RiskEpisodeStatisticsCalculator<>(),
            new EpisodeResultsFactoryBase<>()
        );
        var result = roundBuilder.execute();

        return result.getEvaluationStatistics().getTotalPayoffAverage();
    }


    @ParameterizedTest
    @MethodSource("params")
    public void memoryLeakTest(Supplier<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> playerOne,
                               Supplier<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> playerTwo,
                               BomberManConfig config) {

        var start = System.currentTimeMillis();

        var seedStream = StreamUtils.getSeedStream(5);
        var trialCount = 2;

        var list = new ArrayList<List<Double>>();
        try {
            for (Long seed : (Iterable<Long>)seedStream::iterator) {
                var result = calculateResults(playerOne.get(), playerTwo.get(), config, seed);
                for (int i = 0; i < trialCount; i++) {
                    var tmp = calculateResults(playerOne.get(), playerTwo.get(), config, seed);
                    for (int j = 0; j < tmp.size(); j++) {
                        assertEquals(result.get(j), tmp.get(j), ConvergenceAssert.TEST_CONVERGENCE_ASSERT_TOLERANCE);
                    }
                    System.out.println("Seed: [" + seed + "], trial: [" + i + "]");
                    System.out.println(tmp.stream().map(Object::toString).collect(Collectors.joining(", ")));
                }
                list.add(result);
            }
//            for (int i = 0; i < list.size(); i++) {
//                assertNotEquals(1, list.stream().distinct().count());
//            }
//            System.in.read();
        } catch (InvalidInstanceSetupException | IOException e) {
            fail(e);
        }

        var end = System.currentTimeMillis();
        System.out.println(end - start);

    }

}
