package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;

public class ExplorationUcbValueSamplingProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final Class<TAction> clazz;

    public ExplorationUcbValueSamplingProvider(Class<TAction> clazz) {
        super(true);
        this.clazz = clazz;
    }

    @Override
    public PlayingDistributionWithActionMap<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed)
    {
        int inGameEntityId = node.getStateWrapper().getInGameEntityId();
        var childNodeMap = node.getChildNodeMap();
        int childCount = childNodeMap.size();
        var actionList = new ArrayList<TAction>(childCount);

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
        double[] rewardArray = new double[childCount];

        if(max > min) {
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
            return getActionPlayingDistributionWithWithActionMap(inGameEntityId, childNodeMap, actionList, rewardArray, index);
        } else {
            for (Map.Entry<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> entry : childNodeMap.entrySet()) {
                actionList.add(entry.getKey());
            }
            Arrays.fill(rewardArray, 1.0 / rewardArray.length);
            var index = random.nextInt(childCount);
            return getActionPlayingDistributionWithWithActionMap(inGameEntityId, childNodeMap, actionList, rewardArray, index);
        }
    }



    private PlayingDistributionWithActionMap<TAction> getActionPlayingDistributionWithWithActionMap(int inGameEntityId,
                                                                                                    Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childMap,
                                                                                                    ArrayList<TAction> actionList,
                                                                                                    double[] distributionAsArray,
                                                                                                    int index)
    {
        var action = actionList.get(index);
        EnumMap<TAction, Double> enumMap = new EnumMap<>(clazz);
        for (int i = 0; i < actionList.size(); i++) { // TODO: sample action right away from enum map
            enumMap.put(actionList.get(i), distributionAsArray[i]);
        }
        return new PlayingDistributionWithActionMap<>(action, childMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId], distributionAsArray, enumMap);
    }
}
