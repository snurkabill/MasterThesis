package vahy.noLinear.metadata;

import vahy.api.model.Action;

import java.util.Map;

public class MetadataBucket<TAction extends Action> {

    private final double key_down;
    private final double key_up;
    private double predictedReward;
    private double priorProbability;
    private Map<TAction, Double> childPriorProbabilities;

    public MetadataBucket(double key_down, double key_up, Map<TAction, Double> childPriorProbabilities) {
        this.key_down = key_down;
        this.key_up = key_up;
        this.childPriorProbabilities = childPriorProbabilities;
    }

    public double getKey_down() {
        return key_down;
    }

    public double getKey_up() {
        return key_up;
    }

    public double getPriorProbability() {
        return priorProbability;
    }

    public void setPriorProbability(double priorProbability) {
        this.priorProbability = priorProbability;
    }

    public double getPredictedReward() {
        return predictedReward;
    }

    public void setPredictedReward(double predictedReward) {
        this.predictedReward = predictedReward;
    }

    public Map<TAction, Double> getChildPriorProbabilities() {
        return childPriorProbabilities;
    }

    public void setChildPriorProbabilities(Map<TAction, Double> childPriorProbabilities) {
        this.childPriorProbabilities = childPriorProbabilities;
    }
}
