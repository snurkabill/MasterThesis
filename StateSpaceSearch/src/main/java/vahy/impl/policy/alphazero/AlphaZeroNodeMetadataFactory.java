package vahy.impl.policy.alphazero;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;

import java.util.EnumMap;

public class AlphaZeroNodeMetadataFactory<
    TAction extends Enum<TAction> & Action,
    TObservation extends DoubleVector,
    TState extends State<TAction, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TObservation, AlphaZeroNodeMetadata<TAction>, TState> {

    private final Class<TAction> actionClazz;
    private final int inGameEntityCount;
    private final int totalActionCount;

    public AlphaZeroNodeMetadataFactory(Class<TAction> actionClazz, int inGameEntityCount) {
        this.actionClazz = actionClazz;
        this.inGameEntityCount = inGameEntityCount;
        this.totalActionCount = actionClazz.getEnumConstants().length;
    }

    public int getTotalActionCount() {
        return totalActionCount;
    }

    @Override
    public int getInGameEntityCount() {
        return inGameEntityCount;
    }

    @Override
    public AlphaZeroNodeMetadata<TAction> createEmptyNodeMetadata() {
        return new AlphaZeroNodeMetadata<TAction>(
            DoubleVectorRewardAggregator.emptyReward(inGameEntityCount),
            DoubleVectorRewardAggregator.emptyReward(inGameEntityCount),
            Double.NaN,
            new EnumMap<TAction, Double>(actionClazz));
    }

    @Override
    public AlphaZeroNodeMetadata<TAction> createSearchNodeMetadata(SearchNode<TAction, TObservation, AlphaZeroNodeMetadata<TAction>, TState> parent,
                                                                   StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn,
                                                                   TAction appliedAction) {
        var allPlayerRewards = stateRewardReturn.getAllPlayerRewards();
        if(parent == null) {
            return new AlphaZeroNodeMetadata<TAction>(
                allPlayerRewards,
                allPlayerRewards,
                Double.NaN,
                new EnumMap<TAction, Double>(actionClazz));
        } else {
            var metadata = parent.getSearchNodeMetadata();
            return new AlphaZeroNodeMetadata<TAction>(
                DoubleVectorRewardAggregator.aggregate(metadata.getCumulativeReward(), stateRewardReturn.getAllPlayerRewards()),
                allPlayerRewards,
                metadata.getChildPriorProbabilities().get(appliedAction),
                new EnumMap<TAction, Double>(actionClazz));
        }


    }
}
