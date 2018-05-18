package vahy.impl.search.simulation;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.SearchNode;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoNodeMetadata;
import vahy.impl.search.node.nodeMetadata.alphago.AlphaGoStateActionMetadata;
import vahy.utils.ImmutableTuple;

import java.util.Map;
import java.util.function.Function;

public class AlphaGoSimulator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation>
    implements NodeEvaluationSimulator<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, State<TAction, TReward, TObservation>> {

    private final Function<TObservation, ImmutableTuple<Double, Map<TAction, Double>>> evaluatingFunction;

    public AlphaGoSimulator(Function<TObservation, ImmutableTuple<Double, Map<TAction, Double>>> evaluatingFunction) {
        this.evaluatingFunction = evaluatingFunction;
    }

    @Override
    public void calculateMetadataEstimation(SearchNode<TAction, TReward, TObservation, AlphaGoStateActionMetadata<TReward>, AlphaGoNodeMetadata<TAction, TReward>, State<TAction, TReward, TObservation>> expandedNode) {
        throw new UnsupportedOperationException("Not finished");

//        ImmutableTuple<Double, Map<TAction, Double>> evaluationResult = evaluatingFunction.apply(expandedNode.getWrappedState().getObservation());
//        Map<TAction, Double> allActionProbabilities = evaluationResult.getSecond();
//
//        expandedNode.getSearchNodeMetadata().setWinningProbability(evaluationResult.getFirst());
//        Map<TAction, AlphaGoStateActionMetadata<TReward>> stateActionMetadataMap = expandedNode.getSearchNodeMetadata().getStateActionMetadataMap();

    }
}
