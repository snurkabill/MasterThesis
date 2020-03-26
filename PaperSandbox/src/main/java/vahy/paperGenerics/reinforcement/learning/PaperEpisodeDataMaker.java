package vahy.paperGenerics.reinforcement.learning;

import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperGenerics.PaperModel;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaperEpisodeDataMaker<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecordBase>
    implements EpisodeDataMaker<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> {

    private final double discountFactor;

    public PaperEpisodeDataMaker(double discountFactor) {
        this.discountFactor = discountFactor;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var aggregatedRisk = episodeHistory.get(episodeHistory.size() - 1).getToState().isRiskHit() ? 1.0 : 0.0;
        var aggregatedTotalPayoff = 0.0;
        var iterator = episodeHistory.listIterator(episodeResults.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getReward(), aggregatedTotalPayoff, discountFactor);
            if(previous.isPlayerMove()) {
                var policyArray = previous.getPolicyStepRecord().getPolicyProbabilities();
                var doubleArray = new double[policyArray.length + PaperModel.POLICY_START_INDEX];
                doubleArray[PaperModel.Q_VALUE_INDEX] = aggregatedTotalPayoff;
                doubleArray[PaperModel.RISK_VALUE_INDEX] = aggregatedRisk;
                System.arraycopy(policyArray, 0, doubleArray, PaperModel.POLICY_START_INDEX, policyArray.length);
                mutableDataSampleList.add(new ImmutableTuple<>(
                    previous.getFromState().getPlayerObservation(),
                    new MutableDoubleArray(doubleArray, false)));
            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }
}
