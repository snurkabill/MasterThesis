package vahy.impl.learning.trainer;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeStepRecord;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecordBase;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;

public class OpponentSamplerDataMaker<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TState extends State<TAction, DoubleVector, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecordBase>
    implements EpisodeDataMaker<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> {

    private final int allOpponentActions;

    public OpponentSamplerDataMaker(int allOpponentActions) {
        this.allOpponentActions = allOpponentActions;
    }

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCount());
        for (EpisodeStepRecord<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> entry : episodeHistory) {
            if(!entry.isPlayerMove()) {
                var doubleArray = new double[allOpponentActions];
                doubleArray[entry.getPlayedAction().getActionIndexInOpponentActions()] = 1.0;
                mutableDataSampleList.add(new ImmutableTuple<>(entry.getFromState().getPlayerObservation(), new MutableDoubleArray(doubleArray, false)));
            }
        }
        return mutableDataSampleList;
    }
}
