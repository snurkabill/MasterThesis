package vahy.environment.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.IPolicy;
import vahy.environment.agent.policy.IStatefulPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.InitialStateInstanceFactory;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EpisodeAggregator {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeAggregator.class);

    private final int uniqueEpisodeCount;
    private final int episodeIterationCount;
    private final Function<ImmutableStateImpl, ImmutableTuple<IStatefulPolicy, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> playerPolicySupplier;
    private final IPolicy opponentPolicy;
    private final InitialStateInstanceFactory initialStateInstanceFactory;

    public EpisodeAggregator(
        int uniqueEpisodeCount,
        int episodeIterationCount,
        Function<ImmutableStateImpl, ImmutableTuple<IStatefulPolicy , State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> playerPolicySupplier,
        IPolicy opponentPolicy,
        InitialStateInstanceFactory initialStateInstanceFactory)
    {
        this.uniqueEpisodeCount = uniqueEpisodeCount;
        this.episodeIterationCount = episodeIterationCount;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicy = opponentPolicy;
        this.initialStateInstanceFactory = initialStateInstanceFactory;
    }

    public List<List<Double>> runSimulation(String stringGameRepresentation) throws NotValidGameStringRepresentationException {
        logger.info("Running simulation");
        List<List<Double>> rewardHistory = new ArrayList<>();
        for (int i = 0; i < uniqueEpisodeCount; i++) {
            logger.info("Running {}th unique episode", i);
            for (int j = 0; j < episodeIterationCount; j++) {
                ImmutableStateImpl initialGameState = initialStateInstanceFactory.createInitialState(stringGameRepresentation);
                ImmutableTuple<IStatefulPolicy , State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> policy = playerPolicySupplier.apply(initialGameState);
                Episode episode = new Episode(policy.getSecond(), policy.getFirst(), opponentPolicy);
                System.out.println("Running [" + i +"] a [" + j +  "] episode");
                rewardHistory.add(episode.runEpisode().stream().map(x -> x.getReward().getValue()).collect(Collectors.toList()));
            }
        }
        return rewardHistory;
    }



}
