package vahy.noLinear.metadata;

import vahy.api.model.Action;

import java.util.Map;

public interface NoLinearMetadataViewer<TAction extends Action> {

    double getPriorProbability();

    double getPriorProbability(double riskLevel);

    void setPriorProbability(double riskLevel, double priorProbability);

    double getExpectedReward();

    double getExpectedReward(double riskLevel);

    void setExpectedReward(double riskLevel, double expectedReward);

    Map<TAction, Double> getChildPriorProbabilities();

    Map<TAction, Double> getChildPriorProbabilities(double riskLevel);

    void setChildProbabilities(double riskLevel, Map<TAction, Double> childProbabilities);

}
