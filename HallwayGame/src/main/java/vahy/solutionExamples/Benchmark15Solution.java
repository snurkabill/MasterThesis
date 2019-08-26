package vahy.solutionExamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class Benchmark15Solution {

    private static Logger logger = LoggerFactory.getLogger(Benchmark14Solution.class.getName());

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
            .noisyMoveProbability(0.1)
            .stepPenalty(1)
            .trapProbability(0.1)
            .stateRepresentation(StateRepresentation.COMPACT)
            .buildConfig();

        int batchSize = 100;

        ExperimentSetup experimentSetup = new ExperimentSetupBuilder()
            .randomSeed(0)
            .hallwayInstance(HallwayInstance.BENCHMARK_15)
            //MCTS
            .cpuctParameter(1)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(100))
            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(64)
            .trainingEpochCount(100)
            .learningRate(0.1)
            // REINFORCEMENTs
            .discountFactor(1)
            .batchEpisodeCount(batchSize)
            .stageCount(3000)

            .maximalStepCountBound(1000)

            .trainerAlgorithm(TrainerAlgorithm.EVERY_VISIT_MC)
            .approximatorType(ApproximatorType.HASHMAP_LR)
            .replayBufferSize(20000)
            .selectorType(SelectorType.UCB)
            .evalEpisodeCount(1000)
            .globalRiskAllowed(1.0)
            .explorationConstantSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    var x = Math.exp(-callCount / 100000.0);
                    if(callCount % batchSize == 0) {
                        logger.info("Exploration constant: [{}] in call: [{}]", x, callCount);
                    }
                    return x;
//                    return 1.0;
                }
            })
            .temperatureSupplier(new Supplier<>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    double x = Math.exp(-callCount / 200000.0) * 10;
                    if(callCount % batchSize == 0) {
                        logger.info("Temperature constant: [{}] in call: [{}]", x, callCount);
                    }
                    return x;
//                    return 1.5;
                }
            })
            .riskSupplier(() -> 1.0)
            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildExperimentSetup();
        return new ImmutableTuple<>(gameConfig, experimentSetup);
    }

}
