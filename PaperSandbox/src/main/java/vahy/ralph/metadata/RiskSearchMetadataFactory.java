package vahy.ralph.metadata;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeMetadataFactory;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;

import java.util.EnumMap;

public class RiskSearchMetadataFactory<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends RiskState<TAction, TObservation, TState>>
    implements SearchNodeMetadataFactory<TAction, TObservation, RalphMetadata<TAction>, TState> {

    private final Class<TAction> actionClazz;
    private final int inGameEntityCount;
    private final int totalActionCount;

    public RiskSearchMetadataFactory(Class<TAction> actionClazz, int inGameEntityCount) {
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
    public RalphMetadata<TAction> createSearchNodeMetadata(SearchNode<TAction, TObservation, RalphMetadata<TAction>, TState> parent,
                                                           StateWrapperRewardReturn<TAction, TObservation, TState> stateRewardReturn,
                                                           TAction appliedAction)
    {
        StateWrapper<TAction, TObservation, TState> stateWrapper = stateRewardReturn.getState();
        var riskVector = stateWrapper.getWrappedState().getRiskVector();
        var riskAsDoubles = new double[riskVector.length];
        for (int i = 0; i < riskAsDoubles.length; i++) {
            riskAsDoubles[i] = riskVector[i] ? 1.0 : 0.0;
        }
        var allPlayerRewards = stateRewardReturn.getAllPlayerRewards();
        var metadata = parent.getSearchNodeMetadata();
        return new RalphMetadata<>(
            DoubleVectorRewardAggregator.aggregate(metadata.getCumulativeReward(), stateRewardReturn.getAllPlayerRewards()),
            allPlayerRewards,
            metadata.getChildPriorProbabilities().size() == 0 ? Double.NaN : metadata.getChildPriorProbabilities().get(appliedAction),
            riskAsDoubles,
            new EnumMap<>(actionClazz)
        );
    }

    @Override
    public RalphMetadata<TAction> createEmptyNodeMetadata() {
        return new RalphMetadata<TAction>(
            DoubleVectorRewardAggregator.emptyReward(inGameEntityCount),
            DoubleVectorRewardAggregator.emptyReward(inGameEntityCount),
            Double.NaN,
            new double[inGameEntityCount],
            new EnumMap<TAction, Double>(actionClazz));
    }
}
