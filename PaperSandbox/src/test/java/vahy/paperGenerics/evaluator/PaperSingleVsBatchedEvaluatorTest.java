package vahy.paperGenerics.evaluator;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import vahy.ConvergenceAssert;
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
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.PaperTreeUpdater;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.paperGenerics.benchmark.PaperEpisodeStatisticsCalculator;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.metadata.PaperMetadataFactory;
import vahy.paperGenerics.policy.PaperPolicyImpl;
import vahy.paperGenerics.policy.RiskAverseSearchTree;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.ExplorationNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.InferenceNonExistingFlowStrategy;
import vahy.paperGenerics.policy.riskSubtree.strategiesProvider.StrategiesProvider;
import vahy.paperGenerics.reinforcement.PaperDataTablePredictorWithLr;
import vahy.paperGenerics.reinforcement.learning.PaperEpisodeDataMaker_V2;
import vahy.paperGenerics.selector.PaperNodeSelector;
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

    private RoundBuilder<BomberManConfig, BomberManAction, BomberManRiskState, PaperEpisodeStatistics> getRoundBuilder(BomberManConfig config,
                                                                                                                  CommonAlgorithmConfigBase algorithmConfig,
                                                                                                                  SystemConfig systemConfig,
                                                                                                                  List<PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>> policyArgumentList) {
        return new RoundBuilder<BomberManConfig, BomberManAction, BomberManRiskState, PaperEpisodeStatistics>()
            .setRoundName("SHTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((config_, splittableRandom_) -> policyMode -> new BomberManRiskInstanceInitializer(config_, splittableRandom_).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new PaperEpisodeStatisticsCalculator<>())
            .setStateStateWrapperInitializer(PaperStateWrapper::new)
            .setPlayerPolicySupplierList(policyArgumentList);
    }

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

        var treeExpansionCount = 10;

        for (int i = 0; i < playerCount; i++) {
            int policyId = i + envEntitiesCount;

            var episodeDataMaker_risk = new PaperEpisodeDataMaker_V2<BomberManAction, BomberManRiskState>(discountFactor, totalActionCount, policyId);
            var dataAggregator_risk = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());
            var trainablePredictor_risk = new PaperDataTablePredictorWithLr(defaultPrediction_risk, 0.1, totalActionCount, totalEntityCount);

            var predictorTrainingSetup_risk = new PredictorTrainingSetup<BomberManAction, DoubleVector, BomberManRiskState>(
                policyId,
                trainablePredictor_risk,
                episodeDataMaker_risk,
                dataAggregator_risk
            );

            var metadataFactory = new PaperMetadataFactory<BomberManAction, DoubleVector, BomberManRiskState>(actionClass, totalEntityCount);
            var searchNodeFactory = new SearchNodeBaseFactoryImpl<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(actionClass, metadataFactory);

            var totalRiskAllowedInference = 0.1;
            Supplier<Double> explorationSupplier = () -> 0.0;
            Supplier<Double> temperatureSupplier = () -> 0.1;
            Supplier<Double> trainingRiskSupplier = () -> totalRiskAllowedInference;

            var treeUpdateConditionFactory = new FixedUpdateCountTreeConditionFactory(treeExpansionCount);

            var strategiesProvider = new StrategiesProvider<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                actionClass,
                InferenceExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW,
                InferenceNonExistingFlowStrategy.MAX_UCB_VALUE,
                ExplorationExistingFlowStrategy.SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE,
                ExplorationNonExistingFlowStrategy.SAMPLE_UCB_VALUE_WITH_TEMPERATURE,
                FlowOptimizerType.HARD_HARD,
                SubTreeRiskCalculatorType.MINIMAL_RISK_REACHABILITY,
                NoiseStrategy.NOISY_05_06);

            var updater = new PaperTreeUpdater<BomberManAction, DoubleVector, BomberManRiskState>();
            var nodeEvaluator = batchSize == 0 ?
                new PaperNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor_risk, config.isModelKnown()) :
                new PaperBatchNodeEvaluator<BomberManAction, BomberManRiskState>(searchNodeFactory, trainablePredictor_risk, batchSize, config.isModelKnown());


            playerList.add(new PolicyDefinition<BomberManAction, DoubleVector, BomberManRiskState>(
                policyId,
                1,
                (initialState_, policyMode_, policyId_, random_) -> {
                    Supplier<PaperNodeSelector<BomberManAction, DoubleVector, BomberManRiskState>> nodeSelectorSupplier = () -> new PaperNodeSelector<>(random_, config.isModelKnown(), 1, totalActionCount);
                    var node = searchNodeFactory.createNode(initialState_, metadataFactory.createEmptyNodeMetadata(), new EnumMap<>(actionClass));
                    switch (policyMode_) {
                        case INFERENCE:
                            return new PaperPolicyImpl<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                                policyId_,
                                random_,
                                treeUpdateConditionFactory.create(),
                                new RiskAverseSearchTree<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                                    searchNodeFactory,
                                    node,
                                    nodeSelectorSupplier.get(),
                                    updater,
                                    nodeEvaluator,
                                    random_,
                                    totalRiskAllowedInference,
                                    strategiesProvider));
                        case TRAINING:
                            return new PaperPolicyImpl<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
                                policyId_,
                                random_,
                                treeUpdateConditionFactory.create(),
                                new RiskAverseSearchTree<BomberManAction, DoubleVector, PaperMetadata<BomberManAction>, BomberManRiskState>(
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

        var algorithmConfig = new CommonAlgorithmConfigBase(10, 50);

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

        var roundBuilder = getRoundBuilder(config, algorithmConfig, systemConfig, policyList);
        var result = roundBuilder.execute();
        return result.getTrainingStatisticsList().stream().map(EpisodeStatistics::getTotalPayoffAverage).collect(Collectors.toList());
    }


    @Test
    public void paperSingleVsBatchedEvaluatorTest() {
        var seedStream = StreamUtils.getSeedStream(3);
        var trialCount = 4;
        var playerCount = 3;
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
                        System.out.println(tmp.get(j).toString());
                        System.out.println(result.get(j).toString());
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
