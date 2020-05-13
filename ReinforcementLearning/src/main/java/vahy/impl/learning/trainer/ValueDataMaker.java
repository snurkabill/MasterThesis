package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.policy.PolicyRecord;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValueDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>, TPolicyRecord extends PolicyRecord>
    implements EpisodeDataMaker<TAction, DoubleVector, TState, TPolicyRecord> {

    private final double discountFactor;
    private final int allActionCount;
    private final int playerPolicyId;

    public ValueDataMaker(double discountFactor, int allActionCount, int playerPolicyId) {
        this.discountFactor = discountFactor;
        this.allActionCount = allActionCount;
        this.playerPolicyId = playerPolicyId;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState, TPolicyRecord> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var aggregatedTotalPayoff = 0.0;
        var iterator = episodeHistory.listIterator(episodeResults.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCountList().get(playerPolicyId));
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getReward()[playerPolicyId], aggregatedTotalPayoff, discountFactor);
//            if(previous.getPolicyIdOnTurn() == playerPolicyId) {
                var doubleArray = new double[1];
                doubleArray[0] = aggregatedTotalPayoff;
                mutableDataSampleList.add(new ImmutableTuple<>(previous.getFromState().getPlayerObservation(playerPolicyId), new MutableDoubleArray(doubleArray, false)));
//            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }
}
