package vahy.ralph.evaluator;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import vahy.RiskStateWrapper;
import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.examples.bomberman.BomberManAction;
import vahy.examples.bomberman.BomberManConfig;
import vahy.examples.bomberman.BomberManInstance;
import vahy.examples.bomberman.BomberManRiskInstanceInitializer;
import vahy.examples.bomberman.BomberManRiskState;
import vahy.impl.RoundBuilder;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.impl.learning.dataAggregator.FirstVisitMonteCarloDataAggregator;
import vahy.impl.learning.trainer.PredictorTrainingSetup;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.tree.treeUpdateCondition.FixedUpdateCountTreeConditionFactory;
import vahy.ralph.RalphTreeUpdater;
import vahy.benchmark.RiskEpisodeStatisticsCalculator;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.metadata.RiskSearchMetadataFactory;
import vahy.ralph.policy.RalphPolicy;
import vahy.ralph.policy.RiskAverseSearchTree;
import vahy.ralph.policy.flowOptimizer.FlowOptimizerType;
import vahy.ralph.policy.linearProgram.NoiseStrategy;
import vahy.ralph.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.ralph.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.ralph.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.ralph.reinforcement.RalphDataTablePredictorWithLr;
import vahy.ralph.reinforcement.learning.RalphEpisodeDataMaker_V2;
import vahy.ralph.selector.RalphNodeSelector;
import vahy.test.ConvergenceAssert;
import vahy.utils.EnumUtils;
import vahy.utils.StreamUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PaperSingleVsBatchedEvaluatorTest {

    private List<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> getPlayerSupplierList(int playerCount, int envEntitiesCount, int batchSize, ProblemConfig config) {

        var playerList = new ArrayList<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>>(playerCount);
        double discountFactor = 1;
        var totalEntityCount = envEntitiesCount + playerCount;
        var actionClass = BomberManAction.class;
        var totalActionCount = actionClass.getEnumConstants().length;

        var defaultPrediction_risk = new double[totalEntityCount * 2 + totalActionCount];
        for (int i = totalEntityCount * 2; i < defaultPrediction_risk.length; i++) {
            defaultPrediction_risk[i] = 1.0 / totalActionCount;
        }

        var treeExpansionCount = 100;

        for (int i = 0; i < playerCount; i++) {
            int policyId = i + envEntitiesCount;

            var dataAggregator_risk = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            var episodeDataMaker_risk = new RalphEpisodeDataMaker_V2<BomberManAction, BomberManRiskState>(policyId, totalActionCount, discountFactor, dataAggregator_risk);
            var trainablePredictor_risk = new RalphDataTablePredictorWithLr(defaultPrediction_risk, 0.1, totalActionCount, totalEntityCount);

            var predictorTrainingSetup_risk = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManRiskState>(
                policyId,
                trainablePredictor_risk,
                episodeDataMaker_risk,
                dataAggregator_risk
            );

            var metadataFactory = new RiskSearchMetadataFactory<BomberManAction, DoubleVector, BomberManRiskState>(actionClass, totalEntityCount);
            var searchNodeFactory = new SearchNodeBaseFactoryImpl<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(actionClass, metadataFactory);

            var totalRiskAllowedInference = 0.1;
            Supplier<Double> explorationSupplier = () -> 0.0;
            Supplier<Double> temperatureSupplier = () -> 0.1;
            Supplier<Double> trainingRiskSupplier = () -> totalRiskAllowedInference;

            var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeExpansionCount);

            var strategiesProvider = new StrategiesProvider<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                actionClass,
                InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
                InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
                ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
                ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
                FlowOptimizerType.HARD_HARD,
                SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
                NoiseStrategy.NOISY_05_06);

            var updater = new RalphTreeUpdater<BomberManAction, DoubleVector, BomberManRiskState>();
            var nodeEvaluator = batchSize == 0 ?
                new RalphNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor_risk, config.isModelKnown()) :
                new RalphBatchNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor_risk, batchSize, config.isModelKnown());


            playerList.add(new PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>(
                policyId,
                1,
                (initialState_, policyMode_, policyId_, random_) -> {
                    Supplier<RalphNodeSelector<BomberManAction, DoubleVector, BomberManRiskState>> nodeSelectorSupplier = () -> new RalphNodeSelector<>(random_, config.isModelKnown(), 1, totalActionCount);
                    var node = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
                    switch (policyMode_) {
                        case INFERENCE:
                            return new RalphPolicy<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                                policyId_,
                                random_,
                                treeUpdateConditionFactory.create(),
                                new RiskAverseSearchTree<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                                    searchNodeFactory,
                                    node,
                                    nodeSelectorSupplier.get(),
                                    updater,
                                    nodeEvaluator,
                                    random_,
                                    totalRiskAllowedInference,
                                    strategiesProvider));
                        case TRAINING:
                            return new RalphPolicy<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                                policyId_,
                                random_,
                                treeUpdateConditionFactory.create(),
                                new RiskAverseSearchTree<BomberManAction, DoubleVector, RalphMetadata<BomberManAction>, BomberManRiskState>(
                                    searchNodeFactory,
                                    node,
                                    nodeSelectorSupplier.get(),
                                    updater,
                                    nodeEvaluator,
                                    random_,
                                    trainingRiskSupplier.get(),
                                    strategiesProvider),
                                explorationSupplier.get(),
                                temperatureSupplier.get());
                        default:
                            throw EnumUtils.createExceptionForNotExpectedEnumValue(policyMode_);
                    }
                },
                List.of(predictorTrainingSetup_risk)
            ));
        }
        return playerList;

    }

    private List<List<Double>> runExperiment(List<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> policyList, BomberManConfig config, long seed) {

        var algorithmConfig = new CommonAlgorithmConfigBase(3, 20);

        var systemConfig = new SystemConfig(
            seed,
            false,
            ConvergenceAssert.TEST_THREAD_COUNT,
            false,
            50,
            0,
            false,
            false,
            false,
            Path.of("TEST_PATH"),
            null);

        var roundBuilder = RoundBuilder.getRoundBuilder(
            "PaperSingleVsBatchedEvalTest",
            config,
            systemConfig,
            algorithmConfig,
            policyList,
            null,
            BomberManRiskInstanceInitializer::new,
            RiskStateWrapper::new,
            new RiskEpisodeStatisticsCalculator<>(),
            new EpisodeResultsFactoryBase<>()
        );
        var result = roundBuilder.execute();
        return result.getTrainingStatisticsList().stream().map(EpisodeStatistics::getTotalPayoffAverage).collect(Collectors.toList());
    }


    @Test
    public void paperSingleVsBatchedEvaluatorTest() {
        var seedStream = StreamUtils.getSeedStream(3);
        var trialCount = 3;
        var playerCount = 2;
        try {
            var config = new BomberManConfig(
                100,
                true,
                100,
                1,
                3,
                3,
                3,
                1,
                playerCount,
                0.1,
                BomberManInstance.BM_01,
                PolicyShuffleStrategy.CATEGORY_SHUFFLE);

            var list = new ArrayList<List<List<Double>>>();
            for (Long seed : (Iterable<Long>) seedStream::iterator) {
                List<List<Double>> result = runExperiment(getPlayerSupplierList(playerCount, config.getEnvironmentPolicyCount(), 0, config), config, seed);
                for (int i = 1; i <= trialCount; i++) {
                    List<List<Double>> tmp = runExperiment(getPlayerSupplierList(playerCount, config.getEnvironmentPolicyCount(), i, config), config, seed);
                    for (int j = 0; j < tmp.size(); j++) {
                      //  System.out.println(String.join(" ", result.get(j).stream().map(x -> x.toString()).collect(Collectors.toList())) + "  " + String.join(" ", tmp.get(j).stream().map(x -> x.toString()).collect(Collectors.toList())));
                        assertIterableEquals(result.get(j), tmp.get(j));
                    }
                }
                if(list.stream().map(List::hashCode).distinct().count() > 1) {
                    break;
                }
                list.add(result);
            }
            assertNotEquals(1, list.stream().map(List::hashCode).distinct().count());
        } catch (IOException | InvalidInstanceSetupException e) {
            Assertions.fail(e);
        }
    }

}
