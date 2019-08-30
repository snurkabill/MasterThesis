package vahy.basicExamples;

public class Demo01 {

//    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
//        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
//
//        //  EXAMPLE 1
//        ImmutableTuple<GameConfig, ExperimentSetup> setup = createExperiment1();
//        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
//        new Experiment().prepareAndRun(setup, random);
//    }
//
//    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment1() {
//        GameConfig gameConfig = new ConfigBuilder()
//            .reward(100)
//            .noisyMoveProbability(0.0)
//            .stepPenalty(1)
//            .trapProbability(0.0)
//            .stateRepresentation(StateRepresentation.COMPACT)
//            .buildConfig();
//
//        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
//            .randomSeed(0)
//            .hallwayInstance(HallwayInstance.DEMO_01)
//            //MCTS
//            .cpuctParameter(3)
//            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(20))
//            .batchEpisodeCount(100)
//            .stageCount(0)
//            .maximalStepCountBound(1000)
//            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
//            .approximatorType(ApproximatorType.HASHMAP)
//            .selectorType(SelectorType.UCB)
//            .evalEpisodeCount(1000)
//            .globalRiskAllowed(0.0)
//            .explorationConstantSupplier(new Supplier<>() {
//                @Override
//                public Double get() {
//                    return 0.0;
//                }
//            })
//            .temperatureSupplier(new Supplier<>() {
//                @Override
//                public Double get() {
//                    return 0.0;
//                }
//            })
//            .riskSupplier(() -> 0.0)
//            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
//            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
//            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
//            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
//            .setFlowOptimizerType(FlowOptimizerType.HARD)
//            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
//            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
//            .buildExperimentSetup();
//        return new ImmutableTuple<>(gameConfig, experimentSetup);
//    }

}
