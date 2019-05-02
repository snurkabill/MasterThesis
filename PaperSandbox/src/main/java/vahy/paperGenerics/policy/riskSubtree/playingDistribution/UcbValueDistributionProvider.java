package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.utils.RandomDistributionUtils;

import java.util.List;
import java.util.SplittableRandom;

public class UcbValueDistributionProvider<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    public UcbValueDistributionProvider(List<TAction> playerActions, SplittableRandom random) {
        super(playerActions, random);
    }

    @Override
    public PlayingDistribution<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node)
    {
        var ucbDistribution = getUcbValueDistribution(node);
        int index = RandomDistributionUtils.getRandomIndexFromDistribution(ucbDistribution.getSecond(), random);
        return new PlayingDistribution<>(ucbDistribution.getFirst().get(index), index, ucbDistribution.getSecond(), ucbDistribution.getThird(), () -> subtreeRoot -> 1);
    }
}
