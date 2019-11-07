package vahy.impl.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.DataPointGenerator;

import java.util.List;
import java.util.function.Function;

public class FromEpisodesDataPointGeneratorGeneric<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> implements DataPointGenerator {

    private final String dataTitle;
    private final Function<List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>, Double> function;

    private int counter = 0;
    private double value = Double.NaN;

    public FromEpisodesDataPointGeneratorGeneric(
        String dataTitle,
        Function<List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>>, Double> function)
    {
        this.dataTitle = dataTitle;
        this.function = function;
    }

    @Override
    public String getDataTitle() {
        return dataTitle;
    }

    @Override
    public ImmutableTuple<Double, Double> get() {
        return new ImmutableTuple<>((double) counter, value);
    }

    public void addNewValue(List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> paperEpisodeHistoryList, int episode) {
        counter = episode;
        value = function.apply(paperEpisodeHistoryList);
    }
}
