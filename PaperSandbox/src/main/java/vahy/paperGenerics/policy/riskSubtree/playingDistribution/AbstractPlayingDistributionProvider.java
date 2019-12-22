package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTriple;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public abstract class AbstractPlayingDistributionProvider<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    implements PlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    protected static final double TOLERANCE = Math.pow(10, -15);

    protected final List<TAction> playerActions;
    protected final SplittableRandom random;
    protected final double temperature;

    protected AbstractPlayingDistributionProvider(List<TAction> playerActions, SplittableRandom random, double temperature) {
        this.playerActions = playerActions;
        this.random = random;
        this.temperature = temperature;
    }

    protected ImmutableTriple<List<TAction>, double[], double[]> getUcbVisitDistribution(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        double totalVisitSum = node
            .getChildNodeStream()
            .mapToDouble(x -> x.getSearchNodeMetadata().getVisitCounter())
            .sum();
        return createDistributionAsArray(node
            .getChildNodeStream()
            .map(x -> new ImmutableTriple<>(x.getAppliedAction(), x.getSearchNodeMetadata().getVisitCounter() / totalVisitSum, 1.0d))
            .collect(Collectors.toList()));
    }

    protected ImmutableTriple<List<TAction>, double[], double[]> getUcbValueDistribution(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        // TODO: remove code redundancy
        int childCount = node.getChildNodeMap().size();
//        double min = Double.MAX_VALUE;
//        double max = -Double.MIN_VALUE;

        double min = node.getChildNodeStream().mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward() + x.getSearchNodeMetadata().getGainedReward()).min().orElseThrow(() -> new IllegalStateException("Min does not exist"));
        double max = node.getChildNodeStream().mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward() + x.getSearchNodeMetadata().getGainedReward()).max().orElseThrow(() -> new IllegalStateException("Min does not exist"));

//        double totalValueSum = node
//            .getChildNodeStream()
//            .mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward() + x.getSearchNodeMetadata().getGainedReward())
//            .sum();
        return createDistributionAsArray(node
            .getChildNodeStream()
            .map(x -> new ImmutableTriple<>(x.getAppliedAction(), min == max ? 1.0 / childCount : (((x.getSearchNodeMetadata().getExpectedReward() + x.getSearchNodeMetadata().getGainedReward()) - min) / (max - min)), 1.0d))
            .collect(Collectors.toList()));
    }

    protected ImmutableTriple<List<TAction>, double[], double[]> createDistributionAsArray(List<ImmutableTriple<TAction, Double, Double>> actionDistribution) {
        var actionVector = new double[playerActions.size()];
        var riskVector = new double[playerActions.size()];
        var actionList = new ArrayList<TAction>(playerActions.size());
        for (ImmutableTriple<TAction, Double, Double> entry : actionDistribution) {
            int actionIndex = entry.getFirst().getActionIndexInPlayerActions();
            actionList.add(actionIndex, entry.getFirst());
            actionVector[actionIndex] = entry.getSecond();
            riskVector[actionIndex] = entry.getThird();
        }
        return new ImmutableTriple<>(actionList, actionVector, riskVector);
    }

}
