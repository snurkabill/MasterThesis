package vahy.api.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public abstract class AbstractDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> implements EpisodeDataMaker<TAction, DoubleVector, TState> {

    protected final double discountFactor;
    protected final int playerPolicyId;
    protected final int policyObservationLookbackSize;
    private final DataAggregator dataAggregator;
    protected int entityCount;

    protected AbstractDataMaker(double discountFactor, int playerPolicyId, int policyObservationLookbackSize,  DataAggregator dataAggregator) {
        this.discountFactor = discountFactor;
        this.playerPolicyId = playerPolicyId;
        this.policyObservationLookbackSize = policyObservationLookbackSize;
        this.dataAggregator = dataAggregator;
    }

    protected int getInGameEntityId(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var translationMap = episodeResults.getPolicyIdTranslationMap();
        return translationMap.getInGameEntityId(playerPolicyId);
    }

    protected int getEntityCount(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        return episodeResults.getEpisodeHistory().get(0).getFromState().getTotalEntityCount();
    }

    private List<ImmutableTuple<DoubleVector, MutableDoubleArray>> resolveReverse(List<ImmutableTuple<DoubleVector, MutableDoubleArray>> mutableDataSampleList) {
        if(dataAggregator.requiresStatesInOrder()) {
            Collections.reverse(mutableDataSampleList);
        }
        return mutableDataSampleList;
    }

    protected abstract List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples_inner(ListIterator<ImmutableTuple<EpisodeStepRecord<TAction, DoubleVector, TState>, StateWrapper<TAction, DoubleVector, TState>>> iterator, int inGameEntityId, int estimatedElementCount);

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var inGameEntityId = getInGameEntityId(episodeResults);
        var episodeHistory = episodeResults.getEpisodeHistory();
        entityCount = getEntityCount(episodeResults);

        var filteredStates = episodeHistory.stream().filter(x -> x.getFromState().isInGame(inGameEntityId)).collect(Collectors.toList());
        var wrappedStates = new ArrayList<StateWrapper<TAction, DoubleVector, TState>>(filteredStates.size());
        StateWrapper<TAction, DoubleVector, TState> previousWrapper = null;
        for (var filteredState : filteredStates) {
            var wrappedState = previousWrapper == null ?
                new StateWrapper<>(inGameEntityId, policyObservationLookbackSize, filteredState.getFromState()) :
                new StateWrapper<>(inGameEntityId, filteredState.getFromState(), previousWrapper);
            wrappedStates.add(wrappedState);
            previousWrapper = wrappedState;
        }
        var zippedStates = StreamUtils.zip(filteredStates.stream(), wrappedStates.stream(), ImmutableTuple::new).collect(Collectors.toList());
        var iterator = zippedStates.listIterator(zippedStates.size());
//        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(zippedStates.size());
//
//        var aggregatedTotalPayoff = 0.0;
//        while(iterator.hasPrevious()) {
//            var previous = iterator.previous();
//            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getFirst().getReward()[inGameEntityId], aggregatedTotalPayoff, discountFactor);
////            if(previous.getPolicyIdOnTurn() == playerPolicyId) {
//            var doubleArray = new double[1];
//            doubleArray[0] = aggregatedTotalPayoff;
//            mutableDataSampleList.add(new ImmutableTuple<>(previous.getSecond().getObservation(), new MutableDoubleArray(doubleArray, false)));
////            }
//        }

        return resolveReverse(createEpisodeDataSamples_inner(iterator, inGameEntityId, zippedStates.size()));
    }

}
