package vahy.AlphaGo.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.AlphaGo.policy.AlphaGoPolicy;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.environment.EnvironmentPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.ImmutableStateActionRewardTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AlphaGoEpisode {

    private static final Logger logger = LoggerFactory.getLogger(AlphaGoEpisode.class);
    private final ImmutableStateImpl initialState;
    private final AlphaGoPolicy playerPolicy;
    private final EnvironmentPolicy opponentPolicy;

    private List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> episodeStateRewardReturnList = new ArrayList<>();
    private List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, AlphaGoStepRecord>> episodeHistoryList = new ArrayList<>();

    private boolean episodeAlreadySimulated = false;

    public AlphaGoEpisode(
        ImmutableStateImpl initialState,
        AlphaGoPolicy playerPolicy,
        EnvironmentPolicy opponentPolicy) {
        this.initialState = initialState;
        this.playerPolicy = playerPolicy;
        this.opponentPolicy = opponentPolicy;
    }

    public void runEpisode() {
        if(episodeAlreadySimulated) {
            throw new IllegalStateException("Episode was already simulated");
        }
        ImmutableStateImpl state = this.initialState;
        logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        int playerActionCount = 0;
        while(!state.isFinalState()) {
            ActionType action = playerPolicy.getDiscreteAction(state);
            double[] actionProbabilities = playerPolicy.getActionProbabilityDistribution(state);
            double[] priorProbabilities = playerPolicy.getPriorActionProbabilityDistribution(state);
            DoubleScalarReward estimatedReward = playerPolicy.getEstimatedReward(state);
            playerPolicy.updateStateOnOpponentActions(Collections.singletonList(action));
            playerActionCount++;
            StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> stateRewardReturn = state.applyAction(action);
            logger.debug("Player's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().toPrettyString());
            episodeStateRewardReturnList.add(stateRewardReturn);
            episodeHistoryList.add(new ImmutableTuple<>(new ImmutableStateActionRewardTuple<>(state, action, stateRewardReturn.getReward()), new AlphaGoStepRecord(priorProbabilities, actionProbabilities, estimatedReward)));
            state = (ImmutableStateImpl) stateRewardReturn.getState();
            if(!state.isFinalState()) {
                action = opponentPolicy.getDiscreteAction(state);
                actionProbabilities = playerPolicy.getActionProbabilityDistribution(state);
                priorProbabilities = playerPolicy.getPriorActionProbabilityDistribution(state);
                estimatedReward = playerPolicy.getEstimatedReward(state);
                stateRewardReturn = state.applyAction(action);
                playerPolicy.updateStateOnOpponentActions(Collections.singletonList(action));
                logger.debug("Environment's [{}]th action: [{}], getting reward [{}]", playerActionCount, action, stateRewardReturn.getReward().toPrettyString());
                episodeStateRewardReturnList.add(stateRewardReturn);
                episodeHistoryList.add(new ImmutableTuple<>(new ImmutableStateActionRewardTuple<>(state, action, stateRewardReturn.getReward()), new AlphaGoStepRecord(priorProbabilities, actionProbabilities, estimatedReward)));
                state = (ImmutableStateImpl) stateRewardReturn.getState();
            }
            logger.debug("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerActionCount);
        }
        logger.info("Episode actions: [" + episodeHistoryList.stream().map(x -> x.getFirst().getAction()).collect(Collectors.toList()) + "]");
        logger.info("Total reward: [" + episodeHistoryList.stream().mapToDouble(x -> x.getFirst().getReward().getValue()).sum() + "]");
        episodeAlreadySimulated = true;
    }

    public boolean isEpisodeAlreadySimulated() {
        return episodeAlreadySimulated;
    }

    public List<StateRewardReturn<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> getEpisodeStateRewardReturnList() {
        return this.episodeStateRewardReturnList;
    }

    public List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, AlphaGoStepRecord>> getEpisodeStateActionRewardList() {
        return episodeHistoryList;
    }

    public State<ActionType, DoubleScalarReward, DoubleVectorialObservation> getFinalState() {
        return this.episodeStateRewardReturnList.get(episodeStateRewardReturnList.size() - 1).getState();
    }

}
