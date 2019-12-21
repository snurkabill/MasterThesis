package vahy.paperGenerics.policy.riskSubtree.strategiesProvider;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.flowOptimizer.HardFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.HardHardFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.HardHardSoftFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.HardSoftFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.SoftFlowOptimizer;
import vahy.paperGenerics.policy.riskSubtree.FlowSumSubtreeRiskCalculator;
import vahy.paperGenerics.policy.riskSubtree.MinimalRiskReachAbilityCalculator;
import vahy.paperGenerics.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.paperGenerics.policy.riskSubtree.SubtreePriorRiskCalculator;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRootRiskCalculator;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.ExplorationFeasibleDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.InferenceFeasibleDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.MaxUcbValueDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.MaxUcbVisitDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.PlayingDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.UcbValueDistributionProvider;
import vahy.paperGenerics.policy.riskSubtree.playingDistribution.UcbVisitDistributionProvider;
import vahy.utils.EnumUtils;

import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class StrategiesProvider<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private final InferenceExistingFlowStrategy inferenceExistingFlowStrategy;
    private final InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy;
    private final ExplorationExistingFlowStrategy explorationExistingFlowStrategy;
    private final ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy;
    private final FlowOptimizerType flowOptimizerType;
    private final SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow;
    private final SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow;

    public StrategiesProvider(InferenceExistingFlowStrategy inferenceExistingFlowStrategy,
                              InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy,
                              ExplorationExistingFlowStrategy explorationExistingFlowStrategy,
                              ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy,
                              FlowOptimizerType flowOptimizerType,
                              SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForKnownFlow,
                              SubTreeRiskCalculatorType subTreeRiskCalculatorTypeForUnknownFlow) {
        this.inferenceExistingFlowStrategy = inferenceExistingFlowStrategy;
        this.inferenceNonExistingFlowStrategy = inferenceNonExistingFlowStrategy;
        this.explorationExistingFlowStrategy = explorationExistingFlowStrategy;
        this.explorationNonExistingFlowStrategy = explorationNonExistingFlowStrategy;
        this.flowOptimizerType = flowOptimizerType;
        this.subTreeRiskCalculatorTypeForKnownFlow = subTreeRiskCalculatorTypeForKnownFlow;
        this.subTreeRiskCalculatorTypeForUnknownFlow = subTreeRiskCalculatorTypeForUnknownFlow;
    }

    public PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideInferenceExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        double totalRiskAllowed,
        double temperature,
        SplittableRandom random) {

        switch(inferenceExistingFlowStrategy){
            case SAMPLE_OPTIMAL_FLOW:
                return new InferenceFeasibleDistributionProvider<>(playerActions, random, provideRiskCalculatorForKnownFlow());
            case MAX_UCB_VISIT:
                return new MaxUcbVisitDistributionProvider<>(playerActions, random);
            case MAX_UCB_VALUE:
                return new MaxUcbValueDistributionProvider<>(playerActions, random);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(inferenceExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideInferenceNonExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        double totalRiskAllowed,
        double temperature,
        SplittableRandom random) {

        switch(inferenceNonExistingFlowStrategy){
            case MAX_UCB_VISIT:
                return new MaxUcbVisitDistributionProvider<>(playerActions, random);
            case MAX_UCB_VALUE:
                return new MaxUcbValueDistributionProvider<>(playerActions, random);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(inferenceNonExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideExplorationExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        double totalRiskAllowed,
        double temperature,
        SplittableRandom random) {
        switch(explorationExistingFlowStrategy){
            case SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE:
                return new ExplorationFeasibleDistributionProvider<>(
                    playerActions,
                    random,
                    provideRiskCalculatorForKnownFlow(),
                    provideRiskCalculatorForUnknownFlow(),
                    totalRiskAllowed,
                    temperature);
            case SAMPLE_OPTIMAL_FLOW:
                return new InferenceFeasibleDistributionProvider<>(playerActions, random, provideRiskCalculatorForKnownFlow());
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(explorationExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideExplorationNonExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        double totalRiskAllowed,
        double temperature,
        SplittableRandom random) {

        switch(explorationNonExistingFlowStrategy){
            case SAMPLE_UCB_VALUE:
                return new UcbValueDistributionProvider<>(playerActions, random);
            case SAMPLE_UCB_VISIT:
                return new UcbVisitDistributionProvider<>(playerActions, random);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(explorationExistingFlowStrategy);
        }
    }

    public FlowOptimizer<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideFlowOptimizer() {

        switch (flowOptimizerType) {
            case HARD:
                return new HardFlowOptimizer<>();
            case SOFT:
                return new SoftFlowOptimizer<>();
            case HARD_SOFT:
                return new HardSoftFlowOptimizer<>();
            case HARD_HARD:
                return new HardHardFlowOptimizer<>();
            case HARD_HARD_SOFT:
                return new HardHardSoftFlowOptimizer<>();
            default:
                throw EnumUtils.createExceptionForNotExpectedEnumValue(flowOptimizerType);
        }
    }

    public Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> provideRiskCalculator(SubTreeRiskCalculatorType subTreeRiskCalculatorType) {
        switch(subTreeRiskCalculatorType) {
            case FLOW_SUM:
                return FlowSumSubtreeRiskCalculator::new;
            case MINIMAL_RISK_REACHABILITY:
                return MinimalRiskReachAbilityCalculator::new;
            case PRIOR_SUM:
                return SubtreePriorRiskCalculator::new;
            case ROOT_PREDICTION:
                return SubtreeRootRiskCalculator::new;
            default:
                throw EnumUtils.createExceptionForNotExpectedEnumValue(subTreeRiskCalculatorType);
        }
    }

    public Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> provideRiskCalculatorForKnownFlow() {
        return provideRiskCalculator(subTreeRiskCalculatorTypeForKnownFlow);
    }

    public Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> provideRiskCalculatorForUnknownFlow() {
        return provideRiskCalculator(subTreeRiskCalculatorTypeForUnknownFlow);
    }

}
