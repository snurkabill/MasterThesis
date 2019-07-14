package vahy.paperGenerics.reinforcement.episode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperState;
import vahy.utils.ImmutableTuple;
import vahy.vizualiation.DataSeriesCreator;
import vahy.vizualiation.MyShittyFrameVisualization;
import vahy.vizualiation.SeriesMetadata;

import java.util.LinkedList;
import java.util.List;

public class ProgressTracker<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class.getName());

    private final SeriesMetadata stepCountSeries = new SeriesMetadata("StepCount", new LinkedList<>());
    private final SeriesMetadata totalRewardSeries = new SeriesMetadata("TotalReward", new LinkedList<>());
    private final SeriesMetadata riskHitSeries = new SeriesMetadata("RiskHit", new LinkedList<>());
    private final MyShittyFrameVisualization avgStepCountVisualization = new MyShittyFrameVisualization("Avg Step Count", "SampledBatch", "Value");
    private final MyShittyFrameVisualization avgRewardVisualization = new MyShittyFrameVisualization("Avg Total Reward", "SampledBatch", "Value");
    private final MyShittyFrameVisualization avgRiskHitVisualization = new MyShittyFrameVisualization("Avg Risk Hit", "SampledBatch", "Value");

    private int sampledBatches = 0;


    public void addData(List<EpisodeResults<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> paperEpisodeHistoryList) {
        double avgRisk = paperEpisodeHistoryList
            .stream()
            .mapToDouble(x -> x.isRiskHit() ? 1.0 : 0.0)
            .average().orElseThrow(() -> new IllegalStateException("Avg does not exist"));
        double avgReward = paperEpisodeHistoryList
            .stream()
            .mapToDouble(x -> x.getEpisodeHistoryList()
                .stream()
                .mapToDouble(y -> y.getFirst().getReward().getValue()) // TODO: reward aggregator
                .sum())
            .average().orElseThrow(() -> new IllegalArgumentException("Average does not exist"));
        double avgStepCount = paperEpisodeHistoryList
            .stream()
            .mapToInt(x -> x.getEpisodeHistoryList().size())
            .average().orElseThrow(() -> new IllegalArgumentException("Average does not exist"));

        logger.info("Average reward: [{}], Average step count: [{}], AverageRisk [{}]", avgReward, avgStepCount, avgRisk);

        stepCountSeries.addDataEntry(new ImmutableTuple<>((double) sampledBatches, avgStepCount));
        totalRewardSeries.addDataEntry(new ImmutableTuple<>((double) sampledBatches, avgReward));
        riskHitSeries.addDataEntry(new ImmutableTuple<>((double) sampledBatches, avgRisk));
        avgStepCountVisualization.draw(DataSeriesCreator.createDataset(stepCountSeries));
        avgRewardVisualization.draw(DataSeriesCreator.createDataset(totalRewardSeries));
        avgRiskHitVisualization.draw(DataSeriesCreator.createDataset(riskHitSeries));

        sampledBatches++;
    }


}
