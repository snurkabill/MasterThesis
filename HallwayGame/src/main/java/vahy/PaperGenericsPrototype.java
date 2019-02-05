package vahy;

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
import vahy.riskBasedSearch.SelectorType;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.io.IOException;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class PaperGenericsPrototype {



    public static void main(String[] args) throws NotValidGameStringRepresentationException, IOException {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        ImmutableTuple<GameConfig, ExperimentSetup> setup = createExperiment();
        SplittableRandom random = new SplittableRandom(setup.getSecond().getRandomSeed());
        new Experiment().prepareAndRun(setup, random);
    }

    public static ImmutableTuple<GameConfig, ExperimentSetup> createExperiment() {
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(1)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_03)
            //MCTS
            .cpuctParameter(2)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(4)
            .trainingEpochCount(300)
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(10)
            .stageCount(100)
            .maximalStepCountBound(1000)
            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .selectorType(SelectorType.UCB)
            // .replayBufferSize(200)
            .evalEpisodeCount(1000)
            .globalRiskAllowed(0.15)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
//                 callCount++;
//                 return Math.exp(-callCount / 500.0) / 2;
                    return 0.2;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
//                callCount++;
//                 return Math.exp(-callCount / 2500.0) * 2;
                    return 2.0;
                }
            })
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }




}
