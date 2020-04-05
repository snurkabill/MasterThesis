package vahy.impl.learning.trainer;

import vahy.api.learning.dataAggregator.DataAggregator;
import vahy.api.learning.trainer.EpisodeDataMaker;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.api.predictor.TrainablePredictor;

public class PredictorTrainingSetup<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final TrainablePredictor trainablePredictor;
    private final EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker;
    private final DataAggregator dataAggregator;

    public PredictorTrainingSetup(TrainablePredictor trainablePredictor, EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeDataMaker, DataAggregator dataAggregator) {
        this.trainablePredictor = trainablePredictor;
        this.episodeDataMaker = episodeDataMaker;
        this.dataAggregator = dataAggregator;
    }

    public TrainablePredictor getTrainablePredictor() {
        return trainablePredictor;
    }

    public EpisodeDataMaker<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getEpisodeDataMaker() {
        return episodeDataMaker;
    }

    public DataAggregator getDataAggregator() {
        return dataAggregator;
    }
}
