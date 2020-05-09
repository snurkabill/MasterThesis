package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.policy.PolicySupplier;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.runner.EpisodeWriter;
import vahy.impl.runner.EvaluationArguments;
import vahy.impl.runner.PolicyArguments;
import vahy.impl.runner.Runner;
import vahy.impl.runner.RunnerArguments;
import vahy.impl.testdomain.tictactoe.AlwaysStartAtMiddlePolicy;
import vahy.impl.testdomain.tictactoe.TicTacToeAction;
import vahy.impl.testdomain.tictactoe.TicTacToeConfig;
import vahy.impl.testdomain.tictactoe.TicTacToeState;
import vahy.impl.testdomain.tictactoe.TicTacToeStateInitializer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class TicTacToeIntegrationTest {

    @Test
    public void emptyDomainIntegrationTest() throws IOException {

        var ticTacConfig = new TicTacToeConfig();

        var systemConfig = new SystemConfig(987568, true, 1, false, 10000, 0, false, false, false, Path.of("TEST_PATH"), null);

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
            public String getAlgorithmName() {
                return "DUMMY_ALGO_TEST";
            }

            @Override
            public int getBatchEpisodeCount() {
                return 100;
            }

            @Override
            public int getStageCount() {
                return 100;
            }
        };

        var playerOneSupplier = new PolicyArguments<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            0,
            "Policy_0",
            new PolicySupplier<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>() {
                @Override
                public Policy<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> initializePolicy(TicTacToeState initialState, PolicyMode policyMode) {
                    return new UniformRandomWalkPolicy<>(new SplittableRandom(), 0);
                }
            },
            new ArrayList<>()
        );
        var playerTwoSupplier = new PolicyArguments<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            1,
            "Policy_1",
            new PolicySupplier<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>() {
                @Override
                public Policy<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase> initializePolicy(TicTacToeState initialState, PolicyMode policyMode) {
                    return new AlwaysStartAtMiddlePolicy(new SplittableRandom(), 1);
                }
            },
            new ArrayList<>()
        );

        var policyArgumentsList = List.of(
            playerOneSupplier,
            playerTwoSupplier
        );

        InitialStateSupplier<TicTacToeAction, DoubleVector, TicTacToeState> initailStateSupplier = policyMode -> (new TicTacToeStateInitializer(ticTacConfig, new SplittableRandom())).createInitialState(policyMode);

        var episodeResultsFactory = new EpisodeResultsFactoryBase<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>();
        var statisticsCalculator = new EpisodeStatisticsCalculatorBase<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>();
        var episodeWriter = new EpisodeWriter<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(ticTacConfig, algorithmConfig, systemConfig, LocalDateTime.now().toString(), "dummy_name");

        var runnerArguments = new RunnerArguments<TicTacToeConfig, TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>(
            "Test",
            ticTacConfig,
            systemConfig,
            algorithmConfig,
            initailStateSupplier,
            episodeResultsFactory,
            statisticsCalculator,
            null,
            episodeWriter,
            policyArgumentsList
        );

        var evaluationArguments = new EvaluationArguments<TicTacToeConfig, TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>(
            "Test_eval",
            ticTacConfig,
            systemConfig,
            initailStateSupplier,
            episodeResultsFactory,
            statisticsCalculator,
            episodeWriter
        );

        var runner = new Runner<TicTacToeConfig, TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>();
        var result = runner.run(runnerArguments, evaluationArguments);

        Assert.assertTrue(result.getEpisodeStatistics().getAveragePlayerStepCount().get(0) < result.getEpisodeStatistics().getAveragePlayerStepCount().get(1));
        Assert.assertEquals(result.getEpisodeStatistics().getTotalPayoffAverage().get(0) + result.getEpisodeStatistics().getTotalPayoffAverage().get(1), 0.0, Math.pow(10, -10));
        Assert.assertTrue(result.getEpisodeStatistics().getTotalPayoffAverage().get(0) < result.getEpisodeStatistics().getTotalPayoffAverage().get(1));
    }
}
