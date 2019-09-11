package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class UcbValueDistributionProvider<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    public UcbValueDistributionProvider(List<TAction> playerActions, SplittableRandom random) {
        super(playerActions, random);
    }

    @Override
    public PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node)
    {
        var ucbDistribution = getUcbValueDistribution(node);
        int index = RandomDistributionUtils.getRandomIndexFromDistribution(ucbDistribution.getSecond(), random);
        return new PlayingDistribution<>(ucbDistribution.getFirst().get(index), index, ucbDistribution.getSecond(), ucbDistribution.getThird(), () -> subtreeRoot -> 1);
    }
}
