package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.policy.RandomizedPolicy;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.Map;
import java.util.SplittableRandom;

public class MaxUcbVisitDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    public MaxUcbVisitDistributionProvider() {
        super(false);
    }

    @Override
    public PlayingDistribution<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed)
    {
        int inGameEntityId = node.getStateWrapper().getInGameEntityId();
        var max = Integer.MIN_VALUE;
        TAction action = null;
        var childNodeMap = node.getChildNodeMap();
        for (Map.Entry<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> entry : childNodeMap.entrySet()) {
            var visitCounter = entry.getValue().getSearchNodeMetadata().getVisitCounter();
            if(visitCounter > max) {
                max = visitCounter;
                action = entry.getKey();
            }
        }
        return new PlayingDistribution<>(action, childNodeMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId], RandomizedPolicy.EMPTY_ARRAY);
    }
}
