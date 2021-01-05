package vahy.paperGenerics.shTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHRiskInstanceSupplier;
import vahy.examples.simplifiedHallway.SHRiskState;
import vahy.impl.RoundBuilder;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.earlyStoppingStrategies.AlwaysFalseStoppingStrategy;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.paperGenerics.benchmark.PaperEpisodeStatisticsCalculator;
import vahy.paperGenerics.evaluator.PaperNodeEvaluator;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.paperGenerics.policy.PaperPolicyImpl;
import vahy.paperGenerics.policy.RiskAverseSearchTree;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.PaperDataTablePredictorWithLr;
import vahy.paperGenerics.reinforcement.learning.PaperEpisodeDataMaker_V1;
import vahy.paperGenerics.selector.PaperNodeSelector;
import vahy.test.ConvergenceAssert;
import vahy.utils.EnumUtils;
import vahy.utils.JUnitParameterizedTestHelper;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SHRiskTest {

    private PredictorTrainingSetup<SHAction, DoubleVector, SHRiskState> getTrainingSetup(int playerId, int totalEntityCount, int totalActionCount) {
        double discountFactor = 1;

        var defaultPrediction = new double[totalEntityCount * 2 + totalActionCount];
        for (int i = totalEntityCount * 2; i < defaultPrediction.length; i++) {

            defaultPrediction[i] = 1.0 / totalActionCount;
        }

        var trainablePredictor = new PaperDataTablePredictorWithLr(defaultPrediction, 0.25, totalActionCount, totalEntityCount);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new PaperEpisodeDataMaker_V1<SHAction, SHRiskState>(playerId, totalActionCount, discountFactor, dataAggregator);

        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHRiskState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );


        return predictorTrainingSetup;
    }

    private PolicyDefinition<SHAction, DoubleVector, SHRiskState> getPlayer(ProblemConfig config, int treeUpdateCount, Supplier<Double> temperatureSupplier, double riskAllowed) {

        var playerId = 1;
        var totalEntityCount = 2;
        var actionClass = SHAction.class;
        var totalActionCount = actionClass.getEnumConstants().length;
        var predictorTrainingSetup = getTrainingSetup(playerId, totalEntityCount, totalActionCount);
        var trainablePredictor = predictorTrainingSetup.getTrainablePredictor();

        var metadataFactory = new PaperMetadataFactory<SHAction, DoubleVector, SHRiskState>(actionClass, totalEntityCount);
        var searchNodeFactory = new SearchNodeBaseFactoryImpl<SHAction, DoubleVector, PaperMetadata<SHAction>, SHRiskState>(actionClass, metadataFactory);

        var totalRiskAllowedInference = riskAllowed;
        Supplier<Double> explorationSupplier = () -> 1.0;
        Supplier<Double> trainingRiskSupplier = () -> totalRiskAllowedInference;

        var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeUpdateCount);

        var strategiesProvider = new StrategiesProvider<SHAction, DoubleVector, PaperMetadata<SHAction>, SHRiskState>(
                    actionClass,
                    InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
                    InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
                    ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
                    ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
                    FlowOptimizerType.HARD_HARD,
                    SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
                    NoiseStrategy.NOISY_05_06);

        var updater = new PaperTreeUpdater<SHAction, DoubleVector, SHRiskState>();
        var nodeEvaluator = new PaperNodeEvaluator<SHAction, SHRiskState>(searchNodeFactory, trainablePredictor, config.isModelKnown());
        var cpuctParameter = 1.0;


        return new PolicyDefinition<SHAction, DoubleVector, SHRiskState>(
            playerId,
            1,
            (initialState_, policyMode_, policyId_, random_) -> {
                Supplier<PaperNodeSelector<SHAction, DoubleVector, SHRiskState>> nodeSelectorSupplier = () -> new PaperNodeSelector<>(random_, config.isModelKnown(), cpuctParameter, totalActionCount);
                var node = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
                switch(policyMode_) {
                    case INFERENCE:
                        return new PaperPolicyImpl<>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<SHAction, DoubleVector, PaperMetadata<SHAction>, SHRiskState>(
                                searchNodeFactory,
                                node,
                                nodeSelectorSupplier.get(),
                                updater,
                                nodeEvaluator,
                                random_,
                                totalRiskAllowedInference,
                                strategiesProvider));
                    case TRAINING:
                        return new PaperPolicyImpl<SHAction, DoubleVector, PaperMetadata<SHAction>, SHRiskState>(
                            policyId_,
                            random_,
                            treeUpdateConditionFactory.create(),
                            new RiskAverseSearchTree<SHAction, DoubleVector, PaperMetadata<SHAction>, SHRiskState>(
                                searchNodeFactory,
                                node,
                                nodeSelectorSupplier.get(),
                                updater,
                                nodeEvaluator,
                                random_,
                                trainingRiskSupplier.get(),
                                strategiesProvider),
                            explorationSupplier.get(),
                            temperatureSupplier.get());
                    default: throw EnumUtils.createExceptionForNotExpectedEnumValue(policyMode_);
                }
            },
            List.of(predictorTrainingSetup)
        );
    }

    protected static Stream<Arguments> SHTest03Params() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(0.0, 80.0, 80.0, 1.0),
                Arguments.of(1.0, 60.0, 60.0, 1.0),
                Arguments.of(0.05, 75.0, 76.0, 1.0),

                Arguments.of(0.0, 80.0, 80.0, 0.0),
                Arguments.of(1.0, 60.0, 60.0, 0.0),
                Arguments.of(0.05, 60.0, 60.0, 0.0),

                Arguments.of(0.0, 80.0, 80.0, 0.50),
                Arguments.of(1.0, 60.0, 60.0, 0.50),
                Arguments.of(0.1, 63.0, 65.0, 0.05)
            ),
            StreamUtils.getSeedStream(9548681, 3)
        );
    }

    protected static Stream<Arguments> SHTest05Params() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(0.0, 290.0, 290.0, 1.0),
                Arguments.of(1.0, 288.0, 290.0, 1.0),
                Arguments.of(0.5, 288.0, 290.0, 1.0),

                Arguments.of(0.0, 290.0, 290.0, 0.0),
                Arguments.of(1.0, 288.0, 290.0, 0.0),
                Arguments.of(0.5, 288.0, 290.0, 0.0),

                Arguments.of(0.0, 290.0, 290.0, 0.5),
                Arguments.of(1.0, 288.0, 290.0, 0.5),
                Arguments.of(0.5, 288.0, 290.0, 0.5)
            ),
            StreamUtils.getSeedStream(4567, 3)
        );
    }

    protected static Stream<Arguments> SHTest12Params() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(0.0, 600.0 - 120),
                Arguments.of(1.0, 600.0 - 240),
                Arguments.of(0.1, 335.0)
            ),
            StreamUtils.getSeedStream(5)
        );
    }

    public static void assertConvergenceResult(double expectedPayoffMax, double expectedPayoffMin, double actualPayoff) {
        assertTrue(expectedPayoffMin <= actualPayoff, "Expected min payoff: [" + expectedPayoffMin + "] but actual was: [" + actualPayoff + "]");
        assertTrue(expectedPayoffMax >= actualPayoff, "Expected max payoff: [" + expectedPayoffMax + "] but actual was: [" + actualPayoff + "]");
    }

    @ParameterizedTest(name = "Trap probability {0} to reach expectedPayoff in interval [{1}, {2}] under allowedRisk {3} with seed {4}")
    @MethodSource("SHTest03Params")
    public void convergence03Test(double trapProbability, double expectedPayoffMin, double expectedPayoffMax, double riskAllowed, long seed) {
        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_03)
            .maximalStepCountBound(100)
            .stepPenalty(10)
            .trapProbability(trapProbability)
            .buildConfig();

        var systemConfig = new SystemConfig(
            seed,
            false,
            ConvergenceAssert.TEST_THREAD_COUNT,
            false,
            5000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var algorithmConfig = new CommonAlgorithmConfigBase(100, 100);

        Supplier<Double> temperatureSupplier = new Supplier<>() {
            private int callCount = 0;
            @Override
            public Double get() {
                callCount++;
                return Math.exp(-callCount / 5000.0);
            }
        };

        var player = getPlayer(config, 1, temperatureSupplier, riskAllowed);

        var roundBuilder = RoundBuilder.getRoundBuilder(
            "PaperSH03Test",
            config,
            systemConfig,
            algorithmConfig,
            List.of(player),
            null,
            SHRiskInstanceSupplier::new,
            PaperStateWrapper::new,
            new PaperEpisodeStatisticsCalculator<>(),
            new EpisodeResultsFactoryBase<>()
        );
        var result = roundBuilder.execute();

        assertConvergenceResult(expectedPayoffMax, expectedPayoffMin, result.getEvaluationStatistics().getTotalPayoffAverage().get(player.getPolicyId()));
    }

    @ParameterizedTest(name = "Trap probability {0} to reach {1} max and {2} min expectedPayoff under allowedRisk {3} with seed {4}")
    @MethodSource("SHTest05Params")
    public void convergence05Test(double trapProbability, double expectedPayoffMin, double expectedPayoffMax, double riskAllowed, long seed) {
        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_05)
            .maximalStepCountBound(100)
            .stepPenalty(1)
            .trapProbability(trapProbability)
            .buildConfig();

        var algorithmConfig = new CommonAlgorithmConfigBase(100, 100);

        var systemConfig = new SystemConfig(
            seed,
            false,
            ConvergenceAssert.TEST_THREAD_COUNT,
            false,
            10000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        Supplier<Double> temperatureSupplier = new Supplier<>() {
            private int callCount = 0;
            @Override
            public Double get() {
                callCount++;
                return Math.exp(-callCount / 10000.0) * 4;
            }
        };

        var player = getPlayer(config, 1, temperatureSupplier, riskAllowed);
        var policyArgumentsList = List.of(player);

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHRiskState, PaperEpisodeStatistics>()
            .setRoundName("SH05Test")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((config_, splittableRandom_) -> policyMode -> new SHRiskInstanceSupplier(config_, splittableRandom_).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new PaperEpisodeStatisticsCalculator<>())
            .setStateStateWrapperInitializer(PaperStateWrapper::new)
            .setPlayerPolicySupplierList(policyArgumentsList)
            .setEarlyStoppingStrategy(new AlwaysFalseStoppingStrategy<SHAction, DoubleVector, SHRiskState, PaperEpisodeStatistics>());
        var result = roundBuilder.execute();

        assertConvergenceResult(expectedPayoffMax, expectedPayoffMin, result.getEvaluationStatistics().getTotalPayoffAverage().get(player.getPolicyId()));
    }


}
