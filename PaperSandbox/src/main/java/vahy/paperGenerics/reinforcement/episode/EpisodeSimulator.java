package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateActionReward;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.impl.model.ImmutableStateActionRewardTuple;
import vahy.impl.model.observation.DoubleVector;
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
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeSimulator.class.getName());

    public EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState> calculateEpisode(
        EpisodeImmutableSetup<TAction, TPlayerObservation, TOpponentObservation, TState> episodeImmutableSetup)
    {
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> playerPolicy = episodeImmutableSetup.getPlayerPaperPolicy();
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> opponentPolicy = episodeImmutableSetup.getOpponentPolicy();

        List<StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeStateRewardReturnList = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<TAction, TPlayerObservation, TOpponentObservation, TState>, StepRecord>> episodeHistoryList = new ArrayList<>();

        TState state = episodeImmutableSetup.getInitialState();
        logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        long millis = episodeRun(episodeImmutableSetup, playerPolicy, opponentPolicy, episodeStateRewardReturnList, episodeHistoryList, state);
        return new EpisodeResults<>(episodeStateRewardReturnList, episodeHistoryList, millis);
    }

    private long episodeRun(EpisodeImmutableSetup<TAction, TPlayerObservation, TOpponentObservation, TState> episodeImmutableSetup,
                            PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> playerPolicy,
                            PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> opponentPolicy,
                            List<StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeStateRewardReturnList,
                            List<ImmutableTuple<StateActionReward<TAction, TPlayerObservation, TOpponentObservation, TState>, StepRecord>> episodeHistoryList,
                            TState state) {
        try {
            int playerActionCount = 0;
            long start = System.currentTimeMillis();
            int stepsDone = 0;
            while(!state.isFinalState()) {
                if(stepsDone >= episodeImmutableSetup.getStepCountLimit()) {
                    break;
                }
                var step = makePolicyStep(state, playerPolicy, opponentPolicy);
                playerActionCount++;
                stepsDone++;
                logger.debug("Player's [{}]th action: [{}] getting reward [{}]. ExpectedReward: [{}], Expected risk: [{}], PolicyProbabilities: [{}], PriorProbabilities: [{}]",
                    playerActionCount,
                    step.getFirst(),
                    step.getThird().getReward(),
                    step.getSecond().getRewardPredicted(),
                    step.getSecond().getRisk(),
                    Arrays.toString(step.getSecond().getPolicyProbabilities()),
                    Arrays.toString(step.getSecond().getPriorProbabilities()));
                episodeStateRewardReturnList.add(step.getThird());
                episodeHistoryList.add(new ImmutableTuple<>(new ImmutableStateActionRewardTuple<>(state, step.getFirst(), step.getThird().getReward()), step.getSecond()));
                state = step.getThird().getState();
                if(!state.isFinalState()) {
                    step = makePolicyStep(state, opponentPolicy, playerPolicy);
                    logger.debug("Opponent's [{}]th action: [{}], getting reward [{}]", playerActionCount, step.getFirst(), step.getThird().getReward());
                    episodeStateRewardReturnList.add(step.getThird());
                    episodeHistoryList.add(new ImmutableTuple<>(new ImmutableStateActionRewardTuple<>(state, step.getFirst(), step.getThird().getReward()), step.getSecond()));
                    state = step.getThird().getState();
                }
                logger.debug("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerActionCount);
            }
            long end = System.currentTimeMillis();
            return end - start;
        } catch(Exception e) {
            throw new IllegalStateException("Episode simulation ended due to inner exception. Episode steps: [" +
                episodeHistoryList
                    .stream()
                    .map(x -> x.getFirst().getAction().toString())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Episode does not contain any states as history")
                +
                "] with the executed episode history: " +
                episodeHistoryList
                    .stream()
                    .map(x -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append("----------------------------------------------------------------------------------------------------")
                                .append(System.lineSeparator())
                                .append("State information: ")
                                .append(System.lineSeparator())
                                .append("Is player turn: ")
                                .append(x.getFirst().getState().isPlayerTurn())
                                .append(System.lineSeparator())
                                .append(x.getFirst().getState().readableStringRepresentation())
                                .append(System.lineSeparator())
                                .append("Policy probabilities: ")
                                .append(System.lineSeparator())
                                .append(Arrays.toString(x.getSecond().getPolicyProbabilities()))
                                .append(System.lineSeparator())
                                .append("Prior probabilities: ")
                                .append(System.lineSeparator())
                                .append(Arrays.toString(x.getSecond().getPriorProbabilities()))
                                .append(System.lineSeparator())
                                .append("Policy reward : ")
                                .append(System.lineSeparator())
                                .append(x.getSecond().getRewardPredicted())
                                .append(System.lineSeparator())
                                .append("Policy risk : ")
                                .append(System.lineSeparator())
                                .append(x.getSecond().getRisk())
                                .append(System.lineSeparator())
                                .append("Applied action: ")
                                .append(System.lineSeparator())
                                .append(x.getFirst().getAction())
                                .append(System.lineSeparator())
                                .append("Getting reward: ")
                                .append(System.lineSeparator())
                                .append(x.getFirst().getReward())
                                .append(System.lineSeparator());
                            sb.append("----------------------------------------------------------------------------------------------------");
                            return sb.toString();
                        }
                    ).reduce((a, b) -> a + System.lineSeparator() + b)
                , e);
        }
    }

    private ImmutableTriple<
        TAction,
        StepRecord,
        StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState>> makePolicyStep(TState state,
                                                                                  PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> onTurnPolicy,
                                                                                  PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> otherPolicy) {
        TAction action = onTurnPolicy.getDiscreteAction(state);
        StepRecord playerStepRecord = createStepRecord(state, onTurnPolicy);
        onTurnPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        otherPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn = state.applyAction(action);
        return new ImmutableTriple<>(action, playerStepRecord, stateRewardReturn);
    }

    private StepRecord createStepRecord(TState state, PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> playerPolicy) {
        double[] actionProbabilities = playerPolicy.getActionProbabilityDistribution(state);
        double[] priorProbabilities = playerPolicy.getPriorActionProbabilityDistribution(state);
        double estimatedReward = playerPolicy.getEstimatedReward(state);
        double estimatedRisk = playerPolicy.getEstimatedRisk(state);
        return new StepRecord(priorProbabilities, actionProbabilities, estimatedReward, estimatedRisk);
    }

}
