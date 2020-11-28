package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public interface PlayingDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>> {

    PlayingDistribution<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed);

}
