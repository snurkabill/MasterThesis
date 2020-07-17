package vahy.integration.tictactoe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.policy.PolicyMode;
import vahy.examples.tictactoe.TicTacToeAction;
import vahy.examples.tictactoe.TicTacToeConfig;
import vahy.examples.tictactoe.TicTacToeState;
import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.utils.JUnitParameterizedTestHelper;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TrainableTicTacToeTest extends AbstractTicTacToeConvergenceTest {

    private static Stream<Arguments> params() {
        return JUnitParameterizedTestHelper.cartesian(
            JUnitParameterizedTestHelper.cartesian(
                Stream.of(
                    Arguments.of(createUniformPolicy(0), -0.82),
                    Arguments.of(createAtMiddlePolicy(0), -0.83),
                    Arguments.of(createAtCornerPolicy(0), -0.83)
                ),
                Stream.of(
                    (Supplier<DataAggregator>) () -> new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>()),
                    (Supplier<DataAggregator>) () -> new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>())
                )
            ),
            StreamUtils.getSeedStream(5)
        );
    }

    @ParameterizedTest(name = "Policy {0} with expected diff: {1} using data aggregator: {2} and seed {3}")
    @MethodSource("params")
    public void nonTrainablePoliciesTest(PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> playerOne,
                                         double maximalPlayerOneScoreBound,
                                         Supplier<DataAggregator> dataAggregatorSupplier,
                                         long seed)
    {
        var systemConfig = new SystemConfig(
            seed,
            false,
            TEST_THREAD_COUNT,
            false,
            20_000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var algorithmConfig = new CommonAlgorithmConfigBase(200, 200);

        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState>(1, 1);

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregatorSupplier.get()
        );

        var playerTwo = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState>(
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

        var policyArgumentsList = List.of(playerOne, playerTwo);
        var roundBuilder = getRoundBuilder(new TicTacToeConfig(), systemConfig, algorithmConfig, policyArgumentsList);
        var result = roundBuilder.execute();

        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(0);
        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(1);

        assertEquals(0.0, playerOneResult + playerTwoResult, TEST_CONVERGENCE_ASSERT_TOLERANCE);
        assertConvergenceResult(-1.0, maximalPlayerOneScoreBound, playerOneResult, "PlayerOneResult");
    }
}
