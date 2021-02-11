//package vahy.examples.patrolling;
//
//import vahy.api.episode.PolicyCategoryInfo;
//import vahy.api.episode.PolicyShuffleStrategy;
//import vahy.api.experiment.CommonAlgorithmConfigBase;
//import vahy.api.experiment.SystemConfig;
//import vahy.api.model.StateWrapper;
//import vahy.api.policy.OuterDefPolicySupplier;
//import vahy.api.policy.Policy;
//import vahy.api.policy.PolicyMode;
//import vahy.api.policy.PolicyRecord;
//import vahy.api.policy.PolicyRecordBase;
//import vahy.api.policy.RandomizedPolicy;
//import vahy.impl.RoundBuilder;
//import vahy.impl.learning.dataAggregator.EveryVisitMonteCarloDataAggregator;
//import vahy.impl.learning.trainer.PredictorTrainingSetup;
//import vahy.impl.learning.trainer.ValueDataMaker;
//import vahy.impl.model.observation.DoubleVector;
//import vahy.impl.policy.RandomizedValuePolicy;
//import vahy.impl.policy.UniformRandomWalkPolicy;
//import vahy.impl.predictor.DataTablePredictorWithLr;
//import vahy.impl.runner.PolicyDefinition;
//import vahy.tensorflow.TFHelper;
//import vahy.tensorflow.TFModelImproved;
//import vahy.utils.ImmutableQuadriple;
//import vahy.utils.ImmutableTuple;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.SplittableRandom;
//import java.util.function.Supplier;
//
//public class PatrollingExample_minimal {
//
//    private PatrollingExample_minimal() {
//    }
//
//    public static PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState> getDefenderPolicy(PatrollingConfig patrollingConfig, SystemConfig systemConfig, int defenderLookbackSize) throws IOException, InterruptedException {
//
//        double discountFactor = 1.0;
//
//        var sampleState = new PatrollingInitializer(patrollingConfig, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
//        var modelInputSize = new StateWrapper<>(PatrollingState.DEFENDER_ID, defenderLookbackSize, sampleState).getObservation().getObservedVector().length;
//
//        var path = Paths.get("PythonScripts", "tensorflow_models", "patrollingExample_london_06", "create_value_model_london_06.py");
//        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getRandomSeed(), modelInputSize, 1, 0);
//        var tfModel = new TFModelImproved(
//            modelInputSize,
//            1,
//            4096,
//            1,
//            0.8,
//            0.001,
//            tfModelAsBytes,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
//
////        var trainablePredictor = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel));
////        var dataAggregator = new ReplayBufferDataAggregator(10000);
//
//
//        var trainablePredictor = new DataTablePredictorWithLr(new double[] {0.0}, 0.0001);
//        var dataAggregator = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//
//        var episodeDataMaker = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, PatrollingState.DEFENDER_ID, defenderLookbackSize, dataAggregator);
//
//        var predictor = new PredictorTrainingSetup<>(PatrollingState.DEFENDER_ID, trainablePredictor, episodeDataMaker, dataAggregator);
//
//
////        Supplier<Double> temperatureSupplier = new Supplier<>() {
////            private int callCount = 0;
////            @Override
////            public Double get() {
////                callCount++;
////                return Math.exp(-callCount / 5000.0);
////            }
////        };
//
//
//        Supplier<Double> temperatureSupplier = () -> 0.1;
//
//        var supplier = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
//            @Override
//            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
//                if (policyMode == PolicyMode.INFERENCE) {
//                    return new RandomizedValuePolicy<>(random.split(), policyId, trainablePredictor, 0.0, true);
//                }
//                return new RandomizedValuePolicy<>(random.split(), policyId, trainablePredictor, temperatureSupplier.get(), true);
//            };
//        };
//        return new PolicyDefinition<>(PatrollingState.DEFENDER_ID, 1, defenderLookbackSize, supplier, List.of(predictor));
//    }
//
//    public static PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState> getAttackerPolicy(PatrollingConfig patrollingConfig, SystemConfig systemConfig, int attackerLookbackSize) throws IOException, InterruptedException {
//
//        double discountFactor = 1.0;
//
//        var sampleState = new PatrollingInitializer(patrollingConfig, new SplittableRandom(0)).createInitialState(PolicyMode.TRAINING);
//        var modelInputSize = new StateWrapper<>(PatrollingState.ATTACKER_ID, attackerLookbackSize, sampleState).getObservation().getObservedVector().length;
//
//        var path = Paths.get("PythonScripts", "tensorflow_models", "patrollingExample_london_06", "create_value_model_london_06.py");
//        var tfModelAsBytes = TFHelper.loadTensorFlowModel(path, systemConfig.getRandomSeed(), modelInputSize, 1, 0);
//        var tfModel = new TFModelImproved(
//            modelInputSize,
//            1,
//            4096,
//            1,
//            0.8,
//            0.001,
//            tfModelAsBytes,
//            systemConfig.getParallelThreadsCount(),
//            new SplittableRandom(systemConfig.getRandomSeed()));
//
//
////        var trainablePredictor2 = new TrainableApproximator(new TensorflowTrainablePredictor(tfModel));
////        var dataAggregator2 = new ReplayBufferDataAggregator(10000);
//
//        var trainablePredictor2 = new DataTablePredictorWithLr(new double[] {0.0}, 0.0001);
//        var dataAggregator2 = new EveryVisitMonteCarloDataAggregator(new LinkedHashMap<>());
//
//        var episodeDataMaker2 = new ValueDataMaker<PatrollingAction, PatrollingState>(discountFactor, PatrollingState.ATTACKER_ID, attackerLookbackSize, dataAggregator2);
//
//        var predictor2 = new PredictorTrainingSetup<>(PatrollingState.ATTACKER_ID, trainablePredictor2, episodeDataMaker2, dataAggregator2);
//
////        Supplier<Double> temperatureSupplier = new Supplier<>() {
////            private int callCount = 0;
////            @Override
////            public Double get() {
////                callCount++;
////                return Math.exp(-callCount / 5000.0);
////            }
////        };
//
//        Supplier<Double> temperatureSupplier = () -> 0.1;
//
//
//        var supplier2 = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
//            @Override
//            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
////                if (policyMode == PolicyMode.INFERENCE) {
////                    return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, 0.0);
////                }
////                return new ValuePolicy<PatrollingAction, PatrollingState>(random.split(), policyId, trainablePredictor2, temperatureSupplier.get());
//                return new UniformRandomWalkPolicy<>(random.split(), policyId);
//            };
//        };
//
//        return new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(PatrollingState.ATTACKER_ID, 1, attackerLookbackSize, supplier2, List.of(predictor2));
//    }
//
//
//
//
//    public static PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState> getPerfectAttackerPolicy(PatrollingConfig patrollingConfig,
//                                                                                                             SystemConfig systemConfig,
//                                                                                                             int attackerLookbackSize,
//                                                                                                             int defenderLookbackSize,
//                                                                                                             PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState> defender,
//                                                                                                             PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState> attacker,
//                                                                                                             Map<DoubleVector, Double> stateToExpectedValue) throws IOException, InterruptedException {
//
//        var supplier = new OuterDefPolicySupplier<PatrollingAction, DoubleVector, PatrollingState>() {
//            @Override
//            public Policy<PatrollingAction, DoubleVector, PatrollingState> apply(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialState, PolicyMode policyMode, int policyId, SplittableRandom random) {
//
//                if(policyMode == PolicyMode.TRAINING) {
//                    var attackerInnerPolicy = attacker.getPolicySupplierFactory().createPolicySupplier(PatrollingState.ATTACKER_ID, 1, random.split()).initializePolicy(initialState, PolicyMode.INFERENCE);
//                    return new Policy<PatrollingAction, DoubleVector, PatrollingState>() {
//
//                        @Override
//                        public int getPolicyId() {
//                            return PatrollingState.ATTACKER_ID;
//                        }
//
//                        @Override
//                        public PatrollingAction getDiscreteAction(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> gameState) {
//                            return attackerInnerPolicy.getDiscreteAction(gameState);
//                        }
//
//                        @Override
//                        public void updateStateOnPlayedAction(PatrollingAction patrollingAction) {
//                            attackerInnerPolicy.updateStateOnPlayedAction(patrollingAction);
//                        }
//
//                        @Override
//                        public PolicyRecord getPolicyRecord(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> gameState) {
//                            return attackerInnerPolicy.getPolicyRecord(gameState);
//                        }
//                    };
//                } else {
//                    final StateWrapper<PatrollingAction, DoubleVector, PatrollingState> stateWrapperForSimulatedDefender = new StateWrapper<>(PatrollingState.DEFENDER_ID, defenderLookbackSize, initialState.getWrappedState());
//
//                    var graphDef = patrollingConfig.getGraph();
//                    var attackLengths = new double[graphDef.nodeCount()];
//                    var attackCosts = new double[graphDef.nodeCount()];
//
//                    for (int i = 0; i < attackLengths.length; i++) {
//                        attackLengths[i] = graphDef.getAttackLength(i);
//                        attackCosts[i] = graphDef.getAttackCost(i);
//                    }
//
//                    return new Policy<PatrollingAction, DoubleVector, PatrollingState>() {
//
//                        private StateWrapper<PatrollingAction, DoubleVector, PatrollingState> initialEpisodeState = stateWrapperForSimulatedDefender;
//                        private StateWrapper<PatrollingAction, DoubleVector, PatrollingState> innerWrapper = stateWrapperForSimulatedDefender;
//                        private List<PatrollingAction> actionHistory = new ArrayList<>();
//
//                        @Override
//                        public int getPolicyId() {
//                            return PatrollingState.ATTACKER_ID;
//                        }
//
//                        @Override
//                        public PatrollingAction getDiscreteAction(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> gameState) {
//
//                            var defenderObservation = innerWrapper.getObservation();
//
//                            if(!stateToExpectedValue.containsKey(defenderObservation)) {
//                                var queue = new ArrayDeque<ImmutableQuadriple<StateWrapper<PatrollingAction, DoubleVector, PatrollingState>, Double, List<PatrollingAction>, Set<Integer>>>();
//                                queue.add(new ImmutableQuadriple<>(innerWrapper,0.0, new ArrayList<>(), new HashSet<>()));
//
//                                var listOfPaths = new ArrayList<ImmutableTuple<List<PatrollingAction>, PatrollingState>>();
//
//                                while(!queue.isEmpty()) {
//                                    var node = queue.pollFirst();
//                                    var stateWrapper =  node.getFirst();
//                                    var state = stateWrapper.getWrappedState();
//                                    var countDown = node.getSecond();
//                                    var pathList = node.getThird();
//
//                                    var nodeId = state.getDefenderOnId();
//
//                                    var allActions = stateWrapper.getAllPossibleActions();
//
//                                    if(allActions[allActions.length - 1] == PatrollingAction.WAIT) {
//                                        var newWrapper = stateWrapper.applyAction(PatrollingAction.WAIT).getState();
//                                        var newList = new ArrayList<>(pathList);
//                                        newList.add(PatrollingAction.WAIT);
//                                        queue.add(new ImmutableQuadriple<>(newWrapper, countDown, newList, node.getFourth()));
//                                    } else {
//                                        for (int i = 0; i < allActions.length; i++) {
//                                            var returnWrapper = stateWrapper.applyAction(allActions[i]);
//                                            var newWrapper = returnWrapper.getState();
//                                            var newNodeId = newWrapper.getWrappedState().getDefenderOnId();
//                                            var newCountDown = countDown + graphDef.getMoveCostMatrix()[nodeId][newNodeId];
//                                            var newList = new ArrayList<>(pathList);
//                                            var newSet = new HashSet<>(node.getFourth());
//                                            if(newCountDown < attackLengths[newNodeId] && !newSet.contains(newWrapper.getWrappedState().getDefenderOnId())) {
//                                                newList.add(allActions[i]);
//                                                newSet.add(newWrapper.getWrappedState().getDefenderOnId());
//                                                queue.add(new ImmutableQuadriple<>(newWrapper, newCountDown, newList, newSet));
//                                            } else {
//                                                listOfPaths.add(new ImmutableTuple<>(pathList, newWrapper.getWrappedState()));
//                                            }
//                                        }
//                                    }
//                                }
//
//                                var probabilityMap = new HashMap<Integer, List<Double>>();
//
//                                for (ImmutableTuple<List<PatrollingAction>, PatrollingState> path : listOfPaths) {
//                                    var defenderInnerPolicy = defender
//                                        .getPolicySupplierFactory()
//                                        .createPolicySupplier(PatrollingState.DEFENDER_ID, 1, random.split())
//                                        .initializePolicy(initialEpisodeState, PolicyMode.INFERENCE);
//
//                                    var state = initialEpisodeState;
//
//                                    for (PatrollingAction patrollingAction : actionHistory) {
//                                        if(state.getInGameEntityOnTurnId() == PatrollingState.DEFENDER_ID) {
//                                            defenderInnerPolicy.getDiscreteAction(state);
//                                        }
//                                        defenderInnerPolicy.updateStateOnPlayedAction(patrollingAction);
//                                        state = state.applyAction(patrollingAction).getState();
//                                    }
//
//                                    var probability = 1.0;
//
//                                    probabilityMap.computeIfAbsent(state.getWrappedState().getDefenderOnId(), integer -> new ArrayList<>(Arrays.asList(1.0)));
//
//                                    for (PatrollingAction action : path.getFirst()) {
//                                        if(state.getInGameEntityOnTurnId() == PatrollingState.DEFENDER_ID) {
//                                            defenderInnerPolicy.getDiscreteAction(state);
//                                            probability *= resolvePlayingProbability(action, state.getWrappedState(), defenderInnerPolicy.getPolicyRecord(state));
//                                            state = state.applyAction(action).getState();
//                                            probabilityMap.merge(state.getWrappedState().getDefenderOnId(), new ArrayList<>(Arrays.asList(probability)), (list, newList) -> {list.addAll(newList); return list;});
//                                        } else {
//                                            state = state.applyAction(action).getState();
//                                        }
//                                    }
//                                }
//                                System.out.println("asddf");
//                            }
//
//                            return PatrollingAction.ATTACK_000;
//                        }
//
//                        private double resolvePlayingProbability(PatrollingAction expectedAction, PatrollingState state, PolicyRecord policyRecord) {
//                            var actions = state.getAllPossibleActions(state.getInGameEntityIdOnTurn());
//                            var probabilities = policyRecord.getPolicyProbabilities();
//
//                            for (int i = 0; i < actions.length; i++) {
//                                if(actions[i] == expectedAction) {
//                                    return probabilities[i];
//                                }
//                            }
//                            throw new IllegalStateException("Unreachable");
//                        }
//
//                        @Override
//                        public void updateStateOnPlayedAction(PatrollingAction patrollingAction) {
//                            actionHistory.add(patrollingAction);
//                            var innerReturn = innerWrapper.applyAction(patrollingAction);
//                            innerWrapper = innerReturn.getState();
//                        }
//
//                        @Override
//                        public PolicyRecord getPolicyRecord(StateWrapper<PatrollingAction, DoubleVector, PatrollingState> gameState) {
//                            return new PolicyRecordBase(RandomizedPolicy.EMPTY_ARRAY, 0.0);
//                        }
//                    };
//                }
//            };
//        };
//
//
//
//        return new PolicyDefinition<PatrollingAction, DoubleVector, PatrollingState>(PatrollingState.ATTACKER_ID, 1, attackerLookbackSize, supplier, List.of());
//    }
//
//
//    public static void main(String[] args) throws IOException, InterruptedException {
//
//        var systemConfig = new SystemConfig(
//            987568,
//            false,
//            7,
//            true,
//            100_000,
//            1000,
//            true,
//            false,
//            false,
//            Path.of("TEST_PATH"),
//            System.getProperty("user.home") + "/.local/virtualenvs/tf_2_3/bin/python");
//
//        var algorithmConfig = new CommonAlgorithmConfigBase(100, 100);
//
//        var moveCostMatrix = new double[][] {
//            new double[] {-1.0, 1.0, 1.0},
//            new double[] {1.0, -1.0, 1.0},
//            new double[] {1.0, 1.0, -1.0}
//        };
//
//        var graph = new boolean[moveCostMatrix.length][];
//
//        for (int i = 0; i < moveCostMatrix.length; i++) {
//            graph[i] = new boolean[moveCostMatrix.length];
//            for (int j = 0; j < moveCostMatrix.length; j++) {
//                graph[i][j] = moveCostMatrix[i][j] >= 0;
//            }
//        }
//
//        var isTargetSet = new HashSet<Integer>();
//        var attackLengthMap = new HashMap<Integer, Double>();
//        var attackCostMap = new HashMap<Integer, Double>();
//
//        var givenCosts = List.of(1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
//        var givenAttackLengths = List.of(2.0, 2.0, 2.0, 2.0, 2.0, 2.0);
//
//        for (int i = 0; i < graph.length; i++) {
//            isTargetSet.add(i);
//            attackLengthMap.put(i, givenAttackLengths.get(i));
//            attackCostMap.put(i, givenCosts.get(i));
//        }
//
//        var graphDef = new GraphDef(graph, moveCostMatrix, isTargetSet, attackLengthMap, attackCostMap);
//        var patrollingConfig = new PatrollingConfig(300, false, 0, 2, List.of(new PolicyCategoryInfo(false, 1, 2)), PolicyShuffleStrategy.NO_SHUFFLE, graphDef);
//
//        var defenderLookbackSize = 3;
//        var attackerLookbackSize = 3;
//
//
//        var defenderPolicy = getDefenderPolicy(patrollingConfig, systemConfig, defenderLookbackSize);
//        var attackerPolicy = getAttackerPolicy(patrollingConfig, systemConfig, attackerLookbackSize);
////        var perfectAttackerPolicy = getPerfectAttackerPolicy(patrollingConfig, systemConfig, attackerLookbackSize, defenderLookbackSize, defenderPolicy, attackerPolicy, new HashMap<>());
//
//        var policyArgumentsList = List.of(
//            defenderPolicy,
//            attackerPolicy
////            perfectAttackerPolicy
//        );
//
//
////        var statsCalculator = new EpisodeStatisticsCalculatorBase<>();
////
////
////        var additionalDataPointGeneratorList = new ArrayList<DataPointGeneratorGeneric<EpisodeStatistics>>();
////        additionalDataPointGeneratorList.add(new DataPointGeneratorGeneric<>("Win ratio", episodeStatistics -> episodeStatistics.));
//
//        var roundBuilder = RoundBuilder.getRoundBuilder("Patrolling", patrollingConfig, systemConfig, algorithmConfig, policyArgumentsList, PatrollingInitializer::new);
//        var result = roundBuilder.execute();
//
//        var playerOneResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(PatrollingState.DEFENDER_ID);
//        var playerTwoResult = result.getEvaluationStatistics().getTotalPayoffAverage().get(PatrollingState.ATTACKER_ID);
//
//        System.out.println("Defender: " + playerOneResult);
//        System.out.println("Attacker: " + playerTwoResult);
//    }
//
//
//}
