package vahy.api.experiment;

public class CommonAlgorithmConfigBase implements CommonAlgorithmConfig {

    private final int stageCount;
    private final int batchEpisodeCount;

    public CommonAlgorithmConfigBase(int stageCount, int batchEpisodeCount) {
        this.stageCount = stageCount;
        this.batchEpisodeCount = batchEpisodeCount;
    }

    @Override
    public int getBatchEpisodeCount() {
        return batchEpisodeCount;
    }

    @Override
    public int getStageCount() {
        return stageCount;
    }

    @Override
    public String toLog() {
        return "";
    }

    @Override
    public String toFile() {
        return null;
    }
}
