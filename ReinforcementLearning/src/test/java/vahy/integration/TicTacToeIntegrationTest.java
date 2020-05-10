package vahy.integration;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.policy.PolicySupplier;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.runner.PolicyArguments;
import vahy.examples.tictactoe.AlwaysStartAtMiddlePolicy;
import vahy.examples.tictactoe.TicTacToeAction;
import vahy.examples.tictactoe.TicTacToeConfig;
import vahy.examples.tictactoe.TicTacToeState;
import vahy.examples.tictactoe.TicTacToeStateInitializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class TicTacToeIntegrationTest {

    @Test
    public void emptyDomainIntegrationTest() {

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

        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("TicTacToeIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(ticTacConfig)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((ticTacToeConfig, splittableRandom) -> policyMode -> (new TicTacToeStateInitializer(ticTacConfig, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        Assert.assertTrue(result.getEpisodeStatistics().getAveragePlayerStepCount().get(0) < result.getEpisodeStatistics().getAveragePlayerStepCount().get(1));
        Assert.assertEquals(result.getEpisodeStatistics().getTotalPayoffAverage().get(0) + result.getEpisodeStatistics().getTotalPayoffAverage().get(1), 0.0, Math.pow(10, -10));
        Assert.assertTrue(result.getEpisodeStatistics().getTotalPayoffAverage().get(0) < result.getEpisodeStatistics().getTotalPayoffAverage().get(1));
    }
}
