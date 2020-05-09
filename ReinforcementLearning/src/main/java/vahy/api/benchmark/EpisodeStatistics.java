package vahy.api.benchmark;

import java.time.Duration;
import java.util.List;

public interface EpisodeStatistics {

    Duration getTotalDuration();

    List<Double> getAveragePlayerStepCount();

    List<Double> getStdevPlayerStepCount();

    double getAverageMillisPerEpisode();

    double getStdevMillisPerEpisode();

    List<List<Double>> getTotalPayoffAverage();

    List<List<Double>> getTotalPayoffStdev();


    String printToLog();

    String printToFile();
}
