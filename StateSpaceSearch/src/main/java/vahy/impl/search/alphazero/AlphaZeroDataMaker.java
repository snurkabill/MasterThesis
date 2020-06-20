package vahy.impl.search.alphazero;

import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.PolicyRecord;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlphaZeroDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>, TPolicyRecord extends PolicyRecord>
    implements EpisodeDataMaker<TAction, DoubleVector, TState, TPolicyRecord> {

    private final double discountFactor;
    private final int playerPolicyId;
    private final int actionCount;

    public AlphaZeroDataMaker(int playerPolicyId, int actionCount, double discountFactor) {
        this.discountFactor = discountFactor;
        this.playerPolicyId = playerPolicyId;
        this.actionCount = actionCount;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState, TPolicyRecord> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var entityInGameCount = episodeHistory.get(0).getFromState().getTotalEntityCount();
        var translationMap = episodeResults.getPolicyIdTranslationMap();
        var inGameEntityId = translationMap.getInGameEntityId(playerPolicyId);
        var aggregatedTotalPayoff = new double[entityInGameCount];
        var iterator = episodeHistory.listIterator(episodeResults.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCountList().get(inGameEntityId));
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            if (previous.getFromState().isEnvironmentEntityOnTurn()) {
                var action = previous.getAction();
                var actionId = action.ordinal();
                var doubleArray = new double[entityInGameCount + actionCount];
                System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                doubleArray[entityInGameCount + actionId] = 1.0;
                mutableDataSampleList.add(new ImmutableTuple<>(previous.getFromState().getInGameEntityObservation(inGameEntityId), new MutableDoubleArray(doubleArray, false)));
            } else if (previous.getFromState().isInGame(inGameEntityId)) {
                aggregatedTotalPayoff = DoubleVectorRewardAggregator.aggregateDiscount(previous.getReward(), aggregatedTotalPayoff, discountFactor);

                if(previous.getPolicyIdOnTurn() == playerPolicyId) {
                    var policyArray = previous.getPolicyStepRecord().getPolicyProbabilities();
                    var doubleArray = new double[entityInGameCount + policyArray.length];

                    System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                    System.arraycopy(policyArray, 0, doubleArray, entityInGameCount, policyArray.length);

                    mutableDataSampleList.add(new ImmutableTuple<>(previous.getFromState().getInGameEntityObservation(inGameEntityId), new MutableDoubleArray(doubleArray, false)));
                }
            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }
}
