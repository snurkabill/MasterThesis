package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.policy.RandomizedPolicy;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.RandomDistributionUtils;

import java.util.Map;
import java.util.SplittableRandom;

public class UcbVisitDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    public UcbVisitDistributionProvider(boolean applyTemperature) {
        super(applyTemperature);
    }

    @Override
    public PlayingDistribution<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed)
    {
        int inGameEntityId = node.getStateWrapper().getInGameEntityId();
        Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childNodeMap = node.getChildNodeMap();
        int childCount = childNodeMap.size();
        int totalVisitSum = 0;
        for (SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> entry : childNodeMap.values()) {
            totalVisitSum += entry.getSearchNodeMetadata().getVisitCounter();
        }

        double[] distribution = new double[childCount];
        int j = 0;
        for (Map.Entry<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> entry : childNodeMap.entrySet()) {
            var metadata = entry.getValue().getSearchNodeMetadata();
            distribution[j] = metadata.getVisitCounter() / (double) totalVisitSum;
            j++;
        }

        if(applyTemperature) {
            RandomDistributionUtils.applyBoltzmannNoise(distribution, temperature);
        }

        int index = RandomDistributionUtils.getRandomIndexFromDistribution(distribution, random);
        TAction[] allPossibleActions = node.getAllPossibleActions();
        TAction action = allPossibleActions[index];
        return new PlayingDistribution<>(action, childNodeMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId], RandomizedPolicy.EMPTY_ARRAY);
    }

}
