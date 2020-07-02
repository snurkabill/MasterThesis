package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.policy.RandomizedPolicy;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class MaxUcbValueDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    public MaxUcbValueDistributionProvider() {
        super(false);
    }

    @Override
    public PlayingDistribution<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed)
    {
        int inGameEntityId = node.getStateWrapper().getInGameEntityIdWrapper();
        var childNodeMap = node.getChildNodeMap();

        double max = -Double.MAX_VALUE;
        TAction action = null;

        for (var entry : childNodeMap.entrySet()) {
            var metadata = entry.getValue().getSearchNodeMetadata();
            var value = metadata.getExpectedReward()[inGameEntityId] + metadata.getGainedReward()[inGameEntityId];
            if(value > max) {
                max = value;
                action = entry.getKey();
            }
        }
        return new PlayingDistribution<>(action, childNodeMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId],RandomizedPolicy.EMPTY_ARRAY);
    }
}
