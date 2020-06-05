package vahy.api.search.node;

public interface NodeMetadata {

    double[] getCumulativeReward();

    double[] getGainedReward();

    boolean isEvaluated();

    void setEvaluated();

//    EnumMap<TAction, Double> getChildPriorProbabilities();
//
//    double getPriorProbability();
//
//
//    double[] getPredictedReward();
//
//    double[] getExpectedReward();
//
//    void setPredictedReward(double[] predictedReward);
//
//    void setExpectedReward(double[] expectedReward);
//


}
