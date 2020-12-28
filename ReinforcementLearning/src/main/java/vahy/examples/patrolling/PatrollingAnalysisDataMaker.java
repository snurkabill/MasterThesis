package vahy.examples.patrolling;

import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.AbstractDataMaker;
import vahy.api.learning.trainer.EpisodeStepRecordWithObservation;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PatrollingAnalysisDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>> extends AbstractDataMaker<TAction, TState> {

    public PatrollingAnalysisDataMaker(double discountFactor, int playerPolicyId, DataAggregator dataAggregator) {
        this(discountFactor, playerPolicyId, 1, dataAggregator);
    }

    public PatrollingAnalysisDataMaker(double discountFactor, int playerPolicyId, int policyObservationLookbackSize, DataAggregator dataAggregator) {
        super(discountFactor, playerPolicyId, policyObservationLookbackSize, dataAggregator);
    }

    @Override
    protected List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples_inner(ListIterator<EpisodeStepRecordWithObservation<TAction, TState>> iterator, int inGameEntityId, int estimatedElementCount) {
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(estimatedElementCount);
        var aggregatedTotalPayoff = 0.0;
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            if(previous.getEpisodeStepRecord().getInGameEntityIdOnTurn() != inGameEntityId) {
                aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getEpisodeStepRecord().getReward()[inGameEntityId], aggregatedTotalPayoff, discountFactor);
                var doubleArray = new double[1];
                doubleArray[0] = aggregatedTotalPayoff;
                mutableDataSampleList.add(new ImmutableTuple<>(previous.getObservation(), new MutableDoubleArray(doubleArray, false)));
            }
        }
        return mutableDataSampleList;
    }
}
