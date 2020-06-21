package vahy.paperGenerics.reinforcement.learning;

import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
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

public class PaperEpisodeDataMaker_V1<TAction extends Enum<TAction> & Action, TState extends PaperState<TAction, DoubleVector, TState>, TPolicyRecord extends PolicyRecordBase>
    implements EpisodeDataMaker<TAction, DoubleVector, TState, TPolicyRecord> {

    private final double discountFactor;
    private final int playerPolicyId;

    public PaperEpisodeDataMaker_V1(double discountFactor, int playerPolicyId) {
        this.discountFactor = discountFactor;
        this.playerPolicyId = playerPolicyId;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState, TPolicyRecord> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var translationMap = episodeResults.getPolicyIdTranslationMap();
        var inGameEntityId = translationMap.getInGameEntityId(playerPolicyId);
        var aggregatedRisk = episodeHistory.get(episodeHistory.size() - 1).getToState().isRiskHit(inGameEntityId) ? 1.0 : 0.0;
        var aggregatedTotalPayoff = 0.0;
        var iterator = episodeHistory.listIterator(episodeResults.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCountList().get(inGameEntityId));
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getReward()[inGameEntityId], aggregatedTotalPayoff, discountFactor);
            if(previous.getInGameEntityIdOnTurn() == inGameEntityId) {
                var policyArray = previous.getPolicyStepRecord().getPolicyProbabilities();
                var doubleArray = new double[policyArray.length + PaperModel.POLICY_START_INDEX];
                doubleArray[PaperModel.Q_VALUE_INDEX] = aggregatedTotalPayoff;
                doubleArray[PaperModel.RISK_VALUE_INDEX] = aggregatedRisk;
                System.arraycopy(policyArray, 0, doubleArray, PaperModel.POLICY_START_INDEX, policyArray.length);
                mutableDataSampleList.add(new ImmutableTuple<>(previous.getFromState().getInGameEntityObservation(inGameEntityId), new MutableDoubleArray(doubleArray, false)));
            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }
}
