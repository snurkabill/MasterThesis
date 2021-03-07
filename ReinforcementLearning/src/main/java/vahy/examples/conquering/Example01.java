package vahy.examples.conquering;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.policy.PolicyMode;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.PolicyResults;
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
import java.util.function.Function;

public class Example01 {

    private Example01() {}

    public static void main(String[] args) throws IOException, InvalidInstanceSetupException {

        var config = new ConqueringConfig(100, PolicyShuffleStrategy.NO_SHUFFLE, 100, 1, 2, 5, 0.5);
        var systemConfig = new SystemConfig(987568, false, 7, true, 10000, 0, false, false, false, Path.of("TEST_PATH"));

        var algorithmConfig = new CommonAlgorithmConfigBase(1000, 100);

        double discountFactor = 1;

        var playerCount = config.getPlayerCount();
        var environmentPolicyCount = config.getEnvironmentPolicyCount();

        var policyArgumentsList = new ArrayList<PolicyDefinition<ConqueringAction, DoubleVector, ConqueringState>>();

        for (int i = 0; i < playerCount; i++) {
            policyArgumentsList.add(createPolicyArgument(discountFactor, i + environmentPolicyCount, 1));
        }

        var roundBuilder = RoundBuilder.getRoundBuilder("ConquerExample01", config, systemConfig, algorithmConfig, policyArgumentsList, ConqueringInitializer::new);
        var result = roundBuilder.execute();

        printProperty(result, EpisodeStatistics::getTotalPayoffAverage, "TotalPayoffAverage");

    }

    private static void printProperty(PolicyResults<ConqueringAction, DoubleVector, ConqueringState, EpisodeStatisticsBase> results, Function<EpisodeStatistics, List<Double>> getter, String propertyName) {
        List<Double> values = getter.apply(results.getEvaluationStatistics());
        System.out.println("Property: [" + propertyName + "]");
        for (int i = 0; i < values.size(); i++) {
            System.out.println("Policy" + i + ": " + values.get(i));
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
    }

    private static PolicyDefinition<ConqueringAction, DoubleVector, ConqueringState> createPolicyArgument(double discountFactor,
                                                                                                        int policyId,
                                                                                                        int categoryId) {
        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var dataMaker = new ValueDataMaker<ConqueringAction, ConqueringState>(discountFactor, policyId, dataAggregator);

        var predictor = new DataTablePredictor(new double[]{0.0});
        var predictorTrainingSetup = new PredictorTrainingSetup<ConqueringAction, DoubleVector, ConqueringState>(
            policyId,
            predictor,
            dataMaker,
            dataAggregator
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
