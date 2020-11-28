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
import java.util.EnumMap;
import java.util.List;
import java.util.SplittableRandom;

public class InferenceFeasibleDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final Class<TAction> clazz;

    public InferenceFeasibleDistributionProvider(Class<TAction> clazz) {
        super(true);
        this.clazz = clazz;
    }

    @Override
    public PlayingDistribution<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed)
    {
        int inGameEntityId = node.getStateWrapper().getInGameEntityId();
        var childNodeMap = node.getChildNodeMap();

        int childCount = node.getChildNodeMap().size();
        List<TAction> actionList = new ArrayList<>(childCount);
        EnumMap<TAction, Double> enumMap = new EnumMap<>(clazz);
        double[] distributionArray = new double[childCount];

        int j = 0;
        for (var entry : childNodeMap.entrySet()) {
            actionList.add(entry.getKey());
            distributionArray[j] = entry.getValue().getSearchNodeMetadata().getFlow();
            j++;
        }

        RandomDistributionUtils.tryToRoundDistribution(distributionArray, TOLERANCE);
        for (int i = 0; i < actionList.size(); i++) { // TODO: sample action right away from enum map
            enumMap.put(actionList.get(i), distributionArray[i]);
        }
        int index = RandomDistributionUtils.getRandomIndexFromDistribution(distributionArray, random);
        TAction action = actionList.get(index);

        return new PlayingDistributionWithActionMap<>(action, childNodeMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId], RandomizedPolicy.EMPTY_ARRAY, enumMap);
    }
}
