package vahy.examples.simplifiedHallway;

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
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicySupplier;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyArguments;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class Example01 {

    public static void main(String[] args) {

        var config = new SHConfigBuilder()
            .isModelKnown(true)
            .reward(100)
            .gameStringRepresentation(SHInstance.BENCHMARK_03)
            .maximalStepCountBound(500)
            .stepPenalty(10)
            .trapProbability(1.00)
            .buildConfig();

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
                return 1000;
            }
        };


        var playerOneSupplier = new PolicyArguments<SHAction, DoubleVector, SHState, PolicyRecordBase>(
            0,
            "Policy_0",
            new KnownModelPolicySupplier<>(new SplittableRandom(), 0),
            new ArrayList<>()
        );

        double discountFactor = 1;

        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<SHAction, SHState, PolicyRecordBase>(discountFactor, SHState.playerActionArray.length, 1);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<SHAction, DoubleVector, SHState, PolicyRecordBase>(
            1,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var playerTwoSupplier = new PolicyArguments<SHAction, DoubleVector, SHState, PolicyRecordBase>(
            1,
            "Policy_1",
            new PolicySupplier<SHAction, DoubleVector, SHState, PolicyRecordBase>() {

                private final SplittableRandom random = new SplittableRandom(0);

                @Override
                public Policy<SHAction, DoubleVector, SHState, PolicyRecordBase> initializePolicy(SHState initialState, PolicyMode policyMode) {
                    if(policyMode == PolicyMode.INFERENCE) {
                        return new ValuePolicy<>(random.split(), 1, trainablePredictor, 0.0);
                    }
                    return new ValuePolicy<>(random.split(), 1, trainablePredictor, 0.5);
                }
            },
            List.of(predictorTrainingSetup)
        );

        var policyArgumentsList = List.of(
            playerOneSupplier,
            playerTwoSupplier
        );

        var roundBuilder = new RoundBuilder<SHConfig, SHAction, SHState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("SHIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        System.out.println(result.getEvaluationStatistics().getTotalPayoffAverage().get(1));

    }

}
