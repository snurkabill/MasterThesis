package vahy.solutionExamples;

import vahy.api.episode.TrainerAlgorithm;
import vahy.data.HallwayInstance;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.StateRepresentation;
import vahy.experiment.Experiment;
import vahy.experiment.ExperimentSetup;
import vahy.experiment.ExperimentSetupBuilder;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;
import vahy.riskBasedSearch.SelectorType;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.io.IOException;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class Benchmark13Solution {

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        //  EXAMPLE 1
        ImmutableTuple<GameConfig, ExperimentSetup> setup = createExperiment1();
        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
        new Experiment().prepareAndRun(setup, random);

        //  EXAMPLE 2
//        ImmutableTuple<GameConfig, ExperimentSetup> setup = createExperiment2();
//        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
//        new Experiment().prepareAndRun(setup, random);


    }


    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment1() {
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(2)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_13)
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(500))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(64)
            .trainingEpochCount(1)
            .learningRate(0.01)
            // REINFORCEMENTs
            .discountFactor(1)
            .batchEpisodeCount(10)
            .stageCount(200)

            .maximalStepCountBound(500)

            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP_LR)
            .replayBufferSize(20000)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(1000)
            .globalRiskAllowed(0.2)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 10000.0) / 5;
//                    return 0.1;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 10000.0) * 4;
//                    return 1.5;
                }
            })
            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
            .setFlowOptimizerType(FlowOptimizerType.HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }


    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment2() { // really quite working with commit "finally"
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(2)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_13)
            //MCTS
            .cpuctParameter(5)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(400))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(64)
            .trainingEpochCount(1)
            // REINFORCEMENTs
            .discountFactor(1)
            .batchEpisodeCount(10)
            .stageCount(1000)

            .maximalStepCountBound(1000)

            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)
            .replayBufferSize(20000)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(1000)
            .globalRiskAllowed(0.2)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                 return Math.exp(-callCount / 1000.0);
//                    return 0.1;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                return Math.exp(-callCount / 1000.0) * 3;
//                    return 1.5;
                }
            })
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }
}
