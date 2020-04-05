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

    @Override
    public List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(EpisodeResults<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> episodeResults) {
        var episodeHistory = episodeResults.getEpisodeHistory();
        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>(episodeResults.getPlayerStepCount());
        for (EpisodeStepRecord<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> entry : episodeHistory) {
            if(!entry.isPlayerMove()) {
                var policyArray = entry.getPolicyStepRecord().getPolicyProbabilities();
                var doubleArray = new double[policyArray.length];
                System.arraycopy(policyArray, 0, doubleArray, 0, policyArray.length);
                mutableDataSampleList.add(new ImmutableTuple<>(entry.getFromState().getPlayerObservation(), new MutableDoubleArray(doubleArray, false)));
            }
        }
        return mutableDataSampleList;
    }
}
