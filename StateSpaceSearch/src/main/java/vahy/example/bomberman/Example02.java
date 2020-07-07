package vahy.example.bomberman;

import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManInstanceInitializer;
import vahy.examples.bomberman.BomberManState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.learning.trainer.VectorValueDataMaker;
import vahy.impl.policy.ValuePolicyDefinitionSupplier;
import vahy.impl.policy.alphazero.AlphaZeroDataMaker_V1;
import vahy.impl.policy.alphazero.AlphaZeroDataTablePredictor;
import vahy.impl.policy.alphazero.AlphaZeroPolicyDefinitionSupplier;
import vahy.impl.policy.mcts.MCTSPolicyDefinitionSupplier;
import vahy.impl.predictor.DataTablePredictor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class Example02 {

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException {
        var config = new BomberManConfig(500, true, 100, 1, 2, 3, 3, 1, 5, 0.1, BomberManInstance.BM_01);
        var systemConfig = new SystemConfig(987567, false, 7, true, 500, 0, false, false, false, Path.of("TEST_PATH"), null);

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
                return 200;
            }
        };

        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var actionClass = BomberManAction.class;
        var discountFactor = 1.0;
        var rolloutCount = 1;
        var treeExpansionCount = 20;
        var cpuct = 1.0;

        var asdf = new BomberManInstanceInitializer(config, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
        int totalEntityCount = asdf.getTotalEntityCount();

        var mctsPolicySupplier = new MCTSPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, config.isModelKnown());
        var valuePolicySupplier = new ValuePolicyDefinitionSupplier<BomberManAction, BomberManState>();
        var alphaGoPolicySupplier = new AlphaZeroPolicyDefinitionSupplier<BomberManAction, BomberManState>(actionClass, totalEntityCount, config);

//        var mctsRolloutSupplier = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 0, 1, cpuct, treeExpansionCount, discountFactor, rolloutCount);

        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 0);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<>(
            environmentPolicyCount + 0,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );
        var valuePolicyPlayer_1 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 0, 1, () -> 0.01, predictorTrainingSetup);
    // ----------------------------------------------------------------------------------------

        var trainablePredictor2 = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker2 = new ValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 3);
        var dataAggregator2 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 3,
            trainablePredictor2,
            episodeDataMaker2,
            dataAggregator2
        );
        var valuePolicyPlayer_2 = valuePolicySupplier.getPolicyDefinition(environmentPolicyCount + 3, 1, () -> 0.01, predictorTrainingSetup2);
// ----------------------------------------------------------------------------------------


        var trainablePredictorMCTSEval_1 = new DataTablePredictor(new double[totalEntityCount]);
        var episodeDataMakerMCTSEval_1 = new VectorValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 1);
        var dataAggregatorMCTSEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_1 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 1,
            trainablePredictorMCTSEval_1,
            episodeDataMakerMCTSEval_1,
            dataAggregatorMCTSEval_1
        );
// ----------------------------------------------------------------------------------------

        var trainablePredictorMCTSEval_2 = new DataTablePredictor(new double[totalEntityCount]);
        var episodeDataMakerMCTSEval_2 = new VectorValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, environmentPolicyCount + 2);
        var dataAggregatorMCTSEval_2 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupMCTSEval_2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 2,
            trainablePredictorMCTSEval_2,
            episodeDataMakerMCTSEval_2,
            dataAggregatorMCTSEval_2
        );
// ----------------------------------------------------------------------------------------
        var mctsEvalPlayer_1 = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 1, 1, () -> 0.1, cpuct, treeExpansionCount, predictorTrainingSetupMCTSEval_1);
        var mctsEvalPlayer_2 = mctsPolicySupplier.getPolicyDefinition(environmentPolicyCount + 2, 1, () -> 0.1, cpuct, treeExpansionCount * 4, predictorTrainingSetupMCTSEval_2);

        var totalActionCount = actionClass.getEnumConstants().length;
        var defaultPrediction = new double[totalEntityCount + totalActionCount];
        for (int i = totalEntityCount; i < defaultPrediction.length; i++) {
            defaultPrediction[i] = 1.0 / (totalActionCount);
        }
        var trainablePredictorAlphaGoEval_1 = new AlphaZeroDataTablePredictor(defaultPrediction, 0.1, totalEntityCount);
        var episodeDataMakerAlphaGoEval_1 = new AlphaZeroDataMaker_V1<BomberManAction, BomberManState, PolicyRecordBase>(environmentPolicyCount + 4, totalActionCount, discountFactor);
        var dataAggregatorAlphaGoEval_1 = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetupAlphaGoEval_2 = new PredictorTrainingSetup<>(
            environmentPolicyCount + 4,
            trainablePredictorAlphaGoEval_1,
            episodeDataMakerAlphaGoEval_1,
            dataAggregatorAlphaGoEval_1
        );

        var alphaGoPlayer_1 = alphaGoPolicySupplier.getPolicyDefinition(environmentPolicyCount + 4, 1, 1, () -> 0.1, treeExpansionCount * 4, predictorTrainingSetupAlphaGoEval_2);

        var policyArgumentsList = List.of(
//            playerOneSupplier
//            mctsRolloutSupplier
            valuePolicyPlayer_1
            ,mctsEvalPlayer_1
            ,mctsEvalPlayer_2
            ,valuePolicyPlayer_2
            ,alphaGoPlayer_1
        );
        var roundBuilder = new RoundBuilder<BomberManConfig, BomberManAction, BomberManState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("BomberManIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((BomberManConfig, splittableRandom) -> policyMode -> (new BomberManInstanceInitializer(config, splittableRandom)).createInitialState(policyMode))
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        System.out.println(result.getEvaluationStatistics().getTotalPayoffAverage().get(1));


        List<Double> totalPayoffAverage = result.getEvaluationStatistics().getTotalPayoffAverage();

        for (int i = 0; i < totalPayoffAverage.size(); i++) {
            System.out.println("Policy" + i + " result: " + totalPayoffAverage.get(i));
        }

    }

}
