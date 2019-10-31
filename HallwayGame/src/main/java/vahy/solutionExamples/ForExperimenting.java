package vahy.solutionExamples;

import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfig;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
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
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.reinforcement.learning.ApproximatorType;
import vahy.utils.ImmutableTuple;
import vahy.utils.ThirdPartBinaryUtils;

import java.util.Arrays;
import java.util.function.Supplier;

public class ForExperimenting {

    public static void main(String[] args) {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();
        GameConfig gameConfig = new ConfigBuilder()
            .reward(100) // reward per gold
            .noisyMoveProbability(0.1) // probability of shifting agent after forward move
            .stepPenalty(1) // penalty per one agent action
            .trapProbability(1) // probability of killing the agent when on trap
            .stateRepresentation(StateRepresentation.COMPACT) // don't change this.
            .hallwayInstance(HallwayInstance.BENCHMARK_05) // experiment examples are in /resources/examples/benchmark/
            .buildConfig();

        var riskList = Arrays.asList(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0);

        for (int i = 0; i < riskList.size(); i++) {
            var setup = createExperimentSetup(riskList.get(i));
            var experiment = new Experiment(setup.getFirst(), setup.getSecond());
            experiment.run(gameConfig);
        }

    }

    public static ImmutableTuple<AlgorithmConfig, SystemConfig> createExperimentSetup(double globalRiskParameter) {

        var systemConfig = new SystemConfigBuilder()
            .randomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)  // when REPRODUCIBlE, randomSeed is used, otherwise randomSeed is randomly generated from time seed, keep as is, since linear program is not reproducible anyway ...
            .setDrawWindow(false)  // drawing progress windows during training process
            .setParallelThreadsCount(4)   // ideally count of CPU cores (or CPU cores -1 if other applications are present)
            .setSingleThreadedEvaluation(false)  // when true, only one thread is used for evaluation (set true for time measurements)
            .setEvalEpisodeCount(1000)  // how many times evaluation of trained policy is performed
            .setDumpTrainingData(true) // true for dumping training episodes as well
            .buildSystemConfig();



        var algorithmConfig = new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)  // exploration constant in UCB formula

            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(1)    // relevant only for NN predictor. keep as is
            .trainingEpochCount(10)  // relevant only for NN predictor. keep as is
            // REINFORCEMENT
            .discountFactor(1)      // not relevant. keep as is
            .batchEpisodeCount(100) // how many episodes are sampled in one training cycle
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50)) // strategy of updating search tree. FixedUpdateCountTreeConditionFactory updates search tree at each step N times. very important parameter
            .stageCount(100)       // how many training cycles are performed before evaluation
            .evaluatorType(EvaluatorType.RALF) // not relevant. keep as is
//            .setBatchedEvaluationSize(1)
            .maximalStepCountBound(500)  // count of maximum steps done per one episode
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)  // different data aggregation strategies for training. REPLAY BUFFER relevant only for NN predictor
            .replayBufferSize(100_000) // relevant only for REPLAY BUFFER data aggregation combined with NN predictor
            .learningRate(0.01)  // training speed of predictor. the higher the value, the faster predictor copies sampled data.

            .approximatorType(ApproximatorType.HASHMAP_LR)  // don't  change. very relevant. very experimental. very unstable. requires NN models as well. keep as is.

            .globalRiskAllowed(globalRiskParameter) // risk threshold. by far most important parameter. Delta in paper. when equals to 0, no risk is allowed. when equals to 1, no linear program optimization is performed.

            .selectorType(SelectorType.UCB) // not relevant. keep as is.

            .riskSupplier(new Supplier<Double>() {  // risk supplier during training. called when new policy is created to be sampled for episode. check different BenchmarkSolutions for modeling function of call count.
                @Override
                public Double get() {
                    return globalRiskParameter;
                }

                @Override
                public String toString() {
                    return "globalRiskParameter";
                }
            })


            .explorationConstantSupplier(new Supplier<Double>() {  // exploration constant supplier  during training. see other BenchmarkSolutions for example
                @Override
                public Double get() {
                    return 0.2;
                }

                @Override
                public String toString() {
                    return "() -> 0.20";
                }
            })
            .temperatureSupplier(new Supplier<Double>() {  // temperature supplier supplier  during training. see other BenchmarkSolutions for example
                @Override
                public Double get() {
                    return 1.50;
                }

                @Override
                public String toString() {
                    return "() -> 1.50";
                }
            })


            // self explanatory. in general, don't change it.
            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VISIT)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VISIT)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
        return new ImmutableTuple<>(algorithmConfig, systemConfig);
    }
}
