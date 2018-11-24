package vahy.impl.search.update;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;

public class MCTSTransitionUpdater<
    TAction extends Action,
    TObservation extends Observation,
    TState extends State<TAction, DoubleScalarReward, TObservation, TState>>
    implements NodeTransitionUpdater<TAction, DoubleScalarReward, TObservation, MCTSNodeMetadata<DoubleScalarReward>, TState> {

//    private final double discountFactor;
//    private final RewardAggregator<DoubleScalarReward> rewardAggregator;
//
//    public MCTSTransitionUpdater(double discountFactor, RewardAggregator<DoubleScalarReward> rewardAggregator) {
//        this.discountFactor = discountFactor;
//        this.rewardAggregator = rewardAggregator;
//    }
//
//    @Override
//    public void applyUpdate(
//        SearchNode<TAction, DoubleScalarReward, TObservation, Ucb1StateActionMetadata<DoubleScalarReward>, MCTSNodeMetadata<TAction, DoubleScalarReward>, TState> parent,
//        SearchNode<TAction, DoubleScalarReward, TObservation, Ucb1StateActionMetadata<DoubleScalarReward>, MCTSNodeMetadata<TAction, DoubleScalarReward>, TState> child, TAction action) {
//        MCTSNodeMetadata<TAction, DoubleScalarReward> parentSearchNodeMetadata = parent.getSearchNodeMetadata();
//        Ucb1StateActionMetadata<DoubleScalarReward> stateActionMetadata = parentSearchNodeMetadata.getStateActionMetadataMap().get(action);
//
//        stateActionMetadata.setExpectedReward(new DoubleScalarReward(rewardAggregator.aggregateDiscount(
//            stateActionMetadata.getGainedReward(),
//            child.getSearchNodeMetadata().getExpectedReward(),
//            discountFactor).getValue()));
//        double parentCumulativeEstimates = parentSearchNodeMetadata.getExpectedReward().getValue() * (parentSearchNodeMetadata.getVisitCounter() - 1);
//
//        DoubleScalarReward newParentCumulativeEstimate = parent.isOpponentTurn() ?
//            rewardAggregator.averageReward(parentSearchNodeMetadata
//                .getStateActionMetadataMap()
//                .values()
//                .stream()
//                .map(StateActionMetadata::getExpectedReward))
//            : parentSearchNodeMetadata
//                .getStateActionMetadataMap()
//                .values()
//                .stream()
//                .map(StateActionMetadata::getExpectedReward)
//                .max(Comparable::compareTo)
//                .orElseThrow(() -> new IllegalStateException("Children should be always expanded when doing transition update"));
//
//        double sum = parentCumulativeEstimates + newParentCumulativeEstimate.getValue();
//        parentSearchNodeMetadata.setExpectedReward(new DoubleScalarReward(sum / parentSearchNodeMetadata.getVisitCounter()));
//    }

    @Override
    public void applyUpdate(SearchNode<TAction, DoubleScalarReward, TObservation, MCTSNodeMetadata<DoubleScalarReward>, TState> evaluatedNode,
                            SearchNode<TAction, DoubleScalarReward, TObservation, MCTSNodeMetadata<DoubleScalarReward>, TState> parent,
                            SearchNode<TAction, DoubleScalarReward, TObservation, MCTSNodeMetadata<DoubleScalarReward>, TState> child) {
        MCTSNodeMetadata<DoubleScalarReward> parentNodeMetadata = parent.getSearchNodeMetadata();
        parentNodeMetadata.increaseVisitCounter();
        double estimatedTotalReward = evaluatedNode.getSearchNodeMetadata().getExpectedReward().getValue() +
                                                  evaluatedNode.getSearchNodeMetadata().getCumulativeReward().getValue();
        parentNodeMetadata.setSumOfTotalEstimations(new DoubleScalarReward(parentNodeMetadata.getSumOfTotalEstimations().getValue() + estimatedTotalReward));
        parentNodeMetadata.setExpectedReward(new DoubleScalarReward(parentNodeMetadata.getSumOfTotalEstimations().getValue() / parentNodeMetadata.getVisitCounter()));
    }
}
