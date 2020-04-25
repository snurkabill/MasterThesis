package vahy.api.benchmark;

import java.time.Duration;

public interface EpisodeStatistics {

    Duration getTotalDuration();

    double getAveragePlayerStepCount();

    double getStdevPlayerStepCount();

    double getAverageMillisPerEpisode();

    double getStdevMillisPerEpisode();

    double getTotalPayoffAverage();

    double getTotalPayoffStdev();


    String printToLog();

    String printToFile();
}
