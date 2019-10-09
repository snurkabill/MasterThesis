package vahy.impl.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeImmutableSetup;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeSimulator;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;
import vahy.impl.model.observation.DoubleVector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EpisodeSimulatorGeneric<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeSimulator<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeSimulator.class.getName());
    private int totalStepsDone = 0;
    private int playerStepsDone = 0;
    private double totalCumulativePayoff = 0.0;

    private final EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory;

    public EpisodeSimulatorGeneric(EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> resultsFactory) {
        this.resultsFactory = resultsFactory;
    }

    public EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> calculateEpisode(
        EpisodeImmutableSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeImmutableSetup)
    {
        Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPolicy = episodeImmutableSetup.getPlayerPaperPolicy();
        Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicy = episodeImmutableSetup.getOpponentPolicy();

        TState state = episodeImmutableSetup.getInitialState();
        logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        return episodeRun(episodeImmutableSetup, playerPolicy, opponentPolicy, state);
    }

    private EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeRun(
        EpisodeImmutableSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeImmutableSetup,
        Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPolicy,
        Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicy,
        TState state) {
        var episodeHistoryList = new ArrayList<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>();
        try {
            long start = System.currentTimeMillis();
            while(!state.isFinalState() && playerStepsDone < episodeImmutableSetup.getStepCountLimit()) {
                var step = makePolicyStep(state, playerPolicy, opponentPolicy, true);
                totalCumulativePayoff += step.getReward();
                playerStepsDone++;
                totalStepsDone++;
                if(logger.isDebugEnabled()) {
                    makeStepLog(step);
                }
                episodeHistoryList.add(step);
                state = step.getToState();
                if(!state.isFinalState()) {
                    step = makePolicyStep(state, opponentPolicy, playerPolicy, false);
                    totalCumulativePayoff += step.getReward();
                    totalStepsDone++;
                    logger.debug("Opponent's [{}]th action: [{}], getting reward [{}]", playerStepsDone, step.getPlayedAction(), step.getReward());
                    episodeHistoryList.add(step);
                    state = step.getToState();
                }
                logger.debug("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerStepsDone);
            }
            long end = System.currentTimeMillis();
            return resultsFactory.createResults(episodeHistoryList, playerStepsDone, totalStepsDone, totalCumulativePayoff, Duration.ofMillis(end - start));
        } catch(Exception e) {
            throw new IllegalStateException(createErrorMsg(episodeHistoryList), e);
        }
    }

    private EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> makePolicyStep(
        TState state,
        Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> onTurnPolicy,
        Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> otherPolicy,
        boolean isPlayerMove) {
        TAction action = onTurnPolicy.getDiscreteAction(state);
        var playerPaperPolicyStepRecord = onTurnPolicy.getPolicyRecord(state);
        onTurnPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        otherPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn = state.applyAction(action);
        return new EpisodeStepRecordGeneric<>(isPlayerMove, action, playerPaperPolicyStepRecord, state, stateRewardReturn.getState(), stateRewardReturn.getReward());
    }

    private void makeStepLog(EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> step) {
        if(logger.isDebugEnabled()) {
            logger.debug("Player's [{}]th action. Step log: [{}] ", playerStepsDone, step.toLogString());
        }
    }

    private String createErrorMsg(List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeHistoryList) {
        return "Episode simulation ended due to inner exception. Episode steps: [" +
            episodeHistoryList
                .stream()
                .map(x -> x.getPlayedAction().toString())
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
                            .append(x.getToState().isPlayerTurn())
                            .append(System.lineSeparator())
                            .append(x.getToState().readableStringRepresentation())
                            .append(System.lineSeparator())
                            .append(x.toLogString())
                            .append(System.lineSeparator())
                            .append("Applied action: ")
                            .append(System.lineSeparator())
                            .append(x.getPlayedAction())
                            .append(System.lineSeparator())
                            .append("Getting reward: ")
                            .append(System.lineSeparator())
                            .append(x.getReward())
                            .append(System.lineSeparator());
                        sb.append("----------------------------------------------------------------------------------------------------");
                        return sb.toString();
                    }
                ).reduce((a, b) -> a + System.lineSeparator() + b);
    }

}
