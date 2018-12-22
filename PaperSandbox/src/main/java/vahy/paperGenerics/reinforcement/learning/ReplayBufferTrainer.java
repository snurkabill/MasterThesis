package vahy.paperGenerics.reinforcement.learning;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.StateActionReward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.nodeEvaluator.TrainableNodeEvaluator;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;
import vahy.paperGenerics.reinforcement.episode.EpisodeResults;
import vahy.paperGenerics.reinforcement.episode.StepRecord;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayBufferTrainer<
    TAction extends Enum<TAction> & Action,
    TSearchNodeMetadata extends PaperMetadata<TAction, DoubleReward>,
    TState extends PaperState<TAction, DoubleReward, DoubleVector, TState>> extends AbstractTrainer<TAction, TSearchNodeMetadata, TState> {

    private final int bufferSize;
    private final LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer;

    public ReplayBufferTrainer(InitialStateSupplier<TAction, DoubleReward, DoubleVector, TState> initialStateSupplier,
                               TrainablePaperPolicySupplier<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> paperTrainablePolicySupplier,
                               PaperPolicySupplier<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> opponentPolicySupplier,
                               TrainableNodeEvaluator<TAction, DoubleReward, DoubleVector, TSearchNodeMetadata, TState> paperNodeEvaluator,
                               double discountFactor,
                               RewardAggregator<DoubleReward> rewardAggregator,
                               int stepCountLimit,
                               LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer,
                               int bufferSize) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, paperNodeEvaluator, discountFactor, rewardAggregator, stepCountLimit);
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

    public List<ImmutableTuple<DoubleVector, double[]>> convertEpisodeToDataSamples(EpisodeResults<TAction, DoubleReward, DoubleVector, TState> paperEpisode) {
        List<ImmutableTuple<DoubleVector, double[]>> episodeRaw = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<TAction, DoubleReward, DoubleVector, TState>, StepRecord<DoubleReward>>> episodeHistory = paperEpisode.getEpisodeHistoryList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                MutableDataSample dataSample = createDataSample(episodeHistory, i);
                episodeRaw.add(new ImmutableTuple<>(episodeHistory.get(i).getFirst().getState().getObservation(), createOutputVector(dataSample)));
            }
        }
        return episodeRaw;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
