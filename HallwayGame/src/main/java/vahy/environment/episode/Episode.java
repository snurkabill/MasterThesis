package vahy.environment.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(Episode.class);
    private final ImmutableStateImpl initialState;
    private final IOneHotPolicy playerPolicy;

    public Episode(ImmutableStateImpl initialState, IOneHotPolicy playerPolicy) {
        this.initialState = initialState;
        this.playerPolicy = playerPolicy;
    }

    public List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> runEpisode() {
        ImmutableStateImpl state = this.initialState;
        List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> stepHistory = new LinkedList<>();
        int playerActionCount = 0;
        while(!state.isFinalState()) {
            ActionType action = playerPolicy.getDiscreteAction(state);
            playerActionCount++;
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = state.applyAction(action);
            logger.info("Player's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().getValue());
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
