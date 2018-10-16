package vahy.AlphaGo.reinforcement.learn;

import vahy.AlphaGo.policy.AlphaGoEnvironmentPolicySupplier;
import vahy.AlphaGo.policy.AlphaGoTrainablePolicySupplier;
import vahy.AlphaGo.reinforcement.episode.AlphaGoEpisode;
import vahy.AlphaGo.reinforcement.episode.AlphaGoStepRecord;
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
                               AlphaGoTrainablePolicySupplier trainablePolicySupplier,
                               AlphaGoEnvironmentPolicySupplier opponentPolicySupplier,
                               int bufferSize,
                               DoubleScalarRewardAggregator rewardAggregator,
                               double discountFactor) {
        super(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier, discountFactor, rewardAggregator);
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

    public List<ImmutableTuple<DoubleVectorialObservation, double[]>> convertEpisodeToDataSamples(AlphaGoEpisode episode) {
        List<ImmutableTuple<DoubleVectorialObservation, double[]>> episodeRaw = new ArrayList<>();
        List<ImmutableTuple<StateActionReward<ActionType, DoubleScalarReward, DoubleVectorialObservation, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>, AlphaGoStepRecord>> episodeHistory = episode.getEpisodeStateActionRewardList();
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
