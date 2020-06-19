package vahy.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.examples.tictactoe.AlwaysStartAtCornerPolicy;
import vahy.examples.tictactoe.AlwaysStartAtMiddlePolicy;
import vahy.examples.tictactoe.TicTacToeAction;
import vahy.examples.tictactoe.TicTacToeConfig;
import vahy.examples.tictactoe.TicTacToeState;
import vahy.examples.tictactoe.TicTacToeStateInitializer;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.utils.JUnitParameterizedTestHelper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TicTacToeTest {

    public static final Logger ticTacToeLogger = LoggerFactory.getLogger(TicTacToeTest.class);

    private static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> createUniformPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId),
            new ArrayList<>(0)
        );
    }

    private static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> createAtMiddlePolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtMiddlePolicy(random, policyId),
            new ArrayList<>(0)
        );
    }

    private static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> createAtCornerPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtCornerPolicy(random, policyId),
            new ArrayList<>(0)
        );
    }

    private static Stream<Long> getSeedStream(int streamSize) {
        return new Random(0).longs(streamSize).boxed();
    }

    private static Stream<Arguments> nonTrainableParams() {
        return JUnitParameterizedTestHelper.cartesian(
            Stream.of(
                Arguments.of(createUniformPolicy(0), createUniformPolicy(1), 0.02),
                Arguments.of(createUniformPolicy(0), createAtMiddlePolicy(1), 0.25),
                Arguments.of(createAtMiddlePolicy(0), createUniformPolicy(1), 0.25),
                Arguments.of(createAtMiddlePolicy(0), createAtMiddlePolicy(1), 0.02),
                Arguments.of(createUniformPolicy(0), createAtCornerPolicy(1), 0.17),
                Arguments.of(createAtMiddlePolicy(0), createAtCornerPolicy(1), 0.17),
                Arguments.of(createAtCornerPolicy(0), createAtCornerPolicy(1), 0.02)
            ),
            getSeedStream(10)
        );
    }

    private static Stream<Arguments> trainableParams() {
        return JUnitParameterizedTestHelper.cartesian(
            JUnitParameterizedTestHelper.cartesian(
                Stream.of(
                    Arguments.of(createUniformPolicy(0), -0.82),
                    Arguments.of(createAtMiddlePolicy(0), -0.84),
                    Arguments.of(createAtCornerPolicy(0), -0.84)
                ),
                Stream.of(
                    (Supplier<DataAggregator>) () -> new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>()),
                    (Supplier<DataAggregator>) () -> new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>())
                )
            ),
            getSeedStream(5)
        );
    }

    @ParameterizedTest(name = "Policy_0 {0} vs policy_1 {1} with expected diff: {2} and seed {3}")
    @MethodSource("nonTrainableParams")
    public void nonTrainablePoliciesTest(PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> playerOne,
                                         PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> playerTwo,
                                         double maximalExpectedPlayerOneDiffFromZeroBound,
                                         long seed) {

        var ticTacConfig = new TicTacToeConfig();
        var systemConfig = new SystemConfig(
            seed,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            20_000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var algorithmConfig = new CommonAlgorithmConfigBase(0, 0);

        var policyArgumentsList = List.of(playerOne, playerTwo);

        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("TicTacToeIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(ticTacConfig)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((ticTacToeConfig, splittableRandom) -> policyMode -> (new TicTacToeStateInitializer(ticTacConfig, splittableRandom)).createInitialState(policyMode))
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(0);
        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(1);

        assertEquals(playerOneResult + playerTwoResult, 0.0, Math.pow(10, -10), "Results differs from [0]");
        assertTrue(Math.abs(playerOneResult) <= maximalExpectedPlayerOneDiffFromZeroBound, "PlayerOneResult: [" + playerOneResult + "] is distant from zero than expected: [" + maximalExpectedPlayerOneDiffFromZeroBound + "]");
    }

    @ParameterizedTest(name = "Policy_0 {0} powered by data aggregator {1} with expected diff: {2} and seed {3}")
    @MethodSource("trainableParams")
    public void trainablePolicyTest(PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> playerOne,
                                    double maximalPlayerOneScoreBound,
                                    Supplier<DataAggregator> dataAggregatorSupplier,
                                    long seed) {

        var ticTacConfig = new TicTacToeConfig();
        var systemConfig = new SystemConfig(
            seed,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            10_000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var algorithmConfig = new CommonAlgorithmConfig() {

            @Override
            public String toLog() {
                return "";
            }

            @Override
            public String toFile() {
                return "";
            }

            @Override
            public int getBatchEpisodeCount() {
                return 200;
            }

            @Override
            public int getStageCount() {
                return 200;
            }
        };


        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState, PolicyRecordBase>(discountFactor, 1);

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregatorSupplier.get()
        );

        var playerTwo = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            1,
            1,
            (initialState, policyMode, policyId, random) -> {
                if(policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<>(random.split(), policyId, trainablePredictor, 0.0);
                }
                return new ValuePolicy<>(random.split(), policyId, trainablePredictor, 0.1);
            },
            List.of(predictorTrainingSetup)
        );

        var policyArgumentsList = List.of(
            playerOne,
            playerTwo
        );

        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("TicTacToeIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(ticTacConfig)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((ticTacToeConfig, splittableRandom) -> policyMode -> (new TicTacToeStateInitializer(ticTacConfig, splittableRandom)).createInitialState(policyMode))
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(0);
        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(1);

        assertEquals(playerOneResult + playerTwoResult, 0.0, Math.pow(10, -10), "Results differs from [0]");
        assertTrue(playerOneResult <= maximalPlayerOneScoreBound, "PlayerOneResult: [" + playerOneResult + "] but expected was less or equal to: [" + maximalPlayerOneScoreBound + "]");
    }
}
