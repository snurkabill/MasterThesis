package vahy;

import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfig;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.SelectorType;
import vahy.config.StochasticStrategy;
import vahy.config.SystemConfig;
import vahy.config.SystemConfigBuilder;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
import vahy.environment.state.StateRepresentation;
import vahy.experiment.Experiment;
import vahy.game.HallwayInstance;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.util.function.Supplier;

public class PaperGenericsPrototype {

    public static void main(String[] args) {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        GameConfig gameConfig = new ConfigBuilder()
            .reward(100)
            .noisyMoveProbability(0.0)
            .stepPenalty(1)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .hallwayInstance(HallwayInstance.BENCHMARK_03)
            .buildConfig();

        var setup = createExperiment();
        var experiment = new Experiment(setup.getFirst(), setup.getSecond());
        experiment.run(gameConfig);
    }

    public static ImmutableTuple<AlgorithmConfig, SystemConfig> createExperiment() {

        var systemConfig = new SystemConfigBuilder()
            .randomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(true)
            .setParallelThreadsCount(7)
            .setSingleThreadedEvaluation(true)
            .setEvalEpisodeCount(1000)
            .buildSystemConfig();


        var algorithmConfig = new AlgorithmConfigBuilder()
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
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .selectorType(SelectorType.UCB)
            // .replayBufferSize(200)
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
            .buildAlgorithmConfig();
        return new ImmutableTuple<>(algorithmConfig, systemConfig);
    }




}
