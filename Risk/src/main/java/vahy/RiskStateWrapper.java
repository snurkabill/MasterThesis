package vahy;

import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.StateWrapper;
import vahy.api.model.StateWrapperRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.impl.model.ImmutableStateWrapperRewardReturn;

public class RiskStateWrapper<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends RiskState<TAction, TObservation, TState>> extends StateWrapper<TAction, TObservation, TState> {

    public RiskStateWrapper(int inGameEntityId, TState state) {
        super(inGameEntityId, state);
    }

    public RiskStateWrapper(int inGameEntityId, int aggregateObservationCount, TState state) {
        super(inGameEntityId, aggregateObservationCount, state);
    }

    @Override
    public StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction actionType) {
        StateRewardReturn<TAction, TObservation, TState> stateRewardReturn = state.applyAction(actionType);
        return new ImmutableStateWrapperRewardReturn<>(new RiskStateWrapper<>(inGameEntityId, stateRewardReturn.getState()), stateRewardReturn.getReward()[inGameEntityId], stateRewardReturn.getReward(), stateRewardReturn.getAction()[inGameEntityId]);
    }

    public boolean isRiskHit() {
        return state.isRiskHit(inGameEntityId);
    }

    public boolean[] getRiskVector() {
        return state.getRiskVector();
    }

}
