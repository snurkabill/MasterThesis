package vahy.paperGenerics.policy.riskSubtree;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;

import java.util.LinkedList;
import java.util.Map;

public class FlowSumSubtreeRiskCalculator<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    implements SubtreeRiskCalculator<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    @Override
    public double calculateRisk(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> subTreeRoot) {
        double risk = 0;

        LinkedList<SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> queue = new LinkedList<>();
        queue.addFirst(subTreeRoot);

        while(!queue.isEmpty()) {
            SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node = queue.poll();
            if(node.isLeaf()) {
                if(node.isFinalNode()) {
                    risk += node.getWrappedState().isRiskHit() ? node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution() : 0.0;
                } else {
                    risk += node.getSearchNodeMetadata().getPredictedRisk() * node.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();

                }
            } else {
                for (Map.Entry<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                }
            }
        }
        return risk;
    }

}
