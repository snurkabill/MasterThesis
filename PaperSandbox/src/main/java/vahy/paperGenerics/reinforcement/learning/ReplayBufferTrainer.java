package vahy.paperGenerics.reinforcement.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.api.predictor.TrainablePredictor;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.paperGenerics.reinforcement.episode.sampler.PaperRolloutGameSampler;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayBufferTrainer<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>> extends AbstractTrainer<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(ReplayBufferTrainer.class.getName());

    private final int bufferSize;
    private final LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer;

    public ReplayBufferTrainer(PaperRolloutGameSampler<TAction, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> gameSampler,
                               TrainablePredictor<DoubleVector> trainablePredictor,
                               double discountFactor,
                               RewardAggregator rewardAggregator,
                               LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer,
                               int bufferSize) {
        super(gameSampler, trainablePredictor, discountFactor, rewardAggregator);
        this.bufferSize = bufferSize;
        this.buffer = buffer;
    }


    @Override
    public void trainPolicy(int episodeCount) {
        getGameSampler()
            .sampleEpisodes(episodeCount)
            .stream()
            .map(this::convertEpisodeToDataSamples)
            .forEach(buffer::addLast);
        while(buffer.size() > bufferSize) {
            buffer.removeFirst();
        }
        trainPolicy(buffer.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public void printDataset() {
        for (ImmutableTuple<DoubleVector, double[]> entry : buffer.stream().flatMap(Collection::stream).collect(Collectors.toList())) {
            logger.info("Input: [{}] Target: [{}] Prediction: [{}]",
                Arrays.toString(entry.getFirst().getObservedVector()),
                Arrays.toString(entry.getSecond()),
                Arrays.toString(this.evaluatePolicy(entry.getFirst())));
        }
    }

    public List<ImmutableTuple<DoubleVector, double[]>> convertEpisodeToDataSamples(EpisodeResults<TAction, DoubleVector, TOpponentObservation, TState> paperEpisode) {
        List<ImmutableTuple<DoubleVector, double[]>> episodeRaw = new ArrayList<>();
        var mutableDataList = createDataSample(paperEpisode);
        for (int i = 0; i < mutableDataList.size(); i++) {
            var dataSample = mutableDataList.get(i);
            episodeRaw.add(new ImmutableTuple<>(dataSample.getFirst(), createOutputVector(dataSample.getSecond())));
        }
        return episodeRaw;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
