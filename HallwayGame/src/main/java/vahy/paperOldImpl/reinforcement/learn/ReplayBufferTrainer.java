package vahy.paperOldImpl.reinforcement.learn;

import vahy.api.model.StateActionReward;
import vahy.environment.HallwayAction;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.paperOldImpl.policy.EnvironmentPolicySupplier;
import vahy.paperOldImpl.policy.PaperTrainablePaperPolicySupplier;
import vahy.paperOldImpl.reinforcement.episode.PaperEpisode;
import vahy.paperGenerics.reinforcement.episode.StepRecord;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayBufferTrainer extends AbstractTrainer {

    private final int bufferSize;
    private final LinkedList<List<ImmutableTuple<DoubleVector, double[]>>> buffer;

    public ReplayBufferTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier,
                               PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier,
                               EnvironmentPolicySupplier opponentPolicySupplier,
                               int bufferSize,
                               DoubleScalarRewardAggregator rewardAggregator,
                               double discountFactor,
                               int stepCountLimit) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator, stepCountLimit);
        this.bufferSize = bufferSize;
        this.buffer = new LinkedList<>();
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

    public List<ImmutableTuple<DoubleVector, double[]>> convertEpisodeToDataSamples(PaperEpisode paperEpisode) {
        List<ImmutableTuple<DoubleVector, double[]>> episodeRaw = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<HallwayAction, DoubleReward, DoubleVector, EnvironmentProbabilities, HallwayStateImpl>, StepRecord>> episodeHistory = paperEpisode.getEpisodeStateActionRewardList();
        for (int i = 0; i < episodeHistory.size(); i++) {
            if(!episodeHistory.get(i).getFirst().getState().isOpponentTurn()) {
                MutableDataSample dataSample = createDataSample(episodeHistory, i);
                episodeRaw.add(new ImmutableTuple<>(episodeHistory.get(i).getFirst().getState().getPlayerObservation(), createOutputVector(dataSample)));
            }
        }
        return episodeRaw;
    }


    public int getBufferSize() {
        return bufferSize;
    }
}
