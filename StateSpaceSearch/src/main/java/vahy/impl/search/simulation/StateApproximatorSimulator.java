package vahy.impl.search.simulation;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

import java.util.function.Function;

public class StateApproximatorSimulator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>>
    extends AbstractNodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private final Function<TObservation, TReward> stateEvaluatingFunction;

    public StateApproximatorSimulator(RewardAggregator<TReward> rewardAggregator, Function<TObservation, TReward> stateEvaluatingFunction, double discountFactor) {
        super(rewardAggregator, discountFactor);
        this.stateEvaluatingFunction = stateEvaluatingFunction;
    }

    @Override
    protected TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node, TAction firstAction) {
        StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = node.getWrappedState().applyAction(firstAction);
        TReward expectedReturnAfterAction = stateEvaluatingFunction.apply(stateRewardReturn.getState().getObservation());
        return rewardAggregator.aggregateDiscount(stateRewardReturn.getReward(), expectedReturnAfterAction, discountFactor);
    }
}
