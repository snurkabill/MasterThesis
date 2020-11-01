package vahy.impl.policy.mcts;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.predictor.TrainablePredictor;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

public class MCTSPredictionEvaluator<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends MCTSMetadata,
    TState extends State<TAction, DoubleVector, TState>>
    extends MCTSEvaluator<TAction, DoubleVector, TSearchNodeMetadata, TState> {

    private final TrainablePredictor predictor;

    public MCTSPredictionEvaluator(SearchNodeFactory<TAction, DoubleVector, TSearchNodeMetadata, TState> searchNodeFactory, TrainablePredictor predictor) {
        super(searchNodeFactory);
        this.predictor = predictor;
    }

    @Override
    protected ImmutableTuple<double[], Integer> estimateRewards(SearchNode<TAction, DoubleVector, TSearchNodeMetadata, TState> selectedNode) {
        return new ImmutableTuple<>(predictor.apply(selectedNode.getStateWrapper().getObservation()), 1);
    }
}
