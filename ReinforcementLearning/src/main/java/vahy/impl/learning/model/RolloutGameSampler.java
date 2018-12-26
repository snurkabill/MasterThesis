package vahy.impl.learning.model;

import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;
import vahy.impl.episode.EpisodeImpl;
import vahy.impl.model.observation.DoubleVector;

import java.util.ArrayList;
import java.util.List;

public class RolloutGameSampler< // TODO: move to impl
    TAction extends Action,
    TReward extends DoubleVectorialReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    private final InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier;
    private final TrainablePolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPolicySupplier;
    private final PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicySupplier;

    public RolloutGameSampler(InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                              TrainablePolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPolicySupplier,
                              PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicySupplier) {
        this.initialStateSupplier = initialStateSupplier;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;
    }

    public List<Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> sampleEpisodes(int episodeBatchSize) {
        List<Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> episodeHistoryList = new ArrayList<>();
        for (int j = 0; j < episodeBatchSize; j++) {
            episodeHistoryList.clear(); // TODO: this is shit
            TState initialGameState = initialStateSupplier.createInitialState();
            Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> policy = playerPolicySupplier.initializePolicyWithExploration(initialGameState);
            Policy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicy = opponentPolicySupplier.initializePolicy(initialGameState);
            Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> episode = new EpisodeImpl<>(initialGameState, policy, opponentPolicy);
            episode.runEpisode();
            episodeHistoryList.add(episode);
        }
        return episodeHistoryList;
    }
}
