package vahy.api.benchmark;

public interface EpisodeStatistics {

    double getAveragePlayerStepCount();

    double getStdevPlayerStepCount();

    double getAverageMillisPerEpisode();

    double getStdevMillisPerEpisode();

    double getTotalPayoffAverage();

    double getTotalPayoffStdev();


    String printToLog();

    String printToFile();
}
