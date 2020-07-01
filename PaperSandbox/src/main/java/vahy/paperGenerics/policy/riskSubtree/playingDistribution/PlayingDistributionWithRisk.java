package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PlayingDistribution;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;

import java.util.Map;
import java.util.function.Supplier;

public class PlayingDistributionWithRisk<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>> extends PlayingDistribution<TAction> {

    private final double[] riskOnSubNodes;
    private final Map<TAction, Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>>> usedSubTreeRiskCalculatorSupplierMap;

    public PlayingDistributionWithRisk(TAction action,
                                       double expectedReward,
                                       double[] distribution,
                                       double[] riskOnSubNodes,
                                       Map<TAction, Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>>> usedSubTreeRiskCalculatorSupplierMap) {
        super(action, expectedReward, distribution);
        this.riskOnSubNodes = riskOnSubNodes;
        this.usedSubTreeRiskCalculatorSupplierMap = usedSubTreeRiskCalculatorSupplierMap;

        if(distribution.length != riskOnSubNodes.length) {
            throw new IllegalArgumentException("Player distribution and risks on subnodes differ in length");
        }
    }

    public double[] getRiskOnSubNodes() {
        return riskOnSubNodes;
    }

    public Map<TAction, Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>>> getUsedSubTreeRiskCalculatorSupplierMap() {
        return usedSubTreeRiskCalculatorSupplierMap;
    }
}
