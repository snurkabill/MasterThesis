package vahy.integration.mcts;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
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
import vahy.impl.policy.ValuePolicyDefinitionSupplier;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictor;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

public class BaseMctsTest {

    @Test
    public void baseMctsTest() {

        var ticTacConfig = new TicTacToeConfig();
        var systemConfig = new SystemConfig(
            987568,
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

        var actionClass = TicTacToeAction.class;
        var discountFactor = 1.0;
        var rolloutCount = 1;
        var treeExpansionCount = 10;
        var cpuct = 1.0;

        var mctsPolicySupplier = new MCTSPolicyDefinitionSupplier<TicTacToeAction, TicTacToeState>(actionClass, 2, ticTacConfig);
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<TicTacToeAction, TicTacToeState>();

        var playerOneSupplier = mctsPolicySupplier.getPolicyDefinition(0, 1, cpuct, treeExpansionCount, 1.0, rolloutCount);

        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState>(discountFactor, 1);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var playerTwoSupplier =  valuePolicySupplier.getPolicyDefinition(1, 1, () -> 0.2, predictorTrainingSetup);

        var policyArgumentsList = List.of(
            playerOneSupplier,
            playerTwoSupplier
        );


        var roundBuilder = new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, EpisodeStatisticsBase>()
            .setRoundName("TicTacToeIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(ticTacConfig)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((ticTacToeConfig_, splittableRandom_) -> policyMode -> new TicTacToeStateInitializer(ticTacToeConfig_, splittableRandom_).createInitialState(policyMode))
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        System.out.println("Static policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(0));
        System.out.println("Trainable policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(1));

        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) < result.getEvaluationStatistics().getTotalPayoffAverage().get(1));
        assertEquals(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) + result.getEvaluationStatistics().getTotalPayoffAverage().get(1), 0.0, Math.pow(10, -10));
        assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(1) > 0.3);
    }



}
