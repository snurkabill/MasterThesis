package vahy.agent.environment;

import vahy.environment.MarketAction;
import vahy.environment.MarketState;

import java.util.List;
import java.util.SplittableRandom;

public class RandomMarketPolicy extends MarketPolicy {

    private static final double[] probabilities = new double[] {0.5, 0.5};

    private final SplittableRandom random;

    public RandomMarketPolicy(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public double[] getActionProbabilityDistribution(MarketState gameState) {
        return probabilities;
    }

    @Override
    public MarketAction getDiscreteAction(MarketState gameState) {
        return random.nextBoolean() ? MarketAction.UP : MarketAction.DOWN;
    }

    @Override
    public void updateStateOnPlayedActions(List<MarketAction> opponentActionList) {
        // this is it
    }
}
