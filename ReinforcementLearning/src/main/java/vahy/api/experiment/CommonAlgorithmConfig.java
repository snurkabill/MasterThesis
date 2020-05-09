package vahy.api.experiment;


public interface CommonAlgorithmConfig extends Config {

    String getAlgorithmName();

    int getBatchEpisodeCount();

    int getStageCount();

}
