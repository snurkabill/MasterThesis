package vahy.environment.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.AbstractTreeSearchPolicy;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;

import java.util.LinkedList;
import java.util.List;

public class Episode {

    private static final Logger logger = LoggerFactory.getLogger(Episode.class);
    private final State<ActionType, DoubleScalarReward, DoubleVectorialObservation> initialState;
    private final AbstractTreeSearchPolicy<EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>> playerPolicy;
    private final IOneHotPolicy opponentPolicy;

    public Episode(
        State<ActionType, DoubleScalarReward, DoubleVectorialObservation> initialState,
        AbstractTreeSearchPolicy<EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>> playerPolicy,
        IOneHotPolicy opponentPolicy) {
        this.initialState = initialState;
        this.playerPolicy = playerPolicy;
        this.opponentPolicy = opponentPolicy;
    }

    public List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> runEpisode() {
        State<ActionType, DoubleScalarReward, DoubleVectorialObservation> state = this.initialState;
        List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> stepHistory = new LinkedList<>();
        int playerActionCount = 0;
        while(!state.isFinalState()) {
            ActionType action = playerPolicy.getDiscreteAction(state);
            playerPolicy.applyAction(action);
            playerActionCount++;
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = state.applyAction(action);
            logger.info("Player's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().getValue());
            state = stateRewardReturn.getState();
            stepHistory.add(stateRewardReturn);
            if(!state.isFinalState()) {
                action = opponentPolicy.getDiscreteAction(state);
                stateRewardReturn = state.applyAction(action);
                playerPolicy.applyAction(action);
                logger.info("Environment's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().getValue());
                stepHistory.add(stateRewardReturn);
                state = stateRewardReturn.getState();
            }
        }
        return stepHistory;
    }
}
