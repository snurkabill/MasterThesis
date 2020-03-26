package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;

import java.util.List;
import java.util.function.Supplier;

public class PlayingDistribution<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private final TAction expectedPlayerAction;
    private final int expectedPlayerActionIndex;

    private final double[] playerDistribution;
    private final double[] riskOnPlayerSubNodes;
    private final List<TAction> actionList;

    private final Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> usedSubTreeRiskCalculatorSupplier;

    public PlayingDistribution(TAction expectedPlayerAction,
                               int expectedPlayerActionIndex,
                               double[] playerDistribution,
                               double[] riskOnPlayerSubNodes,
                               List<TAction> actionList,
                               Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> usedSubTreeRiskCalculatorSupplier) {
        this.expectedPlayerAction = expectedPlayerAction;
        this.expectedPlayerActionIndex = expectedPlayerActionIndex;
        this.playerDistribution = playerDistribution;
        this.riskOnPlayerSubNodes = riskOnPlayerSubNodes;
        this.actionList = actionList;
        this.usedSubTreeRiskCalculatorSupplier = usedSubTreeRiskCalculatorSupplier;

        if(playerDistribution.length != riskOnPlayerSubNodes.length) {
            throw new IllegalArgumentException("Player distribution and risks on subnodes differ in length");
        }
    }

    public TAction getExpectedPlayerAction() {
        return expectedPlayerAction;
    }

    public int getExpectedPlayerActionIndex() {
        return expectedPlayerActionIndex;
    }

    public double[] getPlayerDistribution() {
        return playerDistribution;
    }

    public double[] getRiskOnPlayerSubNodes() {
        return riskOnPlayerSubNodes;
    }

    public List<TAction> getActionList() {
        return actionList;
    }

    public Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> getUsedSubTreeRiskCalculatorSupplier() {
        return usedSubTreeRiskCalculatorSupplier;
    }
}
