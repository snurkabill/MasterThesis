package vahy.paperGenerics.policy.riskSubtree.strategiesProvider;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.FlowOptimizerType;
import vahy.paperGenerics.policy.flowOptimizer.HardFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.HardHardFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.HardHardSoftFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.HardSoftFlowOptimizer;
import vahy.paperGenerics.policy.flowOptimizer.SoftFlowOptimizer;
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

public class StrategiesProvider<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    private final InferenceExistingFlowStrategy inferenceExistingFlowStrategy;
    private final InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy;
    private final ExplorationExistingFlowStrategy explorationExistingFlowStrategy;
    private final ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy;
    private final FlowOptimizerType flowOptimizerType;

    public StrategiesProvider(InferenceExistingFlowStrategy inferenceExistingFlowStrategy,
                              InferenceNonExistingFlowStrategy inferenceNonExistingFlowStrategy,
                              ExplorationExistingFlowStrategy explorationExistingFlowStrategy,
                              ExplorationNonExistingFlowStrategy explorationNonExistingFlowStrategy,
                              FlowOptimizerType flowOptimizerType) {
        this.inferenceExistingFlowStrategy = inferenceExistingFlowStrategy;
        this.inferenceNonExistingFlowStrategy = inferenceNonExistingFlowStrategy;
        this.explorationExistingFlowStrategy = explorationExistingFlowStrategy;
        this.explorationNonExistingFlowStrategy = explorationNonExistingFlowStrategy;
        this.flowOptimizerType = flowOptimizerType;
    }

    public PlayingDistributionProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideInferenceExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        SplittableRandom random,
        double totalRiskAllowed,
        double temperature) {

        switch(inferenceExistingFlowStrategy){
            case SAMPLE_OPTIMAL_FLOW:
                return new InferenceFeasibleDistributionProvider<>(playerActions, random);
            case MAX_UCB_VISIT:
                return new MaxUcbVisitDistributionProvider<>(playerActions, random);
            case MAX_UCB_VALUE:
                return new MaxUcbValueDistributionProvider<>(playerActions, random);
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(inferenceExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideInferenceNonExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        SplittableRandom random,
        double totalRiskAllowed,
        double temperature) {

        switch(inferenceNonExistingFlowStrategy){
            case MAX_UCB_VISIT:
                return new MaxUcbVisitDistributionProvider<>(playerActions, random);
            case MAX_UCB_VALUE:
                return new MaxUcbValueDistributionProvider<>(playerActions, random);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(inferenceNonExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideExplorationExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        SplittableRandom random,
        double totalRiskAllowed,
        double temperature) {
        switch(explorationExistingFlowStrategy){
            case SAMPLE_OPTIMAL_FLOW_BOLTZMANN_NOISE:
                return new ExplorationFeasibleDistributionProvider<>(playerActions, random, totalRiskAllowed, temperature);
            case SAMPLE_OPTIMAL_FLOW:
                return new InferenceFeasibleDistributionProvider<>(playerActions, random);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(explorationExistingFlowStrategy);
        }
    }

    public PlayingDistributionProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideExplorationNonExistingFlowStrategy(
        TState state,
        List<TAction> playerActions,
        SplittableRandom random,
        double totalRiskAllowed,
        double temperature) {

        switch(explorationNonExistingFlowStrategy){
            case SAMPLE_UCB_VALUE:
                return new UcbValueDistributionProvider<>(playerActions, random);
            case SAMPLE_UCB_VISIT:
                return new UcbVisitDistributionProvider<>(playerActions, random);
            default:
                throw EnumUtils.createExceptionForUnknownEnumValue(explorationExistingFlowStrategy);
        }
    }

    public FlowOptimizer<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> provideFlowOptimizer() {

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
}
