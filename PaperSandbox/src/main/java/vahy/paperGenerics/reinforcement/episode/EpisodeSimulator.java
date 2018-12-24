package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.ImmutableStateActionRewardTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.utils.ImmutableTriple;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EpisodeSimulator<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TState extends PaperState<TAction, TReward, TObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeSimulator.class.getName());

    public EpisodeResults<TAction, TReward, TObservation, TState> calculateEpisode(EpisodeImmutableSetup<TAction, TReward, TObservation, TState> episodeImmutableSetup) {
        PaperPolicy<TAction, TReward, TObservation, TState> playerPolicy = episodeImmutableSetup.getPlayerPaperPolicy();
        PaperPolicy<TAction, TReward, TObservation, TState> opponentPolicy = episodeImmutableSetup.getOpponentPolicy();

        List<StateRewardReturn<TAction, TReward, TObservation, TState>> episodeStateRewardReturnList = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<TAction, TReward, TObservation, TState>, StepRecord<TReward>>> episodeHistoryList = new ArrayList<>();

        TState state = episodeImmutableSetup.getInitialState();
        logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        int playerActionCount = 0;
        long start = System.currentTimeMillis();
        int stepsDone = 0;
        while(!state.isFinalState()) {
            if(stepsDone >= episodeImmutableSetup.getStepCountLimit()) {
                break;
            }
            ImmutableTriple<TAction, StepRecord<TReward>, StateRewardReturn<TAction, TReward, TObservation, TState>> step = makePolicyStep(state, playerPolicy, opponentPolicy);
            playerActionCount++;
            stepsDone++;
            logger.debug("Player's [{}]th action: [{}], getting reward [{}]", playerActionCount, step.getFirst(), step.getThird().getReward().toPrettyString());
            episodeStateRewardReturnList.add(step.getThird());
            episodeHistoryList.add(new ImmutableTuple<>(new ImmutableStateActionRewardTuple<>(state, step.getFirst(), step.getThird().getReward()), step.getSecond()));
            state = step.getThird().getState();
            if(!state.isFinalState()) {
                step = makePolicyStep(state, opponentPolicy, playerPolicy);
                logger.debug("Opponent's [{}]th action: [{}], getting reward [{}]", playerActionCount, step.getFirst(), step.getThird().getReward().toPrettyString());
                episodeStateRewardReturnList.add(step.getThird());
                episodeHistoryList.add(new ImmutableTuple<>(new ImmutableStateActionRewardTuple<>(state, step.getFirst(), step.getThird().getReward()), step.getSecond()));
                state = step.getThird().getState();
            }
            logger.debug("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerActionCount);
        }
        long end = System.currentTimeMillis();
        long millis = end - start;
        return new EpisodeResults<>(episodeStateRewardReturnList, episodeHistoryList, millis);
    }

    private ImmutableTriple<
        TAction,
        StepRecord<TReward>,
        StateRewardReturn<TAction, TReward, TObservation, TState>> makePolicyStep(TState state,
                                                                                  PaperPolicy<TAction, TReward, TObservation, TState> onTurnPolicy,
                                                                                  PaperPolicy<TAction, TReward, TObservation, TState> otherPolicy) {
        TAction action = onTurnPolicy.getDiscreteAction(state);
        StepRecord<TReward> playerStepRecord = createStepRecord(state, onTurnPolicy);
        onTurnPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        otherPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        StateRewardReturn<TAction, TReward, TObservation, TState> stateRewardReturn = state.applyAction(action);
        return new ImmutableTriple<>(action, playerStepRecord, stateRewardReturn);
    }

    private StepRecord<TReward> createStepRecord(TState state, PaperPolicy<TAction, TReward, TObservation, TState> playerPolicy) {
        double[] actionProbabilities = playerPolicy.getActionProbabilityDistribution(state);
        double[] priorProbabilities = playerPolicy.getPriorActionProbabilityDistribution(state);
        TReward estimatedReward = playerPolicy.getEstimatedReward(state);
        double estimatedRisk = playerPolicy.getEstimatedRisk(state);
        return new StepRecord<>(priorProbabilities, actionProbabilities, estimatedReward, estimatedRisk);
    }

}
