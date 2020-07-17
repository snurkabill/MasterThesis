package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class OpponentSamplerDataMaker<TAction extends Enum<TAction> & Action, TState extends State<TAction, DoubleVector, TState>>
    implements EpisodeDataMaker<TAction, DoubleVector, TState> {

    private final int allOpponentActions;
    private final int playerPolicyId;
    private final int opponentPolicyId;


    public OpponentSamplerDataMaker(int allOpponentActions, int playerPolicyId, int opponentPolicyId) {
        this.allOpponentActions = allOpponentActions;
        this.playerPolicyId = playerPolicyId;
        this.opponentPolicyId = opponentPolicyId;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TState> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCountList().get(opponentPolicyId));
        for (EpisodeStepRecord<TAction, DoubleVector, TState> entry : episodeHistory) {
            if(entry.getPolicyIdOnTurn() == opponentPolicyId) {
                var doubleArray = new double[allOpponentActions];
                doubleArray[entry.getAction().getLocalIndex()] = 1.0;
                mutableDataSampleList.add(new ImmutableTuple<>(entry.getFromState().getCommonObservation(playerPolicyId), new MutableDoubleArray(doubleArray, false)));
            }
        }
        return mutableDataSampleList;
    }
}
