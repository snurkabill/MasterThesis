package vahy.ralph.policy.riskSubtree.strategiesProvider;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.policy.flowOptimizer.AbstractFlowOptimizer;
import vahy.ralph.policy.flowOptimizer.FlowOptimizerType;
import vahy.ralph.policy.flowOptimizer.HardFlowOptimizer;
import vahy.ralph.policy.flowOptimizer.HardHardFlowOptimizer;
import vahy.ralph.policy.flowOptimizer.HardHardSoftFlowOptimizer;
import vahy.ralph.policy.flowOptimizer.HardSoftFlowOptimizer;
import vahy.ralph.policy.flowOptimizer.SoftFlowOptimizer;
import vahy.ralph.policy.linearProgram.NoiseStrategy;
import vahy.ralph.policy.riskSubtree.FlowSumSubtreeRiskCalculator;
import vahy.ralph.policy.riskSubtree.MinimalRiskReachAbilityCalculator;
import vahy.ralph.policy.riskSubtree.SubTreeRiskCalculatorType;
import vahy.ralph.policy.riskSubtree.SubtreePriorRiskCalculator;
import vahy.ralph.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.ralph.policy.riskSubtree.SubtreeRootRiskCalculator;
import vahy.ralph.policy.riskSubtree.playingDistribution.ExplorationUcbValueSamplingProvider;
import vahy.ralph.policy.riskSubtree.playingDistribution.ExplorationFeasibleDistributionProvider;
import vahy.ralph.policy.riskSubtree.playingDistribution.InferenceFeasibleDistributionProvider;
import vahy.ralph.policy.riskSubtree.playingDistribution.MaxUcbValueDistributionProvider;
import vahy.ralph.policy.riskSubtree.playingDistribution.MaxUcbVisitDistributionProvider;
import vahy.ralph.policy.riskSubtree.playingDistribution.PlayingDistributionProvider;
import vahy.ralph.policy.riskSubtree.playingDistribution.UcbValueDistributionProvider;
import vahy.ralph.policy.riskSubtree.playingDistribution.UcbVisitDistributionProvider;
import vahy.utils.EnumUtils;

import java.util.SplittableRandom;
import java.util.function.Supplier;

public class StrategiesProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>> {

    private final Class<TAction> actionClass;
    private final InferenceExistingFlowStrategy inferenceExistingFlowStrategy;
    private final InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy;
    private final ExplorationExistingFlowStrategy explorationExistingFlowStrategy;
    private final ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy;
    private final FlowOptimizerType flowOptimizerType;
    private final SubTreeRiskCalculatorType subTreeRiskCalculatorType;
    private final NoiseStrategy noiseStrategy;

    public StrategiesProvider(Class<TAction> actionClass, InferenceExistingFlowStrategy inferenceExistingFlowStrategy,
                              InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy,
                              ExplorationExistingFlowStrategy explorationExistingFlowStrategy,
                              ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy,
                              FlowOptimizerType flowOptimizerType,
                              SubTreeRiskCalculatorType subTreeRiskCalculatorType,
                              NoiseStrategy noiseStrategy) {
        this.actionClass = actionClass;
        this.inferenceExistingFlowStrategy = inferenceExistingFlowStrategy;
        this.inferenceNonExistingFlowStrategy = inferenceNonExistingFlowStrategy;
        this.explorationExistingFlowStrategy = explorationExistingFlowStrategy;
        this.explorationNonExistingFlowStrategy = explorationNonExistingFlowStrategy;
        this.flowOptimizerType = flowOptimizerType;
        this.subTreeRiskCalculatorType = subTreeRiskCalculatorType;
        this.noiseStrategy = noiseStrategy;
    }

    public PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> provideInferenceExistingFlowStrategy() {
        switch(inferenceExistingFlowStrategy) {
            case SAMPLE_OPTIMAL_FLOW:
                return new InferenceFeasibleDistributionProvider<>(actionClass);
            case MAX_UCB_VISIT:
                return new MaxUcbVisitDistributionProvider<>();
            case MAX_UCB_VALUE:
                return new MaxUcbValueDistributionProvider<>();
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(inferenceExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> provideInferenceNonExistingFlowStrategy() {
        switch(inferenceNonExistingFlowStrategy) {
            case MAX_UCB_VISIT:
                return new MaxUcbVisitDistributionProvider<>();
            case MAX_UCB_VALUE:
                return new MaxUcbValueDistributionProvider<>();
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(inferenceNonExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> provideExplorationExistingFlowStrategy() {
        switch(explorationExistingFlowStrategy){
            case SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE:
                return new ExplorationFeasibleDistributionProvider<>(actionClass, provideRiskCalculator());
            case SAMPLE_OPTIMAL_FLOW:
                return new InferenceFeasibleDistributionProvider<>(actionClass);
            case SAMPLE_UCB_VALUE_WITH_TEMPERATURE:
                return new ExplorationUcbValueSamplingProvider<>(actionClass);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(explorationExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> provideExplorationNonExistingFlowStrategy() {
        switch(explorationNonExistingFlowStrategy){
            case SAMPLE_UCB_VALUE:
                return new UcbValueDistributionProvider<>(false);
            case SAMPLE_UCB_VALUE_WITH_TEMPERATURE:
                return new UcbValueDistributionProvider<>(true);
            case SAMPLE_UCB_VISIT:
                return new UcbVisitDistributionProvider<>(false);
            case SAMPLE_UCB_VISIT_WITH_TEMPERATURE:
                return new UcbVisitDistributionProvider<>(true);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(explorationExistingFlowStrategy);
        }
    }

    public AbstractFlowOptimizer<TAction, TObservation, TSearchNodeMetadata, TState> provideFlowOptimizer(SplittableRandom random) {
        switch (flowOptimizerType) {
            case HARD:
                return new HardFlowOptimizer<>(random, noiseStrategy);
            case SOFT:
                return new SoftFlowOptimizer<>(random, noiseStrategy);
            case HARD_SOFT:
                return new HardSoftFlowOptimizer<>(random, noiseStrategy);
            case HARD_HARD:
                return new HardHardFlowOptimizer<>(random, noiseStrategy);
            case HARD_HARD_SOFT:
                return new HardHardSoftFlowOptimizer<>(random, noiseStrategy);
            default:
                throw EnumUtils.createExceptionForNotExpectedEnumValue(flowOptimizerType);
        }
    }

    public Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>> provideRiskCalculator() {
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

}
