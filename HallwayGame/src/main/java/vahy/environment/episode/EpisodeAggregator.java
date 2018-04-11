package vahy.environment.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.chart.ChartBuilder;
import vahy.environment.agent.policy.IOneHotPolicy;
import vahy.game.InitialStateInstanceFactory;
import vahy.game.NotValidGameStringRepresentationException;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class EpisodeAggregator {

    private static final Logger logger = LoggerFactory.getLogger(EpisodeAggregator.class);

    private final int uniqueEpisodeCount;
    private final int episodeIterationCount;
    private final IOneHotPolicy playerPolicy;
    private final InitialStateInstanceFactory initialStateInstanceFactory;

    public EpisodeAggregator(
        int uniqueEpisodeCount,
        int episodeIterationCount,
        IOneHotPolicy playerPolicy,
        InitialStateInstanceFactory initialStateInstanceFactory)
    {
        this.uniqueEpisodeCount = uniqueEpisodeCount;
        this.episodeIterationCount = episodeIterationCount;
        this.playerPolicy = playerPolicy;
        this.initialStateInstanceFactory = initialStateInstanceFactory;
    }

    public void runSimulation(String stringGameRepresentation) throws NotValidGameStringRepresentationException {
        logger.info("Running simulation");
        List<List<Double>> rewardHistory = new ArrayList<>();
        for (int i = 0; i < uniqueEpisodeCount; i++) {
            logger.info("Running {}th unique episode", i);
            Episode episode = new Episode(initialStateInstanceFactory.createInitialState(stringGameRepresentation), playerPolicy);
            for (int j = 0; j < episodeIterationCount; j++) {
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
