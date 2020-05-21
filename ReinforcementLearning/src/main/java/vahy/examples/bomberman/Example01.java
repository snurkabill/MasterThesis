package vahy.examples.bomberman;

import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.api.policy.PolicySupplier;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicySupplier;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyArguments;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Example01 {

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException {

        var config = new BomberManConfig(500, true, 100, 1, 2, 2, 3, 3, 3, 0.2, BomberManInstance.BM_01);
        var sampleState = (new BomberManInstanceInitializer(config, new SplittableRandom())).createInitialState(PolicyMode.TRAINING);
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
                return 1;
            }

            @Override
            public int getStageCount() {
                return 1000;
            }
        };

        double discountFactor = 1;
        var initializingRandom = new SplittableRandom(systemConfig.getRandomSeed());
        var envPolicySuppliers = createEnvironmentPolicySuppliers(config, initializingRandom.split());

        var trainablePredictor = new DataTablePredictor(new double[] {0.0});
        var episodeDataMaker = new ValueDataMaker<BomberManAction, BomberManState, PolicyRecordBase>(discountFactor, sampleState.getInGameEntityObservation(3).getObservedVector().length, 1);
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictorTrainingSetup = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
            3,
            trainablePredictor,
            episodeDataMaker,
            dataAggregator
        );

        var playerThreeSupplier = new PolicyArguments<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
            3,
            "Policy_3",
            new PolicySupplier<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>() {

                private final SplittableRandom random = initializingRandom.split();

                @Override
                public Policy<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase> initializePolicy(BomberManState initialState, PolicyMode policyMode) {
                    if(policyMode == PolicyMode.INFERENCE) {
                        return new ValuePolicy<>(random.split(), 3, trainablePredictor, 0.0);
                    }
                    return new ValuePolicy<>(random.split(), 3, trainablePredictor, 0.5);
                }
            },
            List.of(predictorTrainingSetup)
        );

        var playerFourSupplier = new PolicyArguments<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
            4,
            "Policy_4",
            new PolicySupplier<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>() {

                private final SplittableRandom random = initializingRandom.split();

                @Override
                public Policy<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase> initializePolicy(BomberManState initialState, PolicyMode policyMode) {
                    if(policyMode == PolicyMode.INFERENCE) {
                        return new ValuePolicy<>(random.split(), 4, trainablePredictor, 0.0);
                    }
                    return new ValuePolicy<>(random.split(), 4, trainablePredictor, 0.5);
                }
            },
            List.of(predictorTrainingSetup)
        );


        var playerFifthSupplier = new PolicyArguments<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
            5,
            "Policy_5",
            new PolicySupplier<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>() {

                private final SplittableRandom random = initializingRandom.split();

                @Override
                public Policy<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase> initializePolicy(BomberManState initialState, PolicyMode policyMode) {
                    if(policyMode == PolicyMode.INFERENCE) {
                        return new ValuePolicy<>(random.split(), 5, trainablePredictor, 0.0);
                    }
                    return new ValuePolicy<>(random.split(), 5, trainablePredictor, 0.5);
                }
            },
            List.of(predictorTrainingSetup)
        );

        var policyArgumentsList = new ArrayList<>(envPolicySuppliers);
        policyArgumentsList.add(playerThreeSupplier);
        policyArgumentsList.add(playerFourSupplier);
        policyArgumentsList.add(playerFifthSupplier);

        var roundBuilder = new RoundBuilder<BomberManConfig, BomberManAction, BomberManState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("BomberManIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((BomberManConfig, splittableRandom) -> policyMode -> (new BomberManInstanceInitializer(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        System.out.println(result.getEvaluationStatistics().getTotalPayoffAverage().get(1));

    }

    private static List<PolicyArguments<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>> createEnvironmentPolicySuppliers(ProblemConfig config, SplittableRandom random) {
        return IntStream.range(0, config.getEnvironmentPolicyCount())
            .mapToObj(x ->
                new PolicyArguments<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
                    x,
                    "EnvironmentPolicy_" + x,
                    new KnownModelPolicySupplier<>(random.split(), x),
                    new ArrayList<>()
                ))
            .collect(Collectors.toList());
    }


}
