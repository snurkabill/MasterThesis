package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeStepRecord;
import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.AbstractDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ValueDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> extends AbstractDataMaker<TAction, TState> {

    public ValueDataMaker(double discountFactor, int playerPolicyId, DataAggregator dataAggregator) {
        this(discountFactor, playerPolicyId, 1, dataAggregator);
    }

    public ValueDataMaker(double discountFactor, int playerPolicyId, int policyObservationLookbackSize, DataAggregator dataAggregator) {
        super(discountFactor, playerPolicyId, policyObservationLookbackSize, dataAggregator);
    }

    @Override
    protected List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples_inner(ListIterator<ImmutableTuple<EpisodeStepRecord<TAction, DoubleVector, TState>, StateWrapper<TAction, DoubleVector, TState>>> iterator, int inGameEntityId, int estimatedElementCount) {
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(estimatedElementCount);
        var aggregatedTotalPayoff = 0.0;
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getFirst().getReward()[inGameEntityId], aggregatedTotalPayoff, discountFactor);
            var doubleArray = new double[1];
            doubleArray[0] = aggregatedTotalPayoff;
            mutableDataSampleList.add(new ImmutableTuple<>(previous.getSecond().getObservation(), new MutableDoubleArray(doubleArray, false)));
        }
        return mutableDataSampleList;
    }
}
