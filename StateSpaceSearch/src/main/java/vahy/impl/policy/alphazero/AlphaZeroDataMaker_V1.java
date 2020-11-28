package vahy.impl.policy.alphazero;

import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.AbstractDataMaker;
import vahy.api.learning.trainer.EpisodeStepRecordWithObservation;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class AlphaZeroDataMaker_V1<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> extends AbstractDataMaker<TAction, TState> {

    private final int actionCount;

    public AlphaZeroDataMaker_V1(int playerPolicyId, int actionCount, double discountFactor, DataAggregator dataAggregator) {
        this(playerPolicyId, actionCount, discountFactor, 1, dataAggregator);
    }

    public AlphaZeroDataMaker_V1( int playerPolicyId, int actionCount, double discountFactor, int policyObservationLookbackSize, DataAggregator dataAggregator) {
        super(discountFactor, playerPolicyId, policyObservationLookbackSize, dataAggregator);
        this.actionCount = actionCount;
    }

    @Override
    protected List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples_inner(ListIterator<EpisodeStepRecordWithObservation<TAction, TState>> iterator, int inGameEntityId, int estimatedElementCount) {
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(estimatedElementCount);
        var aggregatedTotalPayoff = new double[entityCount];
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            var episodeStepRecord = previous.getEpisodeStepRecord();
            var observation = previous.getObservation();
            aggregatedTotalPayoff = DoubleVectorRewardAggregator.aggregateDiscount(episodeStepRecord.getReward(), aggregatedTotalPayoff, discountFactor);
            var doubleArray = new double[entityCount + actionCount];
            if (episodeStepRecord.getFromState().isEnvironmentEntityOnTurn()) {
                var action = episodeStepRecord.getAction();
                var actionId = action.ordinal();
                doubleArray[entityCount + actionId] = 1.0;
                System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                mutableDataSampleList.add(new ImmutableTuple<>(observation, new MutableDoubleArray(doubleArray, false)));
            } else {
                if (episodeStepRecord.getPolicyIdOnTurn() == playerPolicyId) {
                    var policyArray = episodeStepRecord.getPolicyStepRecord().getPolicyProbabilities();
                    System.arraycopy(policyArray, 0, doubleArray, entityCount, policyArray.length);
                    System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                    mutableDataSampleList.add(new ImmutableTuple<>(observation, new MutableDoubleArray(doubleArray, false)));
                }
            }
        }
        return mutableDataSampleList;
    }
}
