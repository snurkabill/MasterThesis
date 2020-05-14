package vahy.impl.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeSetup;
import vahy.api.episode.EpisodeSimulator;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EpisodeSimulatorImpl<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeSimulator<TAction, TObservation, TState, TPolicyRecord> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeSimulator.class.getName());
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled();
    private int totalStepsDone = 0;

    private final EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> resultsFactory;

    public EpisodeSimulatorImpl(EpisodeResultsFactory<TAction, TObservation, TState, TPolicyRecord> resultsFactory) {
        this.resultsFactory = resultsFactory;
    }

    public EpisodeResults<TAction, TObservation, TState, TPolicyRecord> calculateEpisode(EpisodeSetup<TAction, TObservation, TState, TPolicyRecord> episodeSetup)
    {
        TState state = episodeSetup.getInitialState();
        if(TRACE_ENABLED) {
            logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        }
        return episodeRun(episodeSetup.getStepCountLimit(), episodeSetup.getPolicyList() , state);
    }

    private EpisodeResults<TAction, TObservation, TState, TPolicyRecord> episodeRun(int episodeStepCountLimit, List<Policy<TAction, TObservation, TState, TPolicyRecord>> policyList, TState initState) {
        var episodeHistoryList = new ArrayList<EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord>>(episodeStepCountLimit);

        policyList.sort(Comparator.comparing(Policy::getPolicyId));
        List<Integer> playerStepsDone = new ArrayList<>(policyList.size());
        List<Double> totalCumulativePayoffList = new ArrayList<>(policyList.size());
        for (int i = 0; i < policyList.size(); i++) {
            playerStepsDone.add(0);
            totalCumulativePayoffList.add(0.0);
        }

        TState state = initState;
        try {
            long start = System.currentTimeMillis();
            while(!state.isFinalState() && totalStepsDone < episodeStepCountLimit) {

                var policyIdOnTurn = state.getInGameEntityIdOnTurn();
                var onTurnPolicy = policyList.get(policyIdOnTurn);

                var step = makePolicyStep(state, onTurnPolicy, policyList);
                totalStepsDone++;
                playerStepsDone.set(onTurnPolicy.getPolicyId(), playerStepsDone.get(onTurnPolicy.getPolicyId()) + 1);
                distributeRewards(totalCumulativePayoffList, step);

                if(DEBUG_ENABLED) {
                    makeStepLog(step);
                }
                episodeHistoryList.add(step);
                state = step.getToState();
                if(TRACE_ENABLED) {
                    logger.trace("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerStepsDone);
                }
            }
            long end = System.currentTimeMillis();
            return resultsFactory.createResults(episodeHistoryList, policyList.size(), playerStepsDone, totalStepsDone, totalCumulativePayoffList, Duration.ofMillis(end - start));
        } catch(Exception e) {
            throw new IllegalStateException(createErrorMsg(episodeHistoryList), e);
        }
    }

    private void distributeRewards(List<Double> totalCumulativePayoffList, EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord> step) {
        var rewards = step.getReward();
        for (int i = 0; i < rewards.length; i++) {
            totalCumulativePayoffList.set(i, totalCumulativePayoffList.get(i) + rewards[i]);
        }
    }

    private EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord> makePolicyStep(
        TState state,
        Policy<TAction, TObservation, TState, TPolicyRecord> onTurnPolicy,
        List<Policy<TAction, TObservation, TState, TPolicyRecord>> allPolicyList)
    {
        var stateWrapper = new StateWrapper<>(onTurnPolicy.getPolicyId(), state);
        TAction action = onTurnPolicy.getDiscreteAction(stateWrapper);
        var playerPaperPolicyStepRecord = onTurnPolicy.getPolicyRecord(stateWrapper);
        onTurnPolicy.updateStateOnPlayedActions(Collections.singletonList(action));
        var actionList = Collections.singletonList(action);
        for (Policy<TAction, TObservation, TState, TPolicyRecord> entry : allPolicyList) {
            if(entry.getPolicyId() != onTurnPolicy.getPolicyId()) {
                entry.updateStateOnPlayedActions(actionList);
            }
        }
        StateRewardReturn<TAction, TObservation, TState> stateRewardReturn = state.applyAction(action);
        return new EpisodeStepRecordImpl<>(onTurnPolicy.getPolicyId(), action, playerPaperPolicyStepRecord, state, stateRewardReturn.getState(), stateRewardReturn.getReward());
    }

    private void makeStepLog(EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord> step) {
        if(DEBUG_ENABLED) {
            logger.debug("[{}]th action. Step log: [{}] ", totalStepsDone, step.toLogString());
        }
    }

    private String createErrorMsg(List<EpisodeStepRecord<TAction, TObservation, TState, TPolicyRecord>> episodeHistoryList) {
        return "Episode simulation ended due to inner exception. Episode steps: [" +
            episodeHistoryList
                .stream()
                .map(x -> x.getAction().toString())
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
                            .append("PolicyId On Turn: ")
                            .append(x.getToState().getInGameEntityIdOnTurn())
                            .append(System.lineSeparator())
                            .append(x.getToState().readableStringRepresentation())
                            .append(System.lineSeparator())
                            .append(x.toLogString())
                            .append(System.lineSeparator())
                            .append("Applied action: ")
                            .append(System.lineSeparator())
                            .append(x.getAction())
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
