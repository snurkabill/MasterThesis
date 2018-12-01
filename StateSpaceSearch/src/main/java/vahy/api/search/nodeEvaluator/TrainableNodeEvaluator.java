package vahy.api.search.nodeEvaluator;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.utils.ImmutableTuple;

import java.util.List;

public interface TrainableNodeEvaluator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata<TReward>,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends NodeEvaluator<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    void train(List<ImmutableTuple<TObservation, double[]>> trainData);
}
