package vahy;

import vahy.agent.environment.RealDataMarketPolicy;
import vahy.api.experiment.StochasticStrategy;
import vahy.api.experiment.SystemConfig;
import vahy.api.experiment.SystemConfigBuilder;
import vahy.api.learning.ApproximatorType;
import vahy.api.learning.dataAggregator.DataAggregationAlgorithm;
import vahy.config.AlgorithmConfigBuilder;
import vahy.config.EvaluatorType;
import vahy.config.PaperAlgorithmConfig;
import vahy.config.SelectorType;
import vahy.environment.MarketAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketEnvironmentStaticPart;
import vahy.environment.RealMarketAction;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.PaperExperimentEntryPoint;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MarketPrototype2 {
    public static void main(String[] args) throws IOException {

        var algorithmConfig = getAlgorithmConfig();
        var systemConfig = getSystemConfig();
        MarketConfig problemConfig = getGameConfig();

        PaperExperimentEntryPoint.createExperimentAndRun(
            MarketAction.class,
            InitialMarketStateSupplier::new,
//            RealDataMarketPolicySupplier.class,
            splittableRandom -> (initialState, policyMode) -> new RealDataMarketPolicy(problemConfig.getMarketDataProvider().getMarketMovementArray(), initialState.getCurrentDataIndex() + 1),
            algorithmConfig,
            systemConfig,
            problemConfig,
            Path.of("Results")

        );
    }

     public static MarketDataProvider createMarketDataProvider(String absoluteFilePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(absoluteFilePath));
        List<Double> prices = new ArrayList<>();
        List<RealMarketAction> movements = new ArrayList<>();
        lines.forEach(x -> {
                String[] lineParts = x.split(",");
                prices.add(Double.parseDouble(lineParts[0]));
                movements.add(lineParts[1].equals("Up") ? RealMarketAction.MARKET_UP : RealMarketAction.MARKET_DOWN);
            });
        return new MarketDataProvider(movements.toArray(new RealMarketAction[0]), prices.stream().mapToDouble(value -> value).toArray());
    }

    public static double dummyDataGenerator(int i) {
        return  2* Math.sin(i/(double)10) + 3;
    }

    public static MarketDataProvider createMarketDataProvider() {
        List<Double> prices = new ArrayList<>();
        List<RealMarketAction> movements = new ArrayList<>();

        var previousValue = dummyDataGenerator(-1);
        var nextValue = dummyDataGenerator(-1);
        for (int i = 0; i < 100000; i++) {
            previousValue = nextValue;
            nextValue = dummyDataGenerator(i);
            var diff = nextValue - previousValue;
            movements.add(diff > 0.0 ? RealMarketAction.MARKET_UP : RealMarketAction.MARKET_DOWN);
            prices.add(nextValue);

        }
        return new MarketDataProvider(movements.toArray(new RealMarketAction[0]), prices.stream().mapToDouble(value -> value).toArray());
    }

    private static MarketConfig getGameConfig() throws IOException {

        double systemStopLoss = 0.005;
        double constantSpread = 0.0002;
        double priceRange     = 0.0005;
        int tradeSize  = 1;
        int commission = 0; //5 / 1_000_000;

        int lookbackLength = 30;

        var staticPart = new MarketEnvironmentStaticPart(systemStopLoss, constantSpread, priceRange, tradeSize, commission);

//        MarketDataProvider marketDataProvider = createMarketDataProvider("d:/data_for_trading_env_testing/data");
        MarketDataProvider marketDataProvider = createMarketDataProvider();
        return new MarketConfig(staticPart, lookbackLength, marketDataProvider);

    }

    private static SystemConfig getSystemConfig() {
        return new SystemConfigBuilder()
            .setRandomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(true)
            .setParallelThreadsCount(1)
            .setSingleThreadedEvaluation(false)
            .setEvalEpisodeCount(1000)
            .buildSystemConfig();
    }

    private static PaperAlgorithmConfig getAlgorithmConfig() {
        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)

            //.mcRolloutCount(1)
            //NN
            .trainingBatchSize(1)
            .trainingEpochCount(10)
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(100)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(50))
            .stageCount(200)
            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(1)
            .maximalStepCountBound(500)
            .trainerAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC)
            .replayBufferSize(100_000)
            .trainingBatchSize(1)
            .learningRate(0.01)

            .approximatorType(ApproximatorType.HASHMAP_LR)
            .globalRiskAllowed(1.00)
            .riskSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 1.00;
                }

                @Override
                public String toString() {
                    return "() -> 1.00";
                }
            })

            .replayBufferSize(10000)
            .selectorType(SelectorType.UCB)

            .explorationConstantSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 1.0;
                }

                @Override
                public String toString() {
                    return "() -> 0.20";
                }
            })
            .temperatureSupplier(new Supplier<Double>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 10000.0);
//                    return 2.00;
                }

                @Override
                public String toString() {
                    return "() -> 1.05";
                }
            })

            .setInferenceExistingFlowStrategy(InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW)
            .setInferenceNonExistingFlowStrategy(InferenceNonExistingFlowStrategy.MAX_UCB_VALUE)
            .setExplorationExistingFlowStrategy(ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE)
            .setExplorationNonExistingFlowStrategy(ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE)
            .setFlowOptimizerType(FlowOptimizerType.HARD_HARD)
            .setSubTreeRiskCalculatorTypeForKnownFlow(SubTreeRiskCalculatorType.FLOW_SUM)
            .setSubTreeRiskCalculatorTypeForUnknownFlow(SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY)
            .buildAlgorithmConfig();
    }

}

