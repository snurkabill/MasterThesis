package vahy.examples.patrolling;

import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.api.policy.OuterDefPolicySupplier;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.impl.RoundBuilder;
import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.learning.trainer.ValueDataMaker;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.RandomizedValuePolicy;
import vahy.impl.policy.ValuePolicy;
import vahy.impl.predictor.DataTablePredictorWithLr;
import vahy.impl.runner.PolicyDefinition;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class PatrollingExample01 {


    private PatrollingExample01() {

    }

    public static void main(String[] args) {

        var systemConfig = new SystemConfig(987568, true, 7, false, 10000, 0, false, false, false, Path.of("TEST_PATH"), null);

        var algorithmConfig = new CommonAlgorithmConfigBase(1000, 1000);

        var discountFactor = 1.0;

        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.1);
        var episodeDataMaker = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, 0);
        var dataAggregator = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictor = new PredictorTrainingSetup<>(0, trainablePredictor, episodeDataMaker, dataAggregator);



        var trainablePredictor2 = new DataTablePredictorWithLr(new double[] {0.0}, 0.1);
        var episodeDataMaker2 = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, 1);
        var dataAggregator2 = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var predictor2 = new PredictorTrainingSetup<>(0, trainablePredictor2, episodeDataMaker2, dataAggregator2);

        var supplier = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
            @Override
            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 0.0);
                }
                return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 0.1);
            };
        };

        var supplier2 = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
            @Override
            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.0);
                }
                return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.1);
            };
        };



        var attackLength = 1;
//        var attackLength = 2;

        var graph = new boolean[4][];

        graph[0] = new boolean[] {true, true, false, true};
        graph[1] = new boolean[] {true, true, true, false};
        graph[2] = new boolean[] {false, true, true, true};
        graph[3] = new boolean[] {true, false, false, true};

        var patrollingConfig = new PatrollingConfig(1000, false, 0, 2, List.of(new PolicyCategoryInfo(false, 1, 2)), PolicyShuffleStrategy.NO_SHUFFLE, graph, attackLength);

        var policyArgumentsList = List.of(

//            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(0, 1, (state, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId), List.of()),
            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(0, 1, supplier, List.of(predictor)),


//            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(1, 1, (state, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId), List.of())
            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(1, 1, supplier, List.of(predictor2))
        );

        var roundBuilder = RoundBuilder.getRoundBuilder("Patrolling", patrollingConfig, systemConfig, algorithmConfig, policyArgumentsList, PatrollingInitializer::new);
        var result = roundBuilder.execute();

        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(0);
        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(1);

        System.out.println("Defender: " + playerOneResult);
        System.out.println("Attacker: " + playerTwoResult);

    }


}
