package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleVectorRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VectorValueDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>>
    implements EpisodeDataMaker<TAction, DoubleVector, TState> {

    private final double discountFactor;
    private final int playerPolicyId;

    public VectorValueDataMaker(double discountFactor, int playerPolicyId) {
        this.discountFactor = discountFactor;
        this.playerPolicyId = playerPolicyId;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var entityCount = episodeHistory.get(0).getFromState().getTotalEntityCount();
        var translationMap = episodeResults.getPolicyIdTranslationMap();
        var inGameEntityId = translationMap.getInGameEntityId(playerPolicyId);
        var aggregatedTotalPayoff = new double[entityCount];
        var iterator = episodeHistory.listIterator(episodeResults.getTotalStepCount());
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCountList().get(inGameEntityId));
        while(iterator.hasPrevious()) {
            var previous = iterator.previous();
            if(previous.getFromState().isInGame(inGameEntityId)) {
                aggregatedTotalPayoff = DoubleVectorRewardAggregator.aggregateDiscount(previous.getReward(), aggregatedTotalPayoff, discountFactor);
//            if(previous.getPolicyIdOnTurn() == playerPolicyId) {

                mutableDataSampleList.add(new ImmutableTuple<>(previous.getFromState().getInGameEntityObservation(inGameEntityId), new MutableDoubleArray(aggregatedTotalPayoff, false)));
//            }
            }
        }
        Collections.reverse(mutableDataSampleList);
        return mutableDataSampleList;
    }
}
