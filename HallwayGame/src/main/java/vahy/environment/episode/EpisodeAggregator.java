package vahy.environment.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.State;
import vahy.chart.ChartBuilder;
import vahy.environment.ActionType;
import vahy.environment.agent.policy.AbstractTreeSearchPolicy;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.environment.state.ImmutableStateImpl;
import vahy.game.InitialStateInstanceFactory;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.model.DoubleVectorialObservation;
import vahy.impl.search.node.nodeMetadata.empty.EmptySearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.empty.EmptyStateActionMetadata;
import vahy.utils.ImmutableTuple;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EpisodeAggregator {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeAggregator.class);

    private final int uniqueEpisodeCount;
    private final int episodeIterationCount;
    private final Function<ImmutableStateImpl, ImmutableTuple<AbstractTreeSearchPolicy<EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> playerPolicySupplier;
    private final IOneHotPolicy opponentPolicy;
    private final InitialStateInstanceFactory initialStateInstanceFactory;

    public EpisodeAggregator(
        int uniqueEpisodeCount,
        int episodeIterationCount,
        Function<ImmutableStateImpl, ImmutableTuple<AbstractTreeSearchPolicy<EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>>> playerPolicySupplier,
        IOneHotPolicy opponentPolicy,
        InitialStateInstanceFactory initialStateInstanceFactory)
    {
        this.uniqueEpisodeCount = uniqueEpisodeCount;
        this.episodeIterationCount = episodeIterationCount;
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicy = opponentPolicy;
        this.initialStateInstanceFactory = initialStateInstanceFactory;
    }

    public void runSimulation(String stringGameRepresentation) throws NotValidGameStringRepresentationException {
        logger.info("Running simulation");
        List<List<Double>> rewardHistory = new ArrayList<>();
        for (int i = 0; i < uniqueEpisodeCount; i++) {
            logger.info("Running {}th unique episode", i);
            for (int j = 0; j < episodeIterationCount; j++) {
                ImmutableStateImpl initialGameState = initialStateInstanceFactory.createInitialState(stringGameRepresentation);
                ImmutableTuple<AbstractTreeSearchPolicy<EmptyStateActionMetadata<DoubleScalarReward>, EmptySearchNodeMetadata<ActionType, DoubleScalarReward>>, State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> policy = playerPolicySupplier.apply(initialGameState);
                Episode episode = new Episode(policy.getSecond(), policy.getFirst(), opponentPolicy);
                System.out.println("Running [" + i +"] a [" + j +  "] episode");
                rewardHistory.add(episode.runEpisode().stream().map(x -> x.getReward().getValue()).collect(Collectors.toList()));
            }
        }

        LinkedList<Double> average = new LinkedList<>();
        for (int j = 0; j < rewardHistory.stream().mapToInt(List::size).max().getAsInt(); j++) {

            Double jThSum = 0.0;
            int count = 0;
            for (List<Double> aRewardHistory : rewardHistory) {
                if (aRewardHistory.size() > j) {
                    jThSum += aRewardHistory.get(j);
                    count++;
                }
            }
            average.add(jThSum / count);
        }

        LinkedList<Double> runningSum = new LinkedList<>();
        for (Double value : average) {
            if(runningSum.isEmpty()) {
                runningSum.add(value);
            } else {
                runningSum.add(runningSum.getLast() + value);
            }
        }
        List<List<Double>> datasets = new ArrayList<>();
        datasets.add(average);
        datasets.add(runningSum);
        ChartBuilder.chart(new File("average"), datasets, "datasets");

    }



}
