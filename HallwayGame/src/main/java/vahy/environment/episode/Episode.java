package vahy.environment.episode;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;

import java.util.LinkedList;
import java.util.List;

public class Episode {

    private final ImmutableStateImpl initialState;
    private final IOneHotPolicy playerPolicy;

    public Episode(ImmutableStateImpl initialState, IOneHotPolicy playerPolicy) {
        this.initialState = initialState;
        this.playerPolicy = playerPolicy;
    }

    public List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> runEpisode() {
        ImmutableStateImpl state = this.initialState;
        List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> stepHistory = new LinkedList<>();
        while(!state.isFinalState()) {
            ActionType action = playerPolicy.getDiscreteAction(state);
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = state.applyAction(action);
            state = (ImmutableStateImpl) stateRewardReturn.getState();
            stepHistory.add(stateRewardReturn);
            if(!state.isFinalState()) {
                stateRewardReturn =  state.applyEnvironmentAction();
                stepHistory.add(stateRewardReturn);
                state = (ImmutableStateImpl) stateRewardReturn.getState();
            }
        }
        return stepHistory;
    }
}
