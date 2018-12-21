package vahy.environment;

import vahy.api.model.StateRewardReturn;
import vahy.environment.state.PaperState;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.utils.EnumUtils;

public class MarketState implements PaperState<MarketAction, DoubleReward, DoubleVector, MarketState> {

    private final boolean isAgentTurn;
    private final TradingSystemState tradingSystemState;
    private final TradingSystemParameters tradingSystemParameters;
    private final double tradeBalance;
    private final boolean isTradeDone;

    private final double[] lookback; // keep it simple for now
    private final int shiftToEndOfDataCount;

    public MarketState(boolean isAgentTurn,
                       TradingSystemState tradingSystemState,
                       TradingSystemParameters tradingSystemParameters,
                       double tradeBalance,
                       boolean isTradeDone,
                       double[] lookback,
                       int shiftToEndOfDataCount) {
        this.isTradeDone = isTradeDone;
        this.lookback = lookback;
        this.isAgentTurn = isAgentTurn;
        this.tradingSystemState = tradingSystemState;
        this.tradingSystemParameters = tradingSystemParameters;
        this.tradeBalance = tradeBalance;
        this.shiftToEndOfDataCount = shiftToEndOfDataCount;
    }

    private DoubleReward resolveReward(MarketAction actionType, double newestTradeBalance) {
//        if(actionType == MarketAction.CLOSE) {
//            return new DoubleReward(newestTradeBalance);
//        }
        return new DoubleReward(newestTradeBalance);
    }

    private double calculateTradeBalance(MarketAction actionType) {
        if(this.tradingSystemState.isOpenPosition()) {
            if(actionType == MarketAction.NO_ACTION || actionType == MarketAction.REVERSE || actionType == MarketAction.CLOSE) {
                return tradeBalance;
            } else if(actionType == MarketAction.OPEN_LONG || actionType == MarketAction.OPEN_SHORT) {
                return tradeBalance - tradingSystemParameters.getInstantCommissionWhenTradeOpens();
            } else if(actionType == MarketAction.UP) {
                switch (this.tradingSystemState) {
                    case LONG_POSITION:
                        return tradeBalance + this.tradingSystemParameters.getSmallestValueDiff();
                    case SHORT_POSITION:
                        return tradeBalance - this.tradingSystemParameters.getSmallestValueDiff();
                        default:
                            throw EnumUtils.createExceptionForUnknownEnumValue(tradingSystemState);
                }
            } else if(actionType == MarketAction.DOWN) {
                switch (this.tradingSystemState) {
                    case LONG_POSITION:
                        return tradeBalance - this.tradingSystemParameters.getSmallestValueDiff();
                    case SHORT_POSITION:
                        return tradeBalance + this.tradingSystemParameters.getSmallestValueDiff();
                    default:
                        throw EnumUtils.createExceptionForUnknownEnumValue(tradingSystemState);
                }
            } else {
                throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
            }
        } else {
            return tradeBalance;
        }
    }

    private double[] createNewLookback(MarketAction action) {
        if(action.isPlayerAction()) {
            throw new IllegalStateException("New lookback cannot be created on player action");
        }
        double[] newLookback = new double[lookback.length];
        System.arraycopy(lookback, 0, newLookback, 1, lookback.length - 1);
        if(action == MarketAction.UP) {
            newLookback[0] = 1.0;
        } else if(action == MarketAction.DOWN) {
            newLookback[0] = 0.0;
        }
        return newLookback;
    }

    @Override
    public boolean isRiskHit() {
        return tradeBalance <= -tradingSystemParameters.getSystemStopLoss();
    }

    @Override
    public MarketAction[] getAllPossibleActions() {
        if(isAgentTurn) {
            if(!tradingSystemState.isOpenPosition()) {
                return MarketAction.noPositionPlayerActions;
            } else {
                return MarketAction.openPositionPlayerActions;
            }
        } else {
            return MarketAction.environmentActions;
        }
    }

    @Override
    public StateRewardReturn<MarketAction, DoubleReward, DoubleVector, MarketState> applyAction(MarketAction actionType) {
        if(actionType.isPlayerAction() && !this.isAgentTurn) {
            throw new IllegalStateException("Trying to play agent action when agent is not on turn");
        }
        if(!actionType.isPlayerAction() && this.isAgentTurn) {
            throw new IllegalStateException("Trying to play environment action when agent is on turn");
        }
        if(actionType == MarketAction.REVERSE && this.tradingSystemState == TradingSystemState.NO_POSITION) {
            throw new IllegalStateException("Reverse action cannot be performed when there is no open position");
        }
        if((actionType == MarketAction.OPEN_LONG || actionType == MarketAction.OPEN_SHORT) && (tradingSystemState.isOpenPosition())) {
            throw new IllegalStateException("Cannot open position when there is already opened position");
        }
        if(actionType == MarketAction.CLOSE && !tradingSystemState.isOpenPosition()) {
            throw new IllegalStateException("Can't close position when there is none opened");
        }
        double newTradeBalance = calculateTradeBalance(actionType);
        DoubleReward reward = resolveReward(actionType, newTradeBalance);
        switch (actionType) {
            case UP:
            case DOWN:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        true,
                        tradingSystemState,
                        tradingSystemParameters,
                        newTradeBalance,
                        false,
                        createNewLookback(actionType),
                        shiftToEndOfDataCount - 1),
                    reward);

            case NO_ACTION:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        tradingSystemState,
                        tradingSystemParameters,
                        newTradeBalance,
                        false,
                        this.lookback,
                        shiftToEndOfDataCount),
                    reward);

            case OPEN_LONG:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        TradingSystemState.LONG_POSITION,
                        tradingSystemParameters,
                        newTradeBalance,
                        false,
                        this.lookback,
                        shiftToEndOfDataCount),
                    reward);

            case OPEN_SHORT:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        TradingSystemState.SHORT_POSITION,
                        tradingSystemParameters,
                        newTradeBalance,
                        false,
                        this.lookback,
                        shiftToEndOfDataCount),
                    reward);

            case REVERSE:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        tradingSystemState == TradingSystemState.LONG_POSITION ? TradingSystemState.SHORT_POSITION : TradingSystemState.LONG_POSITION,
                        tradingSystemParameters,
                        newTradeBalance,
                        false,
                        this.lookback,
                        shiftToEndOfDataCount),
                    reward);

            case CLOSE:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        TradingSystemState.NO_POSITION,
                        tradingSystemParameters,
                        newTradeBalance,
                        true,
                        this.lookback,
                        shiftToEndOfDataCount),
                    reward);

                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
        }
    }

    @Override
    public MarketState deepCopy() {
        throw new UnsupportedOperationException("Not going to implement it anyway");
    }

    @Override
    public DoubleVector getObservation() {
        int totalLenght = 0;
        totalLenght += lookback.length;
        totalLenght += 1; // tradeBalance;
        totalLenght += TradingSystemState.values().length; // position representation
        double[] observation = new double[totalLenght];
        System.arraycopy(lookback, 0, observation, 0, lookback.length);
        observation[lookback.length] = tradeBalance;
        switch (this.tradingSystemState) {
            case NO_POSITION:
                observation[lookback.length + 1] = 1.0;
                break;
            case LONG_POSITION:
                observation[lookback.length + 2] = 1.0;
                break;
            case SHORT_POSITION:
                observation[lookback.length + 3] = 1.0;
                break;
                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(tradingSystemState);
        }
        return new DoubleVector(observation);
    }

    @Override
    public String readableStringRepresentation() {
        return "haha, no";
    }

    @Override
    public boolean isOpponentTurn() {
        return !isAgentTurn;
    }

    @Override
    public boolean isFinalState() {
        return isRiskHit() || isTradeDone || noMoreData();
    }

    private boolean noMoreData() {
        if(shiftToEndOfDataCount < 0) {
            throw new IllegalStateException("Somewhere is off by one error. State can't reach negative shift ahead of end of data");
        }
        return shiftToEndOfDataCount == 0;
    }
}
