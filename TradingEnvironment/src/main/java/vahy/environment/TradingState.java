package vahy.environment;

import vahy.api.model.StateRewardReturn;
import vahy.environment.state.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;

public class TradingState implements PaperState<MarketAction, DoubleReward, DoubleVector, TradingState> {



    @Override
    public boolean isRiskHit() {
        return false;
    }

    @Override
    public MarketAction[] getAllPossibleActions() {
        return new MarketAction[0];
    }

    @Override
    public StateRewardReturn<MarketAction, DoubleReward, DoubleVector, TradingState> applyAction(MarketAction actionType) {
        return null;
    }

    @Override
    public TradingState deepCopy() {
        return null;
    }

    @Override
    public DoubleVector getObservation() {
        return null;
    }

    @Override
    public String readableStringRepresentation() {
        return null;
    }

    @Override
    public boolean isOpponentTurn() {
        return false;
    }

    @Override
    public boolean isFinalState() {
        return false;
    }
}
