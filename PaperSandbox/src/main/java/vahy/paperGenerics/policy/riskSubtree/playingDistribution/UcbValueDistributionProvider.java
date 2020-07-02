package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.api.policy.RandomizedPolicy;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

public class UcbValueDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    public UcbValueDistributionProvider(boolean applyTemperature) {
        super(applyTemperature);
    }

    @Override
    public PlayingDistribution<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed)
    {
        int inGameEntityId = node.getStateWrapper().getInGameEntityIdWrapper();
        var childNodeMap = node.getChildNodeMap();
        int childCount = childNodeMap.size();

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> entry : childNodeMap.values()) {
            var metadata = entry.getSearchNodeMetadata();
            var value = metadata.getExpectedReward()[inGameEntityId] + metadata.getGainedReward()[inGameEntityId];
            if(value < min) {
                min = value;
            }
            if(value > max) {
                max = value;
            }
        }

        if(min == max) {
            var index = random.nextInt(childCount);
            TAction[] allPossibleActions = node.getAllPossibleActions();
            var action = allPossibleActions[index];
            return new PlayingDistribution<>(action, childNodeMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId], RandomizedPolicy.EMPTY_ARRAY);
        } else {
            List<TAction> actionList = new ArrayList<>(childCount);
            double[] rewardArray = new double[childCount];
            int j = 0;
            for (Map.Entry<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> entry : childNodeMap.entrySet()) {
                actionList.add(entry.getKey());
                var metadata = entry.getValue().getSearchNodeMetadata();
                rewardArray[j] = (((metadata.getExpectedReward()[inGameEntityId] + metadata.getGainedReward()[inGameEntityId]) - min) / (max - min));
                j++;
            }
            if(applyTemperature) {
                RandomDistributionUtils.applyBoltzmannNoise(rewardArray, temperature);
            }
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(rewardArray, random);
            TAction action = actionList.get(index);
            return new PlayingDistribution<>(action, childNodeMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId],RandomizedPolicy.EMPTY_ARRAY);
        }
    }
}
