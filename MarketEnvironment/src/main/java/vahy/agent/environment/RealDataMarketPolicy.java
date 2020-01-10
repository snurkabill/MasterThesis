package vahy.agent.environment;

import vahy.environment.MarketAction;
import vahy.environment.MarketState;
import vahy.environment.RealMarketAction;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.List;

public class RealDataMarketPolicy extends MarketPolicy {

    private static final double[] probabilities = new double[] {0.5, 0.5};

    private final RealMarketAction[] realMarketActionSequence;
    private final int startIndex;
    private int currentIndex;

    public RealDataMarketPolicy(RealMarketAction[] realMarketActionSequence, int startIndex) {
        this.realMarketActionSequence = realMarketActionSequence;
        this.startIndex = startIndex;
        this.currentIndex = startIndex;
    }

    @Override
    public double[] getActionProbabilityDistribution(MarketState gameState) {
        return probabilities;
    }

    @Override
    public MarketAction getDiscreteAction(MarketState gameState) {
        return realMarketActionSequence[currentIndex] == RealMarketAction.MARKET_UP ? MarketAction.UP : MarketAction.DOWN;
    }

    @Override
    public void updateStateOnPlayedActions(List<MarketAction> opponentActionList) {
        for (MarketAction marketAction : opponentActionList) {
            if(!marketAction.isPlayerAction()) {
                currentIndex++;
            }
        }
    }

    @Override
    public PaperPolicyRecord getPolicyRecord(MarketState gameState) {
        return new PaperPolicyRecord(probabilities, probabilities, 0.0, 0.0, 0.0, 0);
    }

    @Override
    public double[] getPriorActionProbabilityDistribution(MarketState gameState) {
        return probabilities;
    }

    @Override
    public double getEstimatedReward(MarketState gameState) {
        return 0.0;
    }

    @Override
    public double getEstimatedRisk(MarketState gameState) {
        return 0.0;
    }

    @Override
    public double getInnerRiskAllowed() {
        return 0;
    }

}
