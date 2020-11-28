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

public class PatrollingExample_london_03 {


    private PatrollingExample_london_03() {

    }

    public static void main(String[] args) {

        var systemConfig = new SystemConfig(987568, false, 6, true, 1_000, 1000, false, false, false, Path.of("TEST_PATH"), null);

        var algorithmConfig = new CommonAlgorithmConfigBase(100, 1000);

        var discountFactor = 1.0;

        var defenderLookbackSize = 3;
        var attackerLookbackSize = 3;

        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.001);
        var dataAggregator = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//        var dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
        var episodeDataMaker = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, 0, defenderLookbackSize, dataAggregator);

        var predictor = new PredictorTrainingSetup<>(0, trainablePredictor, episodeDataMaker, dataAggregator);

        var supplier = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
            @Override
            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
                if (policyMode == PolicyMode.INFERENCE) {
                    return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 0.0);
                }
                return new RandomizedValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor, 1.0);
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
                return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.1);
            };
        };

//        var supplier2 = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
//            @Override
//            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
//                return new Policy<PatrollingAction, DoubleVector, PatrollingState>() {
//                    @Override
//                    public int getPolicyId() {
//                        return 1;
//                    }
//
//                    @Override
//                    public PatrollingAction getDiscreteAction(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> gameState) {
//                        return PatrollingAction.WAIT;
//                    }
//
//                    @Override
//                    public void updateStateOnPlayedAction(PatrollingAction patrollingAction) {
//
//                    }
//
//                    @Override
//                    public PolicyRecord getPolicyRecord(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> gameState) {
//                        return null;
//                    }
//                };
//            };
//        };

//        var attackLength = 1;
//        var attackLength = 7;

        var nodeCount = 6;

        var moveCostMatrix = new double[][] {
            new double[] {1.0, 1.0, 1.0},
            new double[] {1.0, 1.0, 1.0},
            new double[] {1.0, 1.0, 1.0}
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

        var givenCosts = List.of(470.0, 330.0, 400.0);
        var givenAttackLengths = List.of(502.0, 502.0, 502.0);

        for (int i = 0; i < graph.length; i++) {
            isTargetSet.add(i);
            attackLengthMap.put(i, givenAttackLengths.get(i));
            attackCostMap.put(i, givenCosts.get(i));
        }

        var graphDef = new GraphDef(graph, moveCostMatrix, isTargetSet, attackLengthMap, attackCostMap);

        var patrollingConfig = new PatrollingConfig(10, false, 0, 2, List.of(new PolicyCategoryInfo(false, 1, 2)), PolicyShuffleStrategy.NO_SHUFFLE, graphDef);

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
