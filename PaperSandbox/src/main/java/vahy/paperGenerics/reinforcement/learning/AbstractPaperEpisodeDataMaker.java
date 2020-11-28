package vahy.paperGenerics.reinforcement.learning;

import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.AbstractDataMaker;
import vahy.api.learning.trainer.EpisodeStepRecordWithObservation;
import vahy.api.model.Action;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public abstract class AbstractPaperEpisodeDataMaker<TAction extends Enum<TAction> & Action, TState extends PaperState<TAction, DoubleVector, TState>> extends AbstractDataMaker<TAction, TState> {

    protected final int actionCount;

    public AbstractPaperEpisodeDataMaker(int playerPolicyId, int actionCount, double discountFactor, DataAggregator dataAggregator) {
        this(playerPolicyId, actionCount, discountFactor, 1, dataAggregator);
    }

    public AbstractPaperEpisodeDataMaker(int playerPolicyId, int actionCount, double discountFactor, int policyObservationLookbackSize, DataAggregator dataAggregator) {
        super(discountFactor, playerPolicyId, policyObservationLookbackSize, dataAggregator);
        this.actionCount = actionCount;
    }

    protected void fillRiskArray(TState state, double[] aggregatedRisk) {
        var booleanRiskArray = state.getRiskVector();
        for (int i = 0; i < aggregatedRisk.length; i++) {
            aggregatedRisk[i] = booleanRiskArray[i] ? 1.0 : 0.0;
        }
    }

    protected abstract ImmutableTuple<DoubleVector, MutableDoubleArray> createDatasample(double[] aggregatedRisk, double[] aggregatedTotalPayoff, EpisodeStepRecordWithObservation<TAction, TState> previous);

    @Override
    protected List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples_inner(ListIterator<EpisodeStepRecordWithObservation<TAction, TState>> iterator, int inGameEntityId, int estimatedElementCount) {

        var isRiskArrayInitialized = false;
        var aggregatedRisk = new double[entityCount];
        var aggregatedTotalPayoff = new double[entityCount];

        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(estimatedElementCount);
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleVectorRewardAggregator.aggregateDiscount(previous.getEpisodeStepRecord().getReward(), aggregatedTotalPayoff, discountFactor);
            if(!isRiskArrayInitialized) {
                fillRiskArray(previous.getEpisodeStepRecord().getToState(), aggregatedRisk);
                isRiskArrayInitialized = true;
            }
            mutableDataSampleList.add(createDatasample(aggregatedRisk, aggregatedTotalPayoff, previous));
        }
        return mutableDataSampleList;
    }
}
