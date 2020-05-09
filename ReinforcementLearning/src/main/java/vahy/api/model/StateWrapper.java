package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateWrapperRewardReturn;

public class StateWrapper<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    private final int policyId;
    private final TState state;

    public StateWrapper(int policyId, TState state) {
        this.policyId = policyId;
        this.state = state;
    }

    public TState getWrappedState() {
        return state;
    }

    public TAction[] getAllPossibleActions() {
        return state.getAllPossibleActions();
    }

    public StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction actionType) {
        StateRewardReturn<TAction, TObservation, TState> stateRewardReturn = state.applyAction(actionType);
        return new ImmutableStateWrapperRewardReturn<>(new StateWrapper<>(policyId, stateRewardReturn.getState()), stateRewardReturn.getReward());
    }

    public TObservation getObservation() {
        return state.getPlayerObservation(policyId);
    }

    public TObservation getCommonObservation() {
        return state.getCommonObservation(policyId);
    }

    public Predictor<TState> getKnownModelWithPerfectObservationPredictor() {
        return state.getKnownModelWithPerfectObservationPredictor();
    }

    public int getPlayerIdWrapper() {
        return policyId;
    }

    public boolean isPlayerTurn() {
        return state.getPlayerIdOnTurn() == policyId;
    }

    public boolean isFinalState() {
        if(state.isFinalState()) {
            return true;
        }
        return state.isInGame(policyId);
    }

}
