package vahy.mcts;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
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
import vahy.impl.policy.MCTSPolicy;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.MCTS.MonteCarloEvaluator;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadataFactory;
import vahy.impl.search.MCTS.MonteCarloTreeSearchUpdater;
import vahy.impl.search.MCTS.Ucb1NodeSelector;
import vahy.impl.search.node.SearchNodeImpl;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.tree.treeUpdateCondition.TreeUpdateConditionSuplierCountBased;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;

public class BaseMctsTest {

    @Test
    public void baseMctsTest() {

        var ticTacConfig = new TicTacToeConfig();
//        var systemConfig = new SystemConfig(987568, true, Runtime.getRuntime().availableProcessors() - 1, false, 10000, 0, false, false, false, Path.of("TEST_PATH"), null);
        var systemConfig = new SystemConfig(987568, false, 10, false, 10000, 0, false, false, false, Path.of("TEST_PATH"), null);

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

        var playerOneSupplier = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            0,
            1,
            (initialState, policyMode, policyId, random) -> {

                var actionClass = TicTacToeAction.class;
                var nodeMetadataFactory = new MonteCarloTreeSearchMetadataFactory<TicTacToeAction, DoubleVector, TicTacToeState>();
                var searchNodeFactory = new SearchNodeBaseFactoryImpl<>(actionClass, nodeMetadataFactory);
                var root = new SearchNodeImpl<>(initialState, nodeMetadataFactory.createEmptyNodeMetadata(initialState.getTotalEntityCount()), new EnumMap<>(actionClass));

                return new MCTSPolicy<>(policyId, random, new TreeUpdateConditionSuplierCountBased(10),
                    new SearchTreeImpl<>(
                        root,
                        new Ucb1NodeSelector<>(random, 1.0, actionClass.getEnumConstants().length),
                        new MonteCarloTreeSearchUpdater<>(),
                        new MonteCarloEvaluator<>(searchNodeFactory, random, 1.0, 1)
                    ));

            },
            new ArrayList<>()
        );

        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<TicTacToeAction, TicTacToeState, PolicyRecordBase>(discountFactor, 1);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var playerTwoSupplier = new PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState, PolicyRecordBase>(
            1,
            1,
            (initialState, policyMode, policyId, random) -> {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<>(random.split(), policyId, trainablePredictor, 0.0);
                }
                return new ValuePolicy<>(random.split(), policyId, trainablePredictor, 0.2);
            },
            List.of(predictorTrainingSetup)
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
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        System.out.println("Static policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(0));
        System.out.println("Trainable policy result: " + result.getEvaluationStatistics().getTotalPayoffAverage().get(1));

        Assert.assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) < result.getEvaluationStatistics().getTotalPayoffAverage().get(1));
        Assert.assertEquals(result.getEvaluationStatistics().getTotalPayoffAverage().get(0) + result.getEvaluationStatistics().getTotalPayoffAverage().get(1), 0.0, Math.pow(10, -10));
        Assert.assertTrue(result.getEvaluationStatistics().getTotalPayoffAverage().get(1) > 0.8);
    }
}
