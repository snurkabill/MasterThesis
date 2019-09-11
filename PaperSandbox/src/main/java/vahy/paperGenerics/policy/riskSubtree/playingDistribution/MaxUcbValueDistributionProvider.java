package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;

import java.util.List;
import java.util.SplittableRandom;

public class MaxUcbValueDistributionProvider<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    public MaxUcbValueDistributionProvider(List<TAction> playerActions, SplittableRandom random) {
        super(playerActions, random);
    }

    @Override
    public PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node)
    {
        // TODO: remove code redundancy
        var ucbDistribution = getUcbValueDistribution(node);
        var max = ucbDistribution.getSecond()[0];
        var index = 0;
        for (int i = 1; i < ucbDistribution.getFirst().size(); i++) {
            if(max < ucbDistribution.getSecond()[i]) {
                max = ucbDistribution.getSecond()[i];
                index = i;
            }
        }
        return new PlayingDistribution<>(ucbDistribution.getFirst().get(index), index, ucbDistribution.getSecond(), ucbDistribution.getThird(), () -> subtreeRoot -> 1);
    }
}
