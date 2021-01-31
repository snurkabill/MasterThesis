package vahy.multirewardAggregation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.RiskStateWrapper;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.benchmark.RiskEpisodeStatistics;
import vahy.benchmark.RiskEpisodeStatisticsCalculator;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHConfigBuilder;
import vahy.examples.simplifiedHallway.SHInstance;
import vahy.examples.simplifiedHallway.SHRiskInstanceSupplier;
import vahy.examples.simplifiedHallway.SHRiskState;
import vahy.impl.RoundBuilder;
import vahy.impl.episode.DataPointGeneratorGeneric;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.earlyStoppingStrategies.AlwaysFalseStoppingStrategy;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.RandomizedValuePolicy;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;
import vahy.test.ConvergenceAssert;
import vahy.utils.JUnitParameterizedTestHelper;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SHMultirewardTest {

    private static final Logger logger = LoggerFactory.getLogger(SHMultirewardTest.class);

    private PolicyDefinition<SHAction, DoubleVector, SHRiskState> getPlayer(double riskAllowed, Supplier<Double> temperatureSupplier) {

        var policyId = 1;
        var categoryId = 1;
        var discountFactor = 1.0;
        var queueSize = 100;
        var riskDecayFactor = 0.9999;
        System.out.println(queueSize + riskDecayFactor + riskAllowed + temperatureSupplier.get());

        var dataAggregator = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var dataMaker = new MultirewardDataMaker<SHAction, SHRiskState>(discountFactor, policyId, dataAggregator, riskAllowed, queueSize, riskDecayFactor);
//        var dataMaker = new ValueDataMaker<SHAction, SHRiskState>(discountFactor, policyId, dataAggregator);

        var predictor = new DataTablePredictorWithLr(new double[]{0.0}, 0.10);
//        var predictor = new DataTablePredictor(new double[]{0.0});
        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHRiskState>(
            policyId,
            predictor,
            dataMaker,
            dataAggregator
        );

        return new PolicyDefinition<>(
            policyId,
            categoryId,
            (initialState, policyMode, policyId1, random) -> {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new RandomizedValuePolicy<>(random.split(), policyId, predictor, 0.0, 1.0, false);
                }
                return new RandomizedValuePolicy<>(random.split(), policyId, predictor, 1.0, temperatureSupplier.get(), false);
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
//                Arguments.of(0.0, 0.0),
//                Arguments.of(0.0, 0.1),
//                Arguments.of(0.0, 0.5),
//                Arguments.of(0.0, 1.0),


//                Arguments.of(1.0, 0.0),
//                Arguments.of(1.0, 0.1),
//                Arguments.of(1.0, 0.5),
//                Arguments.of(1.0, 1.0),

                Arguments.of(0.1, 0.0),
                Arguments.of(0.1, 0.1),
                Arguments.of(0.1, 0.5)
//                Arguments.of(0.1, 1.0)
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

        var algorithmConfig = new CommonAlgorithmConfigBase(3000, 10);

        Supplier<Double> temperatureSupplier = new Supplier<>() {
            private int callCount = 0;
            @Override
            public Double get() {
                callCount++;
                return Math.exp(-callCount / 5000.0);
            }
        };

        var player = getPlayer(riskAllowed, temperatureSupplier);

        var additionalStatistics = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("Risk Hit Ratio", x -> StreamUtils.labelWrapperFunction(x.getRiskHitRatio()));
        var additionalStatistics2 = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("Exhausted Risk in Index avg", x -> StreamUtils.labelWrapperFunction(x.getRiskExhaustedIndexAverage()));
        var additionalStatistics3 = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("At the end threshold avg", x -> StreamUtils.labelWrapperFunction(x.getRiskThresholdAtEndAverage()));

        var roundBuilder = RoundBuilder.getRoundBuilder(
            "PaperSH03Test",
            config,
            systemConfig,
            algorithmConfig,
            List.of(player),
            List.of(additionalStatistics, additionalStatistics2, additionalStatistics3),
            SHRiskInstanceSupplier::new,
            RiskStateWrapper::new,
            new RiskEpisodeStatisticsCalculator<>(),
            new EpisodeResultsFactoryBase<>()
        );
        var result = roundBuilder.execute();

        var stats = result.getEvaluationStatistics();
        logger.info("Reward: [{}], risk[{}]", stats.getTotalPayoffAverage().get(player.getPolicyId()), stats.getRiskHitRatio().get(player.getPolicyId()));

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
                return Math.exp(-callCount / 10000.0) * 10;
            }
        };

        var player = getPlayer(riskAllowed, temperatureSupplier);
        var policyArgumentsList = List.of(player);

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHRiskState, RiskEpisodeStatistics>()
            .setRoundName("SH05Test")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((config_, splittableRandom_) -> policyMode -> new SHRiskInstanceSupplier(config_, splittableRandom_).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList)
            .setStatisticsCalculator(new RiskEpisodeStatisticsCalculator<>())
            .setStateStateWrapperInitializer(RiskStateWrapper::new)
            .setPlayerPolicySupplierList(policyArgumentsList)
            .setEarlyStoppingStrategy(new AlwaysFalseStoppingStrategy<SHAction, DoubleVector, SHRiskState, RiskEpisodeStatistics>());
        var result = roundBuilder.execute();

        assertConvergenceResult(expectedPayoffMax, expectedPayoffMin, result.getEvaluationStatistics().getTotalPayoffAverage().get(player.getPolicyId()));
    }

    @ParameterizedTest(name = "Trap probability {0}, allowedRisk {1} with seed {2}")
    @MethodSource("SHTest12Params")
    public void convergence12Test(double trapProbability, double riskAllowed, long seed) {
        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_12)
            .maximalStepCountBound(200)
            .stepPenalty(15)
            .trapProbability(trapProbability)
            .buildConfig();

        var algorithmConfig = new CommonAlgorithmConfigBase(10000, 100);

        var systemConfig = new SystemConfig(
            seed,
            false,
            ConvergenceAssert.TEST_THREAD_COUNT,
            true,
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
                var x = Math.exp(-callCount / 100000.0) * 10;
                if(callCount % 1000 == 0) {
                    logger.warn("Temperature: [{}]", x);
                }
                return x;
            }
        };

        var player = getPlayer(riskAllowed, temperatureSupplier);
        var policyArgumentsList = List.of(player);

        var additionalStatistics = new DataPointGeneratorGeneric<RiskEpisodeStatistics>("Risk Hit Ratio", x -> StreamUtils.labelWrapperFunction(x.getRiskHitRatio()));

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHRiskState, RiskEpisodeStatistics>()
            .setRoundName("SH12Test")
            .setAdditionalDataPointGeneratorListSupplier(List.of(additionalStatistics))
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((config_, splittableRandom_) -> policyMode -> new SHRiskInstanceSupplier(config_, splittableRandom_).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList)
            .setStatisticsCalculator(new RiskEpisodeStatisticsCalculator<>())
            .setStateStateWrapperInitializer(RiskStateWrapper::new)
            .setPlayerPolicySupplierList(policyArgumentsList)
            .setEarlyStoppingStrategy(new AlwaysFalseStoppingStrategy<SHAction, DoubleVector, SHRiskState, RiskEpisodeStatistics>());
        var result = roundBuilder.execute();

        var stats = result.getEvaluationStatistics();
        System.out.println("Trap prob: [" + trapProbability + "] Reward: [" + stats.getTotalPayoffAverage().get(player.getPolicyId()) + "], risk[" + stats.getRiskHitRatio().get(player.getPolicyId()) + "]");
    }
}
