package vahy.paperGenerics.reinforcement.episode;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.DataPointGenerator;

import java.util.List;
import java.util.function.Function;

public class FromEpisodesDataPointGenerator<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> implements DataPointGenerator {

    private final String dataTitle;
    private final Function<List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>, Double> function;

    private int counter = 0;
    private double value = Double.NaN;

    public FromEpisodesDataPointGenerator(String dataTitle, Function<List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>, Double> function) {
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

    public void addNewValue(List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> paperEpisodeHistoryList, int episode) {
        counter = episode;
        value = function.apply(paperEpisodeHistoryList);
    }

}
