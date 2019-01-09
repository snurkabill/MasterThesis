package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.impl.model.ImmutableStateActionRewardTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.utils.ImmutableTriple;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EpisodeSimulator<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeSimulator.class.getName());

    public EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> calculateEpisode(EpisodeImmutableSetup<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> episodeImmutableSetup) {
        PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPolicy = episodeImmutableSetup.getPlayerPaperPolicy();
        PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicy = episodeImmutableSetup.getOpponentPolicy();

        List<StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> episodeStateRewardReturnList = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>, StepRecord<TReward>>> episodeHistoryList = new ArrayList<>();

        TState state = episodeImmutableSetup.getInitialState();
        logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        int playerActionCount = 0;
        long start = System.currentTimeMillis();
        int stepsDone = 0;
        while(!state.isFinalState()) {
            if(stepsDone >= episodeImmutableSetup.getStepCountLimit()) {
                break;
            }
            ImmutableTriple<TAction, StepRecord<TReward>, StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> step = makePolicyStep(state, playerPolicy, opponentPolicy);
            playerActionCount++;
            stepsDone++;
            logger.debug("Player's [{}]th action: [{}] getting reward [{}]. ExpectedReward: [{}], Expected risk: [{}], PolicyProbabilities: [{}], PriorProbabilities: [{}]",
                playerActionCount,
                step.getFirst(),
                step.getThird().getReward().getValue(),
                step.getSecond().getRewardPredicted().  getValue(),
                step.getSecond().getRisk(),
                Arrays.toString(step.getSecond().getPolicyProbabilities()),
                Arrays.toString(step.getSecond().getPriorProbabilities()));
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
        StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> makePolicyStep(TState state,
                                                                                  PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> onTurnPolicy,
                                                                                  PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> otherPolicy) {
        TAction action = onTurnPolicy.getDiscreteAction(state);
        StepRecord<TReward> playerStepRecord = createStepRecord(state, onTurnPolicy);
        onTurnPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        otherPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        StateRewardReturn<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn = state.applyAction(action);
        return new ImmutableTriple<>(action, playerStepRecord, stateRewardReturn);
    }

    private StepRecord<TReward> createStepRecord(TState state, PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPolicy) {
        double[] actionProbabilities = playerPolicy.getActionProbabilityDistribution(state);
        double[] priorProbabilities = playerPolicy.getPriorActionProbabilityDistribution(state);
        TReward estimatedReward = playerPolicy.getEstimatedReward(state);
        double estimatedRisk = playerPolicy.getEstimatedRisk(state);
        return new StepRecord<>(priorProbabilities, actionProbabilities, estimatedReward, estimatedRisk);
    }

}
