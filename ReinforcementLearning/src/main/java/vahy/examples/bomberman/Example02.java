package vahy.examples.bomberman;

import org.jetbrains.annotations.NotNull;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.PolicySupplierImpl;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicyRecordBase;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictor;
import vahy.impl.runner.PolicyDefinition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class Example02 {

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException {

        var config = new BomberManConfig(500, true, 100, 1, 1, 4, 3, 1, 2, 0.1, BomberManInstance.BM_00);
        var systemConfig = new SystemConfig(987567, true, 7, true, 10000, 0, false, false, false, Path.of("TEST_PATH"), null);

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
                return 1000;
            }

            @Override
            public int getStageCount() {
                return 1000;
            }
        };

        double discountFactor = 1;

        var playerCount = config.getPlayerCount();
        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var policyArgumentsList = new ArrayList<PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>>();

        for (int i = 0; i < playerCount; i++) {
            policyArgumentsList.add(createPolicyArgument(discountFactor, i + environmentPolicyCount, 1));
        }
//        policyArgumentsList.add(createPolicyArgument(discountFactor, environmentPolicyCount, 1));
//        policyArgumentsList.add(new PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
//            environmentPolicyCount + 1,
//            1,
//            (policyId, categoryId, random) -> new AbstractPolicySupplier<>(policyId, categoryId, random) {
//                @Override
//                protected Policy<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase> createState_inner(StateWrapper<BomberManAction, DoubleVector, BomberManState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
//                    return new UniformRandomWalkPolicy<>(random.split(), policyId);
//                }
//            },
//            new ArrayList<>()
//        ));

        var roundBuilder = new RoundBuilder<BomberManConfig, BomberManAction, BomberManState, PolicyRecordBase, EpisodeStatisticsBase>()
            .setRoundName("BomberManIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((BomberManConfig, splittableRandom) -> policyMode -> (new BomberManInstanceInitializer(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
        var result = roundBuilder.execute();

        System.out.println(result.getEvaluationStatistics().getTotalPayoffAverage().get(1));

    }

    @NotNull
    private static PolicyDefinition<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase> createPolicyArgument(double discountFactor, int policyId, int categoryId) {
        var predictor = new DataTablePredictor(new double[]{0.0});
        var predictorTrainingSetup = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManState, PolicyRecordBase>(
            policyId,
            predictor,
            new ValueDataMaker<>(discountFactor, policyId),
            new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>())
        );

        return new PolicyDefinition<>(
            policyId,
            categoryId,
            (initialState, policyMode, policyId1, random) -> {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<>(random.split(), policyId, predictor, 0.0);
                }
                return new ValuePolicy<>(random.split(), policyId, predictor, 0.2);
            },
            List.of(predictorTrainingSetup)
        );
    }

}
