package vahy.impl.search.simulation;

//public class CumulativeRewardSimulator<
//    TAction extends Action,
//    TReward extends Reward,
//    TObservation extends Observation,
//    TStateActionMetadata extends StateActionMetadata<TReward>,
//    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
//    TState extends State<TAction, TReward, TObservation>> implements NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {
//
//    @Override
//    public void calculateMetadataEstimation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode) {
//        if(expandedNode.getSearchNodeMetadata().getExpectedReward() == null) {
//            expandedNode.getSearchNodeMetadata().setExpectedReward(expandedNode.getSearchNodeMetadata().getCumulativeReward());
//        }
//        for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> entry : expandedNode.getChildNodeMap().entrySet()) {
//            entry.getValue().getSearchNodeMetadata().setExpectedReward(entry.getValue().getSearchNodeMetadata().getCumulativeReward());
//            expandedNode.getSearchNodeMetadata().getStateActionMetadataMap().get(entry.getKey()).setExpectedReward(entry.getValue().getSearchNodeMetadata().getCumulativeReward());
//        }
//    }
//
//}
