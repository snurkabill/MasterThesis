package vahy.agent.environment;

import vahy.environment.MarketAction;
import vahy.environment.MarketDataProvider;
import vahy.environment.MarketState;
import vahy.environment.RealMarketAction;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.List;

public class RealDataMarketPolicy extends MarketPolicy {

    private static final double[] probabilities = new double[] {0.5, 0.5};

    private final MarketDataProvider marketDataProvider;
    private final RealMarketAction[] realMarketActions;
    private final int startIndex;
    private int currentIndex;

    public RealDataMarketPolicy(MarketDataProvider marketDataProvider, int startIndex) {
        this.marketDataProvider = marketDataProvider;
        this.realMarketActions = marketDataProvider.getMarketMovementArray();
        this.startIndex = startIndex;
        this.currentIndex = startIndex;
    }

    @Override
    public double[] getActionProbabilityDistribution(MarketState gameState) {
        return probabilities;
    }

    @Override
    public MarketAction getDiscreteAction(MarketState gameState) {
        return realMarketActions[currentIndex] == RealMarketAction.MARKET_UP ? MarketAction.UP : MarketAction.DOWN;
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

}
