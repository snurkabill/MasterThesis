package vahy.multirewardAggregation;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.RiskState;
import vahy.api.episode.EpisodeResults;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.learning.trainer.EpisodeStepRecordWithObservation;
import vahy.api.model.Action;
import vahy.api.model.StateWrapper;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class MultirewardDataMaker<TAction extends Enum<TAction> & Action, TState extends RiskState<TAction, DoubleVector, TState>> implements EpisodeDataMaker<TAction, DoubleVector, TState> {

    private static final Logger logger = LoggerFactory.getLogger(MultirewardDataMaker.class);

    protected final double discountFactor;
    protected final int playerPolicyId;
    protected final int policyObservationLookbackSize;
    private final DataAggregator dataAggregator;
//    protected int entityCount;

    private final double requiredRiskThreshold;
    private double latestRiskPenalty = 1.0;

    private final CircularFifoQueue<Boolean> queue;
    private final double riskDecayFactor;
    private int counter = 0;

    public MultirewardDataMaker(double discountFactor, int playerPolicyId, DataAggregator dataAggregator, double requiredRiskThreshold, int queueSize, double riskDecayFactor) {
        this(discountFactor, playerPolicyId, 1, dataAggregator, requiredRiskThreshold, queueSize, riskDecayFactor);
    }

    public MultirewardDataMaker(double discountFactor, int playerPolicyId, int policyObservationLookbackSize, DataAggregator dataAggregator, double requiredRiskThreshold, int queueSize, double riskDecayFactor) {
        this.discountFactor = discountFactor;
        this.playerPolicyId = playerPolicyId;
        this.policyObservationLookbackSize = policyObservationLookbackSize;
        this.dataAggregator = dataAggregator;
        this.requiredRiskThreshold = requiredRiskThreshold;
        this.queue = new CircularFifoQueue<>(queueSize);
        this.riskDecayFactor = riskDecayFactor;
    }

    protected int getInGameEntityId(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var translationMap = episodeResults.getPolicyIdTranslationMap();
        return translationMap.getInGameEntityId(playerPolicyId);
    }

    private List<ImmutableTuple<DoubleVector, MutableDoubleArray>> resolveReverse(List<ImmutableTuple<DoubleVector, MutableDoubleArray>> mutableDataSampleList) {
        if(dataAggregator.requiresStatesInOrder()) {
            Collections.reverse(mutableDataSampleList);
        }
        return mutableDataSampleList;
    }

    protected int getEntityCount(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        return episodeResults.getEpisodeHistory().get(0).getFromState().getTotalEntityCount();
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var inGameEntityId = getInGameEntityId(episodeResults);
        var episodeHistory = episodeResults.getEpisodeHistory();
//        entityCount = getEntityCount(episodeResults);
        var filteredStates = episodeHistory.stream().filter(x -> x.getFromState().isInGame(inGameEntityId)).collect(Collectors.toList());
        var observations = new ArrayList<DoubleVector>(filteredStates.size());
        StateWrapper<TAction, DoubleVector, TState> previousWrapper = null;
        for (var filteredState : filteredStates) {
            var wrappedState = previousWrapper == null ?
                new StateWrapper<>(inGameEntityId, policyObservationLookbackSize, filteredState.getFromState()) :
                new StateWrapper<>(inGameEntityId, filteredState.getFromState(), previousWrapper);
            observations.add(wrappedState.getObservation());
            previousWrapper = wrappedState;
        }
        var zippedStates = StreamUtils.zip(filteredStates.stream(), observations.stream(), EpisodeStepRecordWithObservation::new).collect(Collectors.toList());
        var iterator = zippedStates.listIterator(zippedStates.size());

        queue.add(filteredStates.get(filteredStates.size() - 1).getToState().isRiskHit(inGameEntityId));

        return resolveReverse(createEpisodeDataSamples_inner(iterator, inGameEntityId, zippedStates.size()));
    }

    private List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples_inner(ListIterator<EpisodeStepRecordWithObservation<TAction, TState>> iterator, int inGameEntityId, int estimatedElementCount) {
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(estimatedElementCount);
        var aggregatedTotalPayoff = 0.0;
        var isRiskArrayInitialized = false;

        var riskRatio = getRunningRiskRatio();
        var riskRatioDiff = requiredRiskThreshold - riskRatio;

//        latestRiskPenalty = riskRatioDiff >= 0.0 ? latestRiskPenalty * riskDecayFactor : latestRiskPenalty * riskDecayFactor + (1.0 - riskDecayFactor) * (latestRiskPenalty * (1 - riskRatioDiff));
        latestRiskPenalty = riskRatioDiff >= 0.0 ? latestRiskPenalty * riskDecayFactor : latestRiskPenalty * (1 + (1.0 - riskDecayFactor));
        if(counter % 1000 == 0) {

            logger.warn("riskRatioDiff: [{}], risk penalty: [{}]", riskRatioDiff, latestRiskPenalty);
        }
        counter++;

        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            if(!isRiskArrayInitialized) {
                isRiskArrayInitialized = true;
                aggregatedTotalPayoff =  -latestRiskPenalty * (previous.getEpisodeStepRecord().getToState().getRiskVector()[inGameEntityId] ? 1.0 : 0.0);
            }
            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getEpisodeStepRecord().getReward()[inGameEntityId], aggregatedTotalPayoff, discountFactor);
            var doubleArray = new double[] {aggregatedTotalPayoff};
            mutableDataSampleList.add(new ImmutableTuple<>(previous.getObservation(), new MutableDoubleArray(doubleArray, false)));
        }
        return mutableDataSampleList;
    }

    private double getRunningRiskRatio() {
        // TODO: make this with constant complexity by remembering running average
        int riskCounter = 0;
        for (Boolean entry : queue) {
            if(entry) {
                riskCounter++;
            }
        }
        return riskCounter / (double) queue.size();
    }


}
