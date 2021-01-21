package vahy.impl.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.EpisodeSetup;
import vahy.api.episode.EpisodeSimulator;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.episode.RegisteredPolicy;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.utils.ImmutableTuple;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EpisodeSimulatorImpl<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>>
    implements EpisodeSimulator<TAction, TObservation, TState> {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeSimulator.class.getName());
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled() || TRACE_ENABLED;
    private int totalStepsDone = 0;

    private final EpisodeResultsFactory<TAction, TObservation, TState> resultsFactory;

    public EpisodeSimulatorImpl(EpisodeResultsFactory<TAction, TObservation, TState> resultsFactory) {
        this.resultsFactory = resultsFactory;
    }

    @Override
    public EpisodeResults<TAction, TObservation, TState> calculateEpisode(EpisodeSetup<TAction, TObservation, TState> episodeSetup) {
        TState state = episodeSetup.getInitialState();
        if (TRACE_ENABLED) {
            logger.trace("State at the begin of episode: " + System.lineSeparator() + state.readableStringRepresentation());
        }
        return episodeRun(episodeSetup.getStepCountLimit(), episodeSetup.getPolicyIdTranslationMap(), episodeSetup.getRegisteredPolicyList(), state);
    }

    private EpisodeResults<TAction, TObservation, TState> episodeRun(int episodeStepCountLimit,
                                                                     PolicyIdTranslationMap policyIdTranslationMap,
                                                                     List<RegisteredPolicy<TAction, TObservation, TState>> policyList,
                                                                     TState initState) {
        var episodeHistoryList = new ArrayList<EpisodeStepRecord<TAction, TObservation, TState>>(episodeStepCountLimit);

        List<Integer> playerStepsDone = new ArrayList<>(policyList.size());
        List<Long> playerDecisionTimeInMillis = new ArrayList<>(policyList.size());
        List<Double> totalCumulativePayoffList = new ArrayList<>(policyList.size());
        for (int i = 0; i < policyList.size(); i++) {
            playerStepsDone.add(0);
            playerDecisionTimeInMillis.add(0L);
            totalCumulativePayoffList.add(0.0);
        }


        TState state = initState;
        try {
            long episodeStart = System.currentTimeMillis();
            while (!state.isFinalState() && totalStepsDone < episodeStepCountLimit) {

                var inGameEntityIdOnTurn = state.getInGameEntityIdOnTurn();
                var policyIdOnTurn = policyIdTranslationMap.getPolicyId(inGameEntityIdOnTurn);

                var stepWithTime = makePolicyStep(state, policyIdOnTurn, inGameEntityIdOnTurn, policyList, policyIdTranslationMap);
                var step = stepWithTime.getFirst();
                var stepPolicyIdOnTurn = step.getPolicyIdOnTurn();
                var stepInGameEntityOnTurn = step.getInGameEntityIdOnTurn();

                if (stepPolicyIdOnTurn != policyIdOnTurn) {
                    throw new IllegalArgumentException("differnet policy Ids");
                }

                if (stepInGameEntityOnTurn != inGameEntityIdOnTurn) {
                    throw new IllegalArgumentException("Different in game entity ids");
                }

                totalStepsDone++;
                playerStepsDone.set(policyIdOnTurn, playerStepsDone.get(policyIdOnTurn) + 1);
                playerDecisionTimeInMillis.set(policyIdOnTurn, playerDecisionTimeInMillis.get(policyIdOnTurn) + stepWithTime.getSecond());

                collectPolicyStats(totalCumulativePayoffList, step, policyIdTranslationMap);

                if (DEBUG_ENABLED) {
                    makeStepLog(step);
                }
                episodeHistoryList.add(step);
                state = step.getToState();
                if (TRACE_ENABLED) {
                    logger.trace("State at [{}]th timestamp: " + System.lineSeparator() + state.readableStringRepresentation(), playerStepsDone);
                }
            }
            long end = System.currentTimeMillis();

            var averageDecisionDuration = new ArrayList<Double>(policyList.size());
            for (int i = 0; i < playerDecisionTimeInMillis.size(); i++) {
                averageDecisionDuration.add(playerDecisionTimeInMillis.get(i) / (double) playerStepsDone.get(i));
            }

            return resultsFactory.createResults(episodeHistoryList, policyIdTranslationMap, policyList.size(), playerStepsDone, averageDecisionDuration, totalStepsDone, totalCumulativePayoffList, Duration.ofMillis(end - episodeStart));
        } catch (RuntimeException e) {
            throw new RuntimeException(createErrorMsg(episodeHistoryList), e);
        }
    }

    private void collectPolicyStats(List<Double> totalCumulativePayoffList, EpisodeStepRecord<TAction, TObservation, TState> step, PolicyIdTranslationMap translationMap) {
        var rewards = step.getReward();
        for (int i = 0; i < rewards.length; i++) {
            var policyId = translationMap.getPolicyId(i);
            totalCumulativePayoffList.set(policyId, totalCumulativePayoffList.get(policyId) + rewards[i]);
        }
    }

    private ImmutableTuple<EpisodeStepRecord<TAction, TObservation, TState>, Long> makePolicyStep(TState state,
                                                                                                  int policyIdOnTurn,
                                                                                                  int inGameEntityId,
                                                                                                  List<RegisteredPolicy<TAction, TObservation, TState>> allPolicyList,
                                                                                                  PolicyIdTranslationMap policyIdTranslationMap) {
        var onTurnRegisteredPolicy = allPolicyList.get(policyIdOnTurn);
        if (inGameEntityId != onTurnRegisteredPolicy.getInGameEntityId()) {
            throw new IllegalArgumentException("Different policyIds");
        }
        var stateWrapper = new StateWrapper<>(inGameEntityId, onTurnRegisteredPolicy.getObservationLookbackSize() , state);
        var onTurnPolicy = onTurnRegisteredPolicy.getPolicy();
        if (policyIdOnTurn != onTurnPolicy.getPolicyId()) {
            throw new IllegalStateException("Discrepancy. PolicyId from translation map [" + policyIdOnTurn + "] does not match onTurnPolicyId [" + onTurnPolicy.getPolicyId() + "]");
        }
        var start = System.currentTimeMillis();
        var action = onTurnPolicy.getDiscreteAction(stateWrapper);
        var decisionInMs = System.currentTimeMillis() - start;
        var stateRewardReturn = state.applyAction(action);
        var policyStepRecord = onTurnPolicy.getPolicyRecord(stateWrapper);
        onTurnPolicy.updateStateOnPlayedAction(action);
        for (var entry : allPolicyList) {
            if (entry.getPolicyId() != policyIdOnTurn) {
                var toUpdatePolicy = entry.getPolicy();
                var toUpdateInGameEntityId = policyIdTranslationMap.getInGameEntityId(toUpdatePolicy.getPolicyId());
                if (state.isInGame(toUpdateInGameEntityId)) {
                    var observedAction = stateRewardReturn.getAction()[toUpdateInGameEntityId];
                    toUpdatePolicy.updateStateOnPlayedAction(observedAction);
                }
            }
        }
        return new ImmutableTuple<>(new EpisodeStepRecordImpl<>(policyIdOnTurn, inGameEntityId, action, policyStepRecord, state, stateRewardReturn.getState(), stateRewardReturn.getReward()), decisionInMs);
    }

    private void makeStepLog(EpisodeStepRecord<TAction, TObservation, TState> step) {
        if (DEBUG_ENABLED) {
            logger.debug("[{}]th action. Step log: [{}] ", totalStepsDone, step.toLogString());
        }
    }

    private String createErrorMsg(List<EpisodeStepRecord<TAction, TObservation, TState>> episodeHistoryList) {
        if (episodeHistoryList.isEmpty()) {
            return "No steps were done in episode.";
        }
        return "Episode simulation ended due to inner exception. Episode steps: [" +
            episodeHistoryList
                .stream()
                .map(x -> x.getAction().toString())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Episode does not contain any states as history")
            +
            "]. " +
            " Total episode history: " +
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
                            .append(Arrays.toString(x.getReward()))
                            .append(System.lineSeparator());
                        sb.append("----------------------------------------------------------------------------------------------------");
                        return sb.toString();
                    }
                ).reduce((a, b) -> a + System.lineSeparator() + b);
    }

}
