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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class PatrollingExample02 {


    private PatrollingExample02() {

    }

    public static void main(String[] args) {

        var systemConfig = new SystemConfig(987568, false, 7, false, 100000, 0, false, false, false, Path.of("TEST_PATH"), null);

        var algorithmConfig = new CommonAlgorithmConfigBase(100, 1000);

        var discountFactor = 1.0;

        var defenderLookbackSize = 2;
        var attackerLookbackSize = 2;

        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.1);
        var dataAggregator = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, 0, defenderLookbackSize, dataAggregator);

        var predictor = new PredictorTrainingSetup<>(0, trainablePredictor, episodeDataMaker, dataAggregator);

        var supplier = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
            @Override
            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 0.0);
                }
                return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 0.1);
            };
        };


        var trainablePredictor2 = new DataTablePredictorWithLr(new double[] {0.0}, 0.1);
        var dataAggregator2 = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker2 = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, 1, attackerLookbackSize, dataAggregator2);

        var predictor2 = new PredictorTrainingSetup<>(1, trainablePredictor2, episodeDataMaker2, dataAggregator2);

        var supplier2 = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
            @Override
            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.0);
                }
                return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.1);
            };
        };

//        var attackLength = 1;
        var attackLength = 7;

        var graph = new boolean[9][];

        graph[0] = new boolean[] {true, true, false, false, true, false, false, false, false};
        graph[1] = new boolean[] {true, true, true, false, false, false, false, false, false};
        graph[2] = new boolean[] {false, true, true, true, false, false, false, false, false};
        graph[3] = new boolean[] {false, false, true, true, true, false, false, false, false};

        graph[4] = new boolean[] {true, false, false, true, true, true, false, false, true};

        graph[5] = new boolean[] {false, false, false, false, true, true, true, false, false};
        graph[6] = new boolean[] {false, false, false, false, false, true, true, true, false};
        graph[7] = new boolean[] {false, false, false, false, false, false, true, true, true};
        graph[8] = new boolean[] {false, false, false, false, true, false, false, true, true};


        var moveCostMatrix = new int[graph.length][];
        for (int i = 0; i < moveCostMatrix.length; i++) {
            moveCostMatrix[i] = new int[graph[i].length];
            Arrays.fill(moveCostMatrix[i], 1);
        }
        var isTargetSet = new HashSet<Integer>();
        var attackLengthMap = new HashMap<Integer, Integer>();
        var attackCostMap = new HashMap<Integer, Integer>();

        for (int i = 0; i < graph.length; i++) {
            isTargetSet.add(i);
            attackLengthMap.put(i, attackLength);
            attackCostMap.put(i, 1);
        }

        var graphDef = new GraphDef(graph, moveCostMatrix, isTargetSet, attackLengthMap, attackCostMap);

        var patrollingConfig = new PatrollingConfig(1000, false, 0, 2, List.of(new PolicyCategoryInfo(false, 1, 2)), PolicyShuffleStrategy.NO_SHUFFLE, graphDef);

        var policyArgumentsList = List.of(

//            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(0, 1, (state, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId), List.of()),
            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(0, 1, defenderLookbackSize, supplier, List.of(predictor)),


//            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(1, 1, (state, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId), List.of())
            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(1, 1, attackerLookbackSize, supplier2, List.of(predictor2))
        );

        var roundBuilder = RoundBuilder.getRoundBuilder("Patrolling", patrollingConfig, systemConfig, algorithmConfig, policyArgumentsList, PatrollingInitializer::new);
        var result = roundBuilder.execute();

        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(0);
        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(1);

        System.out.println("Defender: " + playerOneResult);
        System.out.println("Attacker: " + playerTwoResult);

    }


}
