package vahy.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyMode;
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
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ReproducibilityTest {

    private static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createUniformPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId),
            new ArrayList<>(0)
        );
    }

    private static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createAtMiddlePolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtMiddlePolicy(random, policyId),
            new ArrayList<>(0)
        );
    }

    private static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createAtCornerPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtCornerPolicy(random, policyId),
            new ArrayList<>(0)
        );
    }

    private static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createTrainablePolicy(int policyId_) {
        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictor(new double[]{0.0});
        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState>(discountFactor, policyId_);

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            policyId_,
            trainablePredictor,
            episodeDataMaker,
            new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>())
        );
        return new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<>(random.split(), policyId, trainablePredictor, 0.0);
                }
                return new ValuePolicy<>(random.split(), policyId, trainablePredictor, 0.1);
            },
            List.of(predictorTrainingSetup)
        );
    }

    private static Stream<Arguments> params() {
        return Stream.of(
            Arguments.of((Supplier<Object>)() -> createUniformPolicy(0), (Supplier<Object>)() -> createUniformPolicy(1)),
            Arguments.of((Supplier<Object>)() -> createUniformPolicy(0), (Supplier<Object>)() -> createAtMiddlePolicy(1)),
            Arguments.of((Supplier<Object>)() -> createAtMiddlePolicy(0), (Supplier<Object>)() -> createUniformPolicy(1)),
            Arguments.of((Supplier<Object>)() -> createAtMiddlePolicy(0), (Supplier<Object>)() -> createTrainablePolicy(1)),
            Arguments.of((Supplier<Object>)() -> createTrainablePolicy(0), (Supplier<Object>)() -> createTrainablePolicy(1))
        );
    }

    private Double calculateResults(PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> playerOne,
                                    PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> playerTwo,
                                    long seed) {
        var ticTacConfig = new TicTacToeConfig();
        var systemConfig = new SystemConfig(
            seed,
            false,
            Runtime.getRuntime().availableProcessors() - 1,
            false,
            1_000,
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
                return 50;
            }

            @Override
            public int getStageCount() {
                return 50;
            }
        };


        var policyArgumentsList = List.of(
            playerOne,
            playerTwo
        );

        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, EpisodeStatisticsBase>()
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
        return playerOneResult;
    }


    @ParameterizedTest
    @MethodSource("params")
    public void reproducibilityTest(Supplier<PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState>> playerOne,
                                    Supplier<PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState>> playerTwo) {

        var seedStream = StreamUtils.getSeedStream(10);
        var trialCount = 3;

        var list = new ArrayList<Double>();
        for (Long seed : (Iterable<Long>)seedStream::iterator) {
            double result = calculateResults(playerOne.get(), playerTwo.get(), seed);
            for (int i = 0; i < trialCount; i++) {
                double tmp = calculateResults(playerOne.get(), playerTwo.get(), seed);
                assertEquals(result, tmp, Math.pow(10, -10));
            }
            list.add(result);
        }
        assertNotEquals(1, list.stream().distinct().count());
    }

}
