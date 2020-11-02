package vahy.impl.learning.trainer;

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

public class VectorValueDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> extends AbstractDataMaker<TAction, TState> {

    public VectorValueDataMaker(double discountFactor, int playerPolicyId, DataAggregator dataAggregator) {
        this(discountFactor, playerPolicyId, 1, dataAggregator);
    }

    public VectorValueDataMaker(double discountFactor, int playerPolicyId, int policyObservationLookbackSize, DataAggregator dataAggregator) {
        super(discountFactor, playerPolicyId, policyObservationLookbackSize, dataAggregator);
    }

    @Override
    protected List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples_inner(ListIterator<EpisodeStepRecordWithObservation<TAction, TState>> iterator, int inGameEntityId, int estimatedElementCount) {
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(estimatedElementCount);
        var aggregatedTotalPayoff = new double[entityCount];
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleVectorRewardAggregator.aggregateDiscount(previous.getEpisodeStepRecord().getReward(), aggregatedTotalPayoff, discountFactor);
            mutableDataSampleList.add(new ImmutableTuple<>(previous.getObservation(), new MutableDoubleArray(aggregatedTotalPayoff, false)));
        }
        return mutableDataSampleList;
    }
}
