package vahy;

import vahy.api.experiment.ApproximatorConfigBuilder;
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
import vahy.environment.MarketProbabilities;
import vahy.environment.MarketState;
import vahy.environment.RealMarketAction;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.paperGenerics.PaperExperimentBuilder;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MarketPrototype2 {
    public static void main(String[] args) throws IOException {

        var algorithmConfig = getAlgorithmConfig();
        var systemConfig = getSystemConfig();
        MarketConfig problemConfig = getGameConfig();


        var paperExperimentBuilder = new PaperExperimentBuilder<MarketConfig, MarketAction, MarketProbabilities, MarketState>()
            .setActionClass(MarketAction.class)
            .setSystemConfig(systemConfig)
            .setAlgorithmConfigList(List.of(algorithmConfig))
            .setProblemConfig(problemConfig)
//            .setOpponentSupplier(RealDataMarketPolicySupplier::new)
            .setProblemInstanceInitializerSupplier(InitialMarketStateSupplier::new);

        paperExperimentBuilder.execute();

    }


     public static MarketDataProvider createMarketDataProvider_old(String absoluteFilePath) throws IOException {
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

    public static MarketDataProvider createMarketDataProvider_new(String absoluteFilePath) throws IOException {
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
        return  2* Math.sin(i/(double)1) + 3;
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

        int lookbackLength = 5;
        int allowedCountOfTimestampsAheadOfEndOfData = 10;

        String pathToFile = "";

//        MarketDataProvider marketDataProvider = createMarketDataProvider_new(pathToFile);
        MarketDataProvider marketDataProvider = createMarketDataProvider();
        var staticPart = new MarketEnvironmentStaticPart(systemStopLoss, constantSpread, priceRange, tradeSize, commission, marketDataProvider);
        return new MarketConfig(1000, staticPart, lookbackLength, marketDataProvider, allowedCountOfTimestampsAheadOfEndOfData);
    }

    private static SystemConfig getSystemConfig() {
        return new SystemConfigBuilder()
            .setRandomSeed(0)
            .setStochasticStrategy(StochasticStrategy.REPRODUCIBLE)
            .setDrawWindow(false)
            .setParallelThreadsCount(4)
            .setSingleThreadedEvaluation(false)
            .setEvalEpisodeCount(1000)
            .buildSystemConfig();
    }

    private static PaperAlgorithmConfig getAlgorithmConfig() {
        return new AlgorithmConfigBuilder()
            //MCTS
            .cpuctParameter(1)

            //.mcRolloutCount(1)
            .setPlayerApproximatorConfig(new ApproximatorConfigBuilder().setDataAggregationAlgorithm(DataAggregationAlgorithm.EVERY_VISIT_MC).setApproximatorType(ApproximatorType.HASHMAP_LR).setLearningRate(0.01).build())
            // REINFORCEMENT
            .discountFactor(1)
            .batchEpisodeCount(1000)
            .treeUpdateConditionFactory(new FixedUpdateCountTreeConditionFactory(0))
            .stageCount(1000)
            .evaluatorType(EvaluatorType.RALF)
//            .setBatchedEvaluationSize(1)

            .selectorType(SelectorType.UCB)

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
            .explorationConstantSupplier(new Supplier<Double>() {
                @Override
                public Double get() {
                    return 1.0;
                }

                @Override
                public String toString() {
                    return "() -> 1.00";
                }
            })
            .temperatureSupplier(new Supplier<Double>() {
                private int callCount = 0;
                @Override
                public Double get() {
                    callCount++;
                    return Math.exp(-callCount / 1000000.0) * 1;
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

