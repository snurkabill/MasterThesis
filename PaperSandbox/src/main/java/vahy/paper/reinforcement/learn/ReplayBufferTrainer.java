package vahy.paper.reinforcement.learn;

import vahy.paper.policy.EnvironmentPolicySupplier;
import vahy.paper.policy.PaperTrainablePaperPolicySupplier;
import vahy.paper.reinforcement.episode.PaperEpisode;
import vahy.paper.reinforcement.episode.StepRecord;
import vahy.api.model.State;
import vahy.api.model.StateActionReward;
import vahy.environment.ActionType;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.reward.DoubleScalarRewardAggregator;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReplayBufferTrainer extends AbstractTrainer {

    private final int bufferSize;
    private final LinkedList<List<ImmutableTuple<DoubleVectorialObservation, double[]>>> buffer;

    public ReplayBufferTrainer(HallwayGameInitialInstanceSupplier initialStateSupplier,
                               PaperTrainablePaperPolicySupplier paperTrainablePolicySupplier,
                               EnvironmentPolicySupplier opponentPolicySupplier,
                               int bufferSize,
                               DoubleScalarRewardAggregator rewardAggregator,
                               double discountFactor) {
        super(initialStateSupplier, paperTrainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator);
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

    public List<ImmutableTuple<DoubleVectorialObservation, double[]>> convertEpisodeToDataSamples(PaperEpisode paperEpisode) {
        List<ImmutableTuple<DoubleVectorialObservation, double[]>> episodeRaw = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, StepRecord>> episodeHistory = paperEpisode.getEpisodeStateActionRewardList();
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
