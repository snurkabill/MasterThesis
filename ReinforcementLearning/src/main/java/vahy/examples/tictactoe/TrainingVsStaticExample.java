package vahy.examples.tictactoe;

import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyRecordBase;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.runner.PolicyDefinition;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TrainingVsStaticExample {

    public static void main(String[] args) {

        var ticTacConfig = new TicTacToeConfig(3);
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

        var playerOneSupplier = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            0,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId),
            new ArrayList<>()
        );
        var playerTwoSupplier = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            1,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtMiddlePolicy(random, policyId),
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
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

    }
}
