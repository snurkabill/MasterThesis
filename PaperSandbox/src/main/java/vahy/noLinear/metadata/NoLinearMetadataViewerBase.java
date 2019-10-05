package vahy.noLinear.metadata;

import vahy.api.model.Action;
import vahy.dataStructures.DiscretizedArrayIndexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NoLinearMetadataViewerBase<TAction extends Action> implements NoLinearMetadataViewer<TAction> {

    private DiscretizedArrayIndexer<MetadataBucket<TAction>> discretizedArrayIndexer;

    public NoLinearMetadataViewerBase(int riskLevelCount, Supplier<Map<TAction, Double>> mapSupplier) {
        var indexedList = new ArrayList<MetadataBucket<TAction>>();
        double diff = 1.0 / riskLevelCount;
        double key_down = 0.0;
        double key_up = diff;
        for (int i = 0; i < riskLevelCount; i++, key_down += diff, key_up += diff) {
            indexedList.add(new MetadataBucket<>(key_down, key_up, mapSupplier.get()));
        }
        this.discretizedArrayIndexer = new DiscretizedArrayIndexer<>(indexedList);
    }

    @Override
    public double getPriorProbability() {
        return discretizedArrayIndexer
                .getInnerList()
                .stream()
                .mapToDouble(MetadataBucket::getPriorProbability)
                .average()
                .orElseThrow(() -> new IllegalStateException("Average does not exists"));
    }

    @Override
    public double getPriorProbability(double riskLevel) {
        return discretizedArrayIndexer.getElement(riskLevel).getPriorProbability();
    }

    @Override
    public void setPriorProbability(double riskLevel, double priorProbability) {
        discretizedArrayIndexer.getElement(riskLevel).setPriorProbability(priorProbability);
    }

    @Override
    public Map<TAction, Double> getChildPriorProbabilities() {
        return discretizedArrayIndexer
                .getInnerList()
                .stream()
                .map(MetadataBucket::getChildPriorProbabilities)
                .reduce(new HashMap<>(), (tActionDoubleMap, tActionDoubleMap2) -> {
                    tActionDoubleMap2.forEach((k, v) -> tActionDoubleMap.merge(k, v, Double::sum));
                    return tActionDoubleMap;
                });
    }

    @Override
    public Map<TAction, Double> getChildPriorProbabilities(double riskLevel) {
        return discretizedArrayIndexer.getElement(riskLevel).getChildPriorProbabilities();
    }

    @Override
    public void setChildProbabilities(double riskLevel, Map<TAction, Double> childProbabilities) {
        discretizedArrayIndexer.getElement(riskLevel).setChildPriorProbabilities(childProbabilities);
    }

    @Override
    public double getExpectedReward() {
        return discretizedArrayIndexer
                .getInnerList()
                .stream()
                .mapToDouble(MetadataBucket::getPredictedReward)
                .average()
                .orElseThrow(() -> new IllegalStateException("Average does not exists"));
    }

    @Override
    public double getExpectedReward(double riskLevel) {
        return discretizedArrayIndexer.getElement(riskLevel).getPredictedReward();
    }

    @Override
    public void setExpectedReward(double riskLevel, double expectedReward) {
        discretizedArrayIndexer.getElement(riskLevel).setPredictedReward(expectedReward);
    }

}
