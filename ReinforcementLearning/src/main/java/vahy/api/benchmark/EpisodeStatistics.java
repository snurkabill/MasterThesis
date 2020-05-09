package vahy.api.benchmark;

import java.time.Duration;
import java.util.List;

public interface EpisodeStatistics {

    Duration getTotalDuration();

    List<Double> getAveragePlayerStepCount();

    List<Double> getStdevPlayerStepCount();

    double getAverageMillisPerEpisode();

    double getStdevMillisPerEpisode();

    List<Double> getTotalPayoffAverage();

    List<Double> getTotalPayoffStdev();


    String printToLog();

    String printToFile();
}
