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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class PatrollingExample_london_06 {


    private PatrollingExample_london_06() {

    }

    public static void main(String[] args) {

        var systemConfig = new SystemConfig(987568, false, 7, true, 10_000, 1000, true, false, false, Path.of("TEST_PATH"), null);

        var algorithmConfig = new CommonAlgorithmConfigBase(10000, 1000);

        var discountFactor = 1.0;

        var defenderLookbackSize = 1;
        var attackerLookbackSize = 1;

        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.001);
        var dataAggregator = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, 0, defenderLookbackSize, dataAggregator);

        var predictor = new PredictorTrainingSetup<>(0, trainablePredictor, episodeDataMaker, dataAggregator);

        var supplier = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
            @Override
            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 0.0);
                }
                return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 0.01);
            };
        };


        var trainablePredictor2 = new DataTablePredictorWithLr(new double[] {0.0}, 0.001);
        var dataAggregator2 = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker2 = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, 1, attackerLookbackSize, dataAggregator2);

        var predictor2 = new PredictorTrainingSetup<>(1, trainablePredictor2, episodeDataMaker2, dataAggregator2);

        var supplier2 = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
            @Override
            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.0);
                }
                return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.01);
            };
        };

//        var attackLength = 1;
//        var attackLength = 7;

        var nodeCount = 6;

        var moveCostMatrix = new double[][] {
            new double[] {-100.0, 1622.34, 2206.24, 3431.17, 2758.20, 1084.68},
            new double[] {1622.34, -100.0, 583.91, 1808.83, 1135.86, 816.23},
            new double[] {2206.24, 583.91, -100.0, 1224.93, 551.96, 1400.14},
            new double[] {3431.17, 1808.83, 1224.93, -100.0, 706.35, 2625.06},
            new double[] {2758.20, 1135.86, 551.96, 706.35, -100-.0, 1952.09},
            new double[] {1084.68, 816.23, 1400.14, 2625.06, 1952.09, -100.0}
        };

        var graph = new boolean[moveCostMatrix.length][];

        for (int i = 0; i < moveCostMatrix.length; i++) {
            graph[i] = new boolean[moveCostMatrix.length];
            for (int j = 0; j < moveCostMatrix.length; j++) {
                graph[i][j] = moveCostMatrix[i][j] >= 0;
            }
        }

        var isTargetSet = new HashSet<Integer>();
        var attackLengthMap = new HashMap<Integer, Double>();
        var attackCostMap = new HashMap<Integer, Double>();

        var givenCosts = List.of(470.0, 470.0, 330.0, 400.0, 459.99999999999994, 509.99999999999994);
        var givenAttackLengths = List.of(5025.0, 5025.0, 5025.0, 5025.0, 5025.0, 5025.0);

        for (int i = 0; i < graph.length; i++) {
            isTargetSet.add(i);
            attackLengthMap.put(i, givenAttackLengths.get(i));
            attackCostMap.put(i, givenCosts.get(i));
        }

        var graphDef = new GraphDef(graph, moveCostMatrix, isTargetSet, attackLengthMap, attackCostMap);

        var patrollingConfig = new PatrollingConfig(1000, false, 0, 2, List.of(new PolicyCategoryInfo(false, 1, 2)), PolicyShuffleStrategy.NO_SHUFFLE, graphDef);

        var policyArgumentsList = List.of(

//            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(0, 1, (state, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId), List.of()),
            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(0, 1, defenderLookbackSize, supplier, List.of(predictor)),


//            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(1, 1, (state, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId), List.of())
            new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(1, 1, attackerLookbackSize, supplier2, List.of(predictor2))
        );



//        var statsCalculator = new EpisodeStatisticsCalculatorBase<>();
//
//
//
//        var additionalDataPointGeneratorList = new ArrayList<DataPointGeneratorGeneric<EpisodeStatistics>>();
//        additionalDataPointGeneratorList.add(new DataPointGeneratorGeneric<>("Win ratio", episodeStatistics -> episodeStatistics.));



        var roundBuilder = RoundBuilder.getRoundBuilder("Patrolling", patrollingConfig, systemConfig, algorithmConfig, policyArgumentsList, PatrollingInitializer::new);
        var result = roundBuilder.execute();

        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(0);
        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(1);

        System.out.println("Defender: " + playerOneResult);
        System.out.println("Attacker: " + playerTwoResult);


    }


}
