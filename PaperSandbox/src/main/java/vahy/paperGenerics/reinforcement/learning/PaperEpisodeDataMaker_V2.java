package vahy.paperGenerics.reinforcement.learning;

import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaperEpisodeDataMaker_V2<TAction extends Enum<TAction> & Action, TState extends PaperState<TAction, DoubleVector, TState>>
    implements EpisodeDataMaker<TAction, DoubleVector, TState> {

    private final double discountFactor;
    private final int playerPolicyId;
    private final int actionCount;

    public PaperEpisodeDataMaker_V2(double discountFactor, int actionCount, int playerPolicyId) {
        this.discountFactor = discountFactor;
        this.playerPolicyId = playerPolicyId;
        this.actionCount = actionCount;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var entityInGameCount = episodeHistory.get(0).getFromState().getTotalEntityCount();
        var translationMap = episodeResults.getPolicyIdTranslationMap();
        var inGameEntityId = translationMap.getInGameEntityId(playerPolicyId);
        var booleanRiskArray = episodeHistory.get(episodeHistory.size() - 1).getToState().getRiskVector();
        var aggregatedRisk = new double[entityInGameCount];
        for (int i = 0; i < aggregatedRisk.length; i++) {
            aggregatedRisk[i] = booleanRiskArray[i] ? 1.0 : 0.0;
        }
        var aggregatedTotalPayoff = new double[entityInGameCount];
        var iterator = episodeHistory.listIterator(episodeResults.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCountList().get(inGameEntityId));
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            if (previous.getFromState().isInGame(inGameEntityId)) {
                aggregatedTotalPayoff = DoubleVectorRewardAggregator.aggregateDiscount(previous.getReward(), aggregatedTotalPayoff, discountFactor);
                var doubleArray = new double[entityInGameCount * 2 + actionCount];

                if(previous.getPolicyIdOnTurn() == playerPolicyId) {
                    var policyArray = previous.getPolicyStepRecord().getPolicyProbabilities();
                    System.arraycopy(policyArray, 0, doubleArray, entityInGameCount * 2, policyArray.length);
                    System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                    System.arraycopy(aggregatedRisk, 0, doubleArray, entityInGameCount, aggregatedTotalPayoff.length);
                    mutableDataSampleList.add(new ImmutableTuple<>(previous.getFromState().getInGameEntityObservation(inGameEntityId), new MutableDoubleArray(doubleArray, false)));
                } else {
                    var action = previous.getAction();
                    var actionId = action.ordinal();
                    doubleArray[entityInGameCount * 2 + actionId] = 1.0;
                    System.arraycopy(aggregatedTotalPayoff, 0, doubleArray, 0, aggregatedTotalPayoff.length);
                    System.arraycopy(aggregatedRisk, 0, doubleArray, entityInGameCount, aggregatedTotalPayoff.length);
                    mutableDataSampleList.add(new ImmutableTuple<>(previous.getFromState().getInGameEntityObservation(inGameEntityId), new MutableDoubleArray(doubleArray, false)));
                }
            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }
}
