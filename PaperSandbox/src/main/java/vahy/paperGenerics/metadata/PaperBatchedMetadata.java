package vahy.paperGenerics.metadata;

import vahy.api.model.Action;
import vahy.api.search.node.factory.BatchedSearchNodeMetadata;

import java.util.Map;

public class PaperBatchedMetadata<TAction extends Action> extends PaperMetadata<TAction> implements BatchedSearchNodeMetadata {

    private boolean isVisible = false;

    public PaperBatchedMetadata(double cumulativeReward,
                                double gainedReward,
                                double predictedReward,
                                double priorProbability,
                                double predictedRisk,
                                Map<TAction, Double> childPriorProbabilities) {
        super(cumulativeReward, gainedReward, predictedReward, priorProbability, predictedRisk, childPriorProbabilities);
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

    @Override
    public void setVisible() {
        isVisible = true;
    }
}
