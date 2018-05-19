package vahy.impl.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.Episode;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EpisodeImpl<TAction extends Action, TReward extends Reward, TObservation extends Observation> implements Episode<TAction, TReward, TObservation> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeImpl.class);
    private final State<TAction, TReward, TObservation> initialState;
    private final Policy<TAction, TReward, TObservation> playerPolicy;
    private final Policy<TAction, TReward, TObservation> opponentPolicy;

    public EpisodeImpl(
        State<TAction, TReward, TObservation> initialState,
        Policy<TAction, TReward, TObservation> playerPolicy,
        Policy<TAction, TReward, TObservation> opponentPolicy) {
        this.initialState = initialState;
        this.playerPolicy = playerPolicy;
        this.opponentPolicy = opponentPolicy;
    }

    public List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> runEpisode() {
        State<TAction, TReward, TObservation> state = this.initialState;
        logger.info("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> stepHistory = new LinkedList<>();
        int playerActionCount = 0;
        while(!state.isFinalState()) {
            TAction action = playerPolicy.getDiscreteAction(state);
            playerPolicy.updateStateOnOpponentActions(Collections.singletonList(action));
            playerActionCount++;
            StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = state.applyAction(action);
            logger.info("Player's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().toPrettyString());
            state = stateRewardReturn.getState();
            stepHistory.add(stateRewardReturn);
            if(!state.isFinalState()) {
                action = opponentPolicy.getDiscreteAction(state);
                stateRewardReturn = state.applyAction(action);
                playerPolicy.updateStateOnOpponentActions(Collections.singletonList(action));
                logger.info("Environment's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().toPrettyString());
                stepHistory.add(stateRewardReturn);
                state = stateRewardReturn.getState();
            }
            logger.info("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerActionCount);
        }
        return stepHistory;
    }
}
