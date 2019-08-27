package vahy.paperGenerics.reinforcement.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.StateActionReward;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.paperGenerics.reinforcement.episode.StepRecord;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayBufferTrainer<
    TAction extends Enum<TAction> & Action,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, DoubleReward>,
    TState extends PaperState<TAction, DoubleReward, DoubleVector, TOpponentObservation, TState>> extends AbstractTrainer<TAction, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(ReplayBufferTrainer.class.getName());

    private final int bufferSize;
    private final LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer;

    public ReplayBufferTrainer(InitialStateSupplier<TAction, DoubleReward, DoubleVector, TOpponentObservation, TState> initialStateSupplier,
                               TrainablePaperPolicySupplier<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> paperTrainablePolicySupplier,
                               PaperPolicySupplier<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                               TrainableNodeEvaluator<TAction, DoubleReward, DoubleVector, TOpponentObservation, TSearchNodeMetadata, TState> paperNodeEvaluator,
                               double discountFactor,
                               RewardAggregator<DoubleReward> rewardAggregator,
                               int stepCountLimit,
                               LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer,
                               int bufferSize,
                               ProgressTrackerSettings progressTrackerSettings) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, paperNodeEvaluator, discountFactor, rewardAggregator, progressTrackerSettings, stepCountLimit);
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

    public List<ImmutableTuple<DoubleVector, double[]>> convertEpisodeToDataSamples(EpisodeResults<TAction, DoubleReward, DoubleVector, TOpponentObservation, TState> paperEpisode) {
        List<ImmutableTuple<DoubleVector, double[]>> episodeRaw = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<TAction, DoubleReward, DoubleVector, TOpponentObservation, TState>, StepRecord<DoubleReward>>> episodeHistory = paperEpisode.getEpisodeHistoryList();
        boolean isRiskHit = paperEpisode.isRiskHit();
        for (int i = 0; i < episodeHistory.size(); i++) {
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                MutableDataSample dataSample = createDataSample(episodeHistory, i, isRiskHit);
                episodeRaw.add(new ImmutableTuple<>(episodeHistory.get(i).getFirst().getState().getPlayerObservation(), createOutputVector(dataSample)));
            }
        }
        return episodeRaw;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
