package vahy.integration.mcts;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.utils.JUnitParameterizedTestHelper;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MCTS_10_SHTest {

    private PolicyDefinition<SHAction, DoubleVector, SHState> playerSupplier;

    @BeforeEach
    private void init() {

        var playerId = 1;
        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictorWithLr(new double[]{0.0, 0.0}, 0.1);
        var episodeDataMaker = new VectorValueDataMaker<SHAction, SHState>(discountFactor, playerId);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHState>(
            playerId,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        Supplier<Double> explorationSupplier = () -> 0.5;

        playerSupplier = new MCTSPolicyDefinitionSupplier<SHAction, SHState>(SHAction.class, 2, null).getPolicyDefinition(
            playerId,
            1,
            explorationSupplier,
            1,
            10,
            predictorTrainingSetup
        );
    }



    private static Stream<Arguments> SHTest03Params() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(0.0, 80.0),
                Arguments.of(1.0, 60.0),
                Arguments.of(0.05, 74.0)
            ),
            StreamUtils.getSeedStream(987543,5)
        );
    }

    private static Stream<Arguments> SHTest05Params() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(0.0, 290.0),
                Arguments.of(1.0, 288.0),
                Arguments.of(0.5, 288.0)
            ),
            StreamUtils.getSeedStream(9874255,5)
        );
    }

    private static Stream<Arguments> SHTest12Params() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(0.0, 600.0 - 120),
                Arguments.of(1.0, 600.0 - 240),
                Arguments.of(0.1, 335.0)
            ),
            StreamUtils.getSeedStream(5)
        );
    }

    public static void assertConvergenceResult(double expectedPayoff, double actualPayoff) {
        assertTrue(expectedPayoff <= actualPayoff, "Expected payoff: [" + expectedPayoff + "] but actual was: [" + actualPayoff + "]");
    }

    @ParameterizedTest(name = "Trap probability {0} to reach {1} expectedPayoff with seed {2}")
    @MethodSource("SHTest03Params")
    public void convergence03Test(double trapProbability, double expectedPayoff, long seed) {
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
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            10000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var algorithmConfig = new CommonAlgorithmConfigBase(10, 50);

        var policyArgumentsList = List.of(playerSupplier);

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatisticsBase>()
            .setRoundName("SH03Test")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        assertConvergenceResult(expectedPayoff, result.getEvaluationStatistics().getTotalPayoffAverage().get(playerSupplier.getPolicyId()));
    }

    @ParameterizedTest(name = "Trap probability {0} to reach {1} payoff with seed {2}")
    @MethodSource("SHTest05Params")
    public void convergence05Test(double trapProbability, double expectedPayoff, long seed) {
        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_05)
            .maximalStepCountBound(100)
            .stepPenalty(1)
            .trapProbability(trapProbability)
            .buildConfig();

        var algorithmConfig = new CommonAlgorithmConfigBase(50, 50);

        var systemConfig = new SystemConfig(
            seed,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            10000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var policyArgumentsList = List.of(playerSupplier);

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatisticsBase>()
            .setRoundName("SH05Test")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        assertConvergenceResult(expectedPayoff, result.getEvaluationStatistics().getTotalPayoffAverage().get(playerSupplier.getPolicyId()));
    }

//    @ParameterizedTest(name = "Trap probability {0} to reach {1} expectedPayoff with seed {2}")
//    @MethodSource("SHTest12Params")
//    public void convergence12Test(double trapProbability, double expectedPayoff, long seed) {
//        var config = new SHConfigBuilder()
//            .isModelKnown(true)
//            .reward(100)
//            .gameStringRepresentation(SHInstance.BENCHMARK_12)
//            .maximalStepCountBound(100)
//            .stepPenalty(10)
//            .trapProbability(trapProbability)
//            .buildConfig();
//
//        var systemConfig = new SystemConfig(
//            seed,
//            false,
//            Runtime.getRuntime().availableProcessors() - 1,
//            false,
//            10000,
//            0,
//            false,
//            false,
//            false,
//            Path.of("TEST_PATH"),
//            null);
//
//        var algorithmConfig = new CommonAlgorithmConfigBase(500, 100);
//
//        var policyArgumentsList = List.of(playerSupplier);
//
//        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatisticsBase>()
//            .setRoundName("SH03Test")
//            .setAdditionalDataPointGeneratorListSupplier(null)
//            .setCommonAlgorithmConfig(algorithmConfig)
//            .setProblemConfig(config)
//            .setSystemConfig(systemConfig)
//            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
//            .setResultsFactory(new EpisodeResultsFactoryBase<>())
//            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
//            .setStateStateWrapperInitializer(StateWrapper::new)
//            .setPlayerPolicySupplierList(policyArgumentsList);
//        var result = roundBuilder.execute();
//
//        assertConvergenceResult(expectedPayoff, result.getEvaluationStatistics().getTotalPayoffAverage().get(playerSupplier.getPolicyId()));
//    }


}
