package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicy;

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
    private int totalStepsDone = 0;
    private int playerStepsDone = 0;
    private double totalCumulativePayoff = 0.0;

    public EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState> calculateEpisode(
        EpisodeImmutableSetup<TAction, TPlayerObservation, TOpponentObservation, TState> episodeImmutableSetup)
    {
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> playerPolicy = episodeImmutableSetup.getPlayerPaperPolicy();
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> opponentPolicy = episodeImmutableSetup.getOpponentPolicy();

        TState state = episodeImmutableSetup.getInitialState();
        logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        return episodeRun(episodeImmutableSetup, playerPolicy, opponentPolicy, state);
    }

    private EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState> episodeRun(
        EpisodeImmutableSetup<TAction, TPlayerObservation, TOpponentObservation, TState> episodeImmutableSetup,
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> playerPolicy,
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> opponentPolicy,
        TState state) {
        var episodeHistoryList = new ArrayList<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState>>();
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
            return new EpisodeResults<>(episodeHistoryList, playerStepsDone, totalStepsDone, totalCumulativePayoff, state.isRiskHit(), end - start);
        } catch(Exception e) {
            throw new IllegalStateException(createErrorMsg(episodeHistoryList), e);
        }
    }

    private EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState> makePolicyStep(
        TState state,
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> onTurnPolicy,
        PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> otherPolicy,
        boolean isPlayerMove) {
        TAction action = onTurnPolicy.getDiscreteAction(state);
        var playerPaperPolicyStepRecord = onTurnPolicy.getPolicyRecord(state);
        onTurnPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        otherPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> stateRewardReturn = state.applyAction(action);
        return new EpisodeStepRecord<>(isPlayerMove, action, playerPaperPolicyStepRecord, state, stateRewardReturn.getState(), stateRewardReturn.getReward());
    }

    private void makeStepLog(EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState> step) {
        logger.debug("Player's [{}]th action: [{}] getting reward [{}]. ExpectedReward: [{}], Expected risk: [{}], PolicyProbabilities: [{}], PriorProbabilities: [{}]",
            playerStepsDone,
            step.getPlayedAction(),
            step.getReward(),
            step.getPaperPolicyStepRecord().getPredictedReward(),
            step.getPaperPolicyStepRecord().getPredictedRisk(),
            Arrays.toString(step.getPaperPolicyStepRecord().getPolicyProbabilities()),
            Arrays.toString(step.getPaperPolicyStepRecord().getPriorProbabilities()));
    }

    private String createErrorMsg(List<EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState>> episodeHistoryList) {
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
                            .append("Policy probabilities: ")
                            .append(System.lineSeparator())
                            .append(Arrays.toString(x.getPaperPolicyStepRecord().getPolicyProbabilities()))
                            .append(System.lineSeparator())
                            .append("Prior probabilities: ")
                            .append(System.lineSeparator())
                            .append(Arrays.toString(x.getPaperPolicyStepRecord().getPriorProbabilities()))
                            .append(System.lineSeparator())
                            .append("Policy reward : ")
                            .append(System.lineSeparator())
                            .append(x.getPaperPolicyStepRecord().getPredictedReward())
                            .append(System.lineSeparator())
                            .append("Policy risk : ")
                            .append(System.lineSeparator())
                            .append(x.getPaperPolicyStepRecord().getPredictedRisk())
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
