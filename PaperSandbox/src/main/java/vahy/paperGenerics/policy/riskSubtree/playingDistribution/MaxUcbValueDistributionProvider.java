package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class MaxUcbValueDistributionProvider<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    public MaxUcbValueDistributionProvider() {
        super(false);
    }

    @Override
    public PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node,
        double temperature,
        SplittableRandom random,
        double totalRiskAllowed)
    {

        int childCount = node.getChildNodeMap().size();
        double originMin = node.getChildNodeStream().mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward() + x.getSearchNodeMetadata().getGainedReward()).min().orElseThrow(() -> new IllegalStateException("Min does not exist"));
        double originMax = node.getChildNodeStream().mapToDouble(x -> x.getSearchNodeMetadata().getExpectedReward() + x.getSearchNodeMetadata().getGainedReward()).max().orElseThrow(() -> new IllegalStateException("Min does not exist"));

        List<TAction> actionList = new ArrayList<>(childCount);
        double[] rewardArray = new double[childCount];
        double[] riskArray = new double[childCount];

        int j = 0;
        for (Map.Entry<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
            actionList.add(entry.getKey());
            var metadata = entry.getValue().getSearchNodeMetadata();
            rewardArray[j] = originMin == originMax ? 1.0 / childCount : (((metadata.getExpectedReward() + metadata.getGainedReward()) - originMin) / (originMax - originMin));
            riskArray[j] = 1.0d;
            j++;
        }

        var max = rewardArray[0];
        var index = 0;
        for (int i = 1; i < childCount; i++) {
            var value = rewardArray[i];
            if(max < value) {
                max = value;
                index = i;
            }
        }
        return new PlayingDistribution<>(actionList.get(index), index, rewardArray, riskArray, actionList, () -> subtreeRoot -> 1);
    }
}
