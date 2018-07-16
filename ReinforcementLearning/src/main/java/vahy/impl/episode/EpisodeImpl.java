package vahy.impl.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.Episode;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;
import vahy.impl.model.ImmutableStateActionRewardTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EpisodeImpl<TAction extends Action, TReward extends Reward, TObservation extends Observation> implements Episode<TAction, TReward, TObservation> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeImpl.class);
    private final State<TAction, TReward, TObservation> initialState;
    private final Policy<TAction, TReward, TObservation> playerPolicy;
    private final Policy<TAction, TReward, TObservation> opponentPolicy;

    private List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> episodeStateRewardReturnList = new ArrayList<>();
    private List<StateActionReward<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> episodeHistoryList = new ArrayList<>();

    private boolean episodeAlreadySimulated = false;

    public EpisodeImpl(
        State<TAction, TReward, TObservation> initialState,
        Policy<TAction, TReward, TObservation> playerPolicy,
        Policy<TAction, TReward, TObservation> opponentPolicy) {
        this.initialState = initialState;
        this.playerPolicy = playerPolicy;
        this.opponentPolicy = opponentPolicy;
    }

    public void runEpisode() {
        if(episodeAlreadySimulated) {
            throw new IllegalStateException("Episode was already simulated");
        }
        State<TAction, TReward, TObservation> state = this.initialState;
        logger.debug("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        int playerActionCount = 0;
        while(!state.isFinalState()) {
            TAction action = playerPolicy.getDiscreteAction(state);
            playerPolicy.updateStateOnOpponentActions(Collections.singletonList(action));
            playerActionCount++;
            StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>> stateRewardReturn = state.applyAction(action);
            logger.info("Player's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().toPrettyString());
            episodeStateRewardReturnList.add(stateRewardReturn);
            episodeHistoryList.add(new ImmutableStateActionRewardTuple<>(state, action, stateRewardReturn.getReward()));
            state = stateRewardReturn.getState();
            if(!state.isFinalState()) {
                action = opponentPolicy.getDiscreteAction(state);
                stateRewardReturn = state.applyAction(action);
                playerPolicy.updateStateOnOpponentActions(Collections.singletonList(action));
                logger.info("Environment's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().toPrettyString());
                episodeStateRewardReturnList.add(stateRewardReturn);
                episodeHistoryList.add(new ImmutableStateActionRewardTuple<>(state, action, stateRewardReturn.getReward()));
                state = stateRewardReturn.getState();
            }
            logger.info("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerActionCount);
        }
        episodeAlreadySimulated = true;
    }

    @Override
    public boolean isEpisodeAlreadySimulated() {
        return episodeAlreadySimulated;
    }

    @Override
    public List<StateRewardReturn<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> getEpisodeStateRewardReturnList() {
        return this.episodeStateRewardReturnList;
    }

    @Override
    public List<StateActionReward<TAction, TReward, TObservation, State<TAction, TReward, TObservation>>> getEpisodeStateActionRewardList() {
        return episodeHistoryList;
    }

    @Override
    public State<TAction, TReward, TObservation> getFinalState() {
        return this.episodeStateRewardReturnList.get(episodeStateRewardReturnList.size() - 1).getState();
    }

}
