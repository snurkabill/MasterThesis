package vahy.api.search.node;

public interface SearchNodeMetadata {

    double getCumulativeReward();

    double getGainedReward();

    double getPredictedReward();

    double getExpectedReward();

    void setPredictedReward(double predictedReward);

    void setExpectedReward(double expectedReward);

    boolean isEvaluated();

    void setEvaluated();

}
