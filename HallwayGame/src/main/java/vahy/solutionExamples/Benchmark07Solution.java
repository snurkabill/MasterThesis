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
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;
import vahy.riskBasedSearch.SelectorType;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.io.IOException;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class Benchmark07Solution {

    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        //  EXAMPLE 1
        ImmutableTuple<GameConfig, ExperimentSetup> setup = createExperiment1();
        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
        new Experiment().prepareAndRun(setup, random);


        // EXAMPLE 2
//        ImmutableTuple<GameConfig, ExperimentSetup> setup = createExperiment2();
//        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
//        new Experiment().prepareAndRun(setup, random);


    }

    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment1() {
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.4)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_07)
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(0)
            .trainingEpochCount(0)
            // REINFORCEMENT
            .discountFactor(1)

            .batchEpisodeCount(100)
            .stageCount(100)

            .maximalStepCountBound(1000)
            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)
            .learningRate(0.0001)

            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(1000)
            .globalRiskAllowed(0.00)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
//                     return Math.exp(-callCount / 2000.0) / 2;
                    return 0.2;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
//                    return Math.exp(-callCount / 2000.0) * 3;
                    return 2.0;
                }
            })
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }


    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment2() {
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.4)
            .stepPenalty(1)
            .trapProbability(1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_07)
            //MCTS
            .cpuctParameter(3)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(0)
            .trainingEpochCount(0)
            // REINFORCEMENT
            .discountFactor(1)

            .batchEpisodeCount(100)
            .stageCount(100)

            .maximalStepCountBound(1000)
            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP)

            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(10000)
            .globalRiskAllowed(0.25)
            .explorationConstantSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    return 0.0;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                @Override
                public Double get() {
                    return 0.0;
                }
            })
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }


}
