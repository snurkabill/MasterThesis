package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;
import vahy.utils.StreamUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValueDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> implements EpisodeDataMaker<TAction, DoubleVector, TState> {

    private final double discountFactor;
    private final int playerPolicyId;
    private final int policyObservationLookbackSize;

    public ValueDataMaker(double discountFactor, int playerPolicyId) {
        this(discountFactor, playerPolicyId, 1);
    }

    public ValueDataMaker(double discountFactor, int playerPolicyId, int policyObservationLookbackSize) {
        this.discountFactor = discountFactor;
        this.playerPolicyId = playerPolicyId;
        this.policyObservationLookbackSize = policyObservationLookbackSize;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var translationMap = episodeResults.getPolicyIdTranslationMap();
        var inGameEntityId = translationMap.getInGameEntityId(playerPolicyId);
        var aggregatedTotalPayoff = 0.0;

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
        var iterator = zippedStates.listIterator(episodeResults.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCountList().get(inGameEntityId));
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getFirst().getReward()[inGameEntityId], aggregatedTotalPayoff, discountFactor);
//            if(previous.getPolicyIdOnTurn() == playerPolicyId) {
            var doubleArray = new double[1];
            doubleArray[0] = aggregatedTotalPayoff;
            mutableDataSampleList.add(new ImmutableTuple<>(previous.getSecond().getObservation(), new MutableDoubleArray(doubleArray, false)));
//            }

        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }
}
