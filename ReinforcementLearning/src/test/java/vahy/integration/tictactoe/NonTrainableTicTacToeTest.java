package vahy.integration.tictactoe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.examples.tictactoe.TicTacToeAction;
import vahy.examples.tictactoe.TicTacToeConfig;
import vahy.examples.tictactoe.TicTacToeState;
import vahy.examples.tictactoe.TicTacToeStateInitializer;
import vahy.impl.RoundBuilder;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;
import vahy.test.ConvergenceAssert;
import vahy.utils.JUnitParameterizedTestHelper;
import vahy.utils.StreamUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class NonTrainableTicTacToeTest extends  AbstractTicTacToeConvergenceTest {

    protected static Stream<Arguments> params() {
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
            StreamUtils.getSeedStream(10)
        );
    }

    @ParameterizedTest(name = "Policy_0 {0} vs policy_1 {1} with expected diff: {2} and seed {3}")
    @MethodSource("params")
    public void nonTrainablePoliciesTest(PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> playerOne,
                                         PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> playerTwo,
                                         double maximalExpectedPlayerOneDiffFromZeroBound,
                                         long seed)
    {
        var systemConfig = new SystemConfig(
            seed,
            false,
            ConvergenceAssert.TEST_THREAD_COUNT,
            false,
            20_000,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"));

        var algorithmConfig = new CommonAlgorithmConfigBase(0, 0);
        var policyArgumentsList = List.of(playerOne, playerTwo);
        var roundBuilder = RoundBuilder.getRoundBuilder("TicTacToeTest", new TicTacToeConfig(), systemConfig, algorithmConfig, policyArgumentsList, TicTacToeStateInitializer::new);
        var result = roundBuilder.execute();

        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(0);
        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(1);

        assertEquals(0.0, playerOneResult + playerTwoResult, ConvergenceAssert.TEST_CONVERGENCE_ASSERT_TOLERANCE);
        ConvergenceAssert.assertConvergenceResult(0.0, maximalExpectedPlayerOneDiffFromZeroBound, Math.abs(playerOneResult), "PlayerOneResult");
    }
}
