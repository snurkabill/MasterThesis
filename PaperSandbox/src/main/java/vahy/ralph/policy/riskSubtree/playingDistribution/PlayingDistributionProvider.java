package vahy.ralph.policy.riskSubtree.playingDistribution;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;

import java.util.SplittableRandom;

public interface PlayingDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>> {

    PlayingDistribution<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed);

}
