package vahy.noLinear.metadata;

import vahy.api.model.Action;
import vahy.impl.search.MCTS.MonteCarloTreeSearchMetadata;

import java.util.Map;
import java.util.function.Supplier;

public class NoLinearMetadata<TAction extends Action> extends MonteCarloTreeSearchMetadata {

    private final NoLinearMetadataViewer<TAction> noLinearMetadataViewer;

    public NoLinearMetadata(double cumulativeReward,
                            double gainedReward,
                            double predictedReward,
                            int riskLevelCount,
                            Supplier<Map<TAction, Double>> mapSupplier) {
        super(cumulativeReward, gainedReward, predictedReward);
        this.noLinearMetadataViewer = new NoLinearMetadataViewerBase<>(riskLevelCount, mapSupplier);
    }


//    public void setPriorProbability(double priorProbability) {
//        this.priorProbability = priorProbability;
//    }


    public double getExpectedReward(double riskLevel) {
        return noLinearMetadataViewer.getExpectedReward(riskLevel);
    }

    public void setExpectedReward(double riskLevel, double expectedReward) {
        noLinearMetadataViewer.setExpectedReward(riskLevel, expectedReward);
    }

    public Map<TAction, Double> getChildPriorProbabilities() {
        return noLinearMetadataViewer.getChildPriorProbabilities();
    }

    public Map<TAction, Double> getChildPriorProbabilities(double riskLevel) {
        return noLinearMetadataViewer.getChildPriorProbabilities(riskLevel);
    }

    @Override
    public double getExpectedReward() {
        return noLinearMetadataViewer.getExpectedReward();
    }

    public double getPriorProbability() {
        return noLinearMetadataViewer.getPriorProbability();
    }


}
