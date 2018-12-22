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
    private final MarketEnvironmentStaticPart marketEnvironmentStaticPart;
    private final double tradeBalance;
    private final boolean isTradeDone;
    private final double[] lookback; // keep it simple for now
    private final double currentMidPrice;
    private final int currentDataIndex;
    private final RealMarketAction currentMarketMovement;
    private final int shiftToEndOfDataCount;

    public MarketState(TradingSystemState tradingSystemState,
                       MarketEnvironmentStaticPart marketEnvironmentStaticPart,
                       double[] lookback,
                       double currentMidPrice,
                       RealMarketAction currentMarketMovement,
                       int dataIndex,
                       int shiftToEndOfDataCount) {
        this(true,
            tradingSystemState,
            marketEnvironmentStaticPart,
            0.0,
            false,
            lookback,
            currentMidPrice,
            currentMarketMovement,
            dataIndex,
            shiftToEndOfDataCount);
    }

    private MarketState(boolean isAgentTurn,
                        TradingSystemState tradingSystemState,
                        MarketEnvironmentStaticPart marketEnvironmentStaticPart,
                        double tradeBalance,
                        boolean isTradeDone,
                        double[] lookback,
                        double currentMidPrice,
                        RealMarketAction currentMarketMovement,
                        int currentDataIndex,
                        int shiftToEndOfDataCount) {
        this.isTradeDone = isTradeDone;
        this.lookback = lookback;
        this.currentMidPrice = currentMidPrice;
        this.currentDataIndex = currentDataIndex;
        this.currentMarketMovement = currentMarketMovement;
        this.isAgentTurn = isAgentTurn;
        this.tradingSystemState = tradingSystemState;
        this.marketEnvironmentStaticPart = marketEnvironmentStaticPart;
        this.tradeBalance = tradeBalance;
        this.shiftToEndOfDataCount = shiftToEndOfDataCount;
    }

    public int getCurrentDataIndex() {
        return currentDataIndex;
    }

    private double buyPrice(double price) {
        return price + marketEnvironmentStaticPart.getConstantSpread() / 2.0;
    }

    private double sellPrice(double price) {
        return price - marketEnvironmentStaticPart.getConstantSpread() / 2.0;
    }

    private double buyUnit(double price) {
        return buyPrice(price) * marketEnvironmentStaticPart.getSize();
    }

    private double sellUnit(double price) {
        return sellPrice(price) * marketEnvironmentStaticPart.getSize();
    }

    private double baseCommission()  {
        return this.marketEnvironmentStaticPart.getCommission() * this.marketEnvironmentStaticPart.getSize();
    }

    private double calculateTradeBalance(MarketAction actionType) {
        if(this.tradingSystemState.isOpenPosition()) {
            if(actionType == MarketAction.NO_ACTION) {
                return tradeBalance;
            } else if(actionType == MarketAction.REVERSE) {
                if (this.tradingSystemState == TradingSystemState.LONG_POSITION) {
                    return tradeBalance + (sellUnit(currentMidPrice) - baseCommission()) * 2;
                } else {
                    return tradeBalance - (buyUnit(currentMidPrice) - baseCommission()) * 2;
                }
            } else if(actionType == MarketAction.CLOSE) {
                if(tradingSystemState == TradingSystemState.LONG_POSITION) {
                    return tradeBalance + sellUnit(currentMidPrice) - baseCommission();
                } else {
                    return tradeBalance - buyUnit(currentMidPrice) - baseCommission();
                }
            } else if(actionType == MarketAction.UP || actionType == MarketAction.DOWN) {
                return tradeBalance;
            } else {
                throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
            }
        } else {
            if(actionType == MarketAction.OPEN_LONG) {
                return tradeBalance - buyUnit(currentMidPrice) - baseCommission();
            } else if(actionType == MarketAction.OPEN_SHORT) {
                return tradeBalance + sellUnit(currentMidPrice) - baseCommission();
//                       + (currentMidPrice - this.marketEnvironmentStaticPart.getConstantSpread() / 2.0) * this.marketEnvironmentStaticPart.getSize()
            } else {
                return 0.0;
            }
        }
    }

    private double calculateProfitAndLoss(MarketAction actionType, double newTradeBalance, double nextBarPrice) {
        if(this.tradingSystemState.isOpenPosition()) {
            if(actionType == MarketAction.NO_ACTION ) {
                switch (this.tradingSystemState) {
                    case LONG_POSITION:
                        return newTradeBalance + sellUnit(currentMidPrice);
                    case SHORT_POSITION:
                        return newTradeBalance - buyUnit(currentMidPrice);
                    default: throw EnumUtils.createExceptionForUnknownEnumValue(tradingSystemState);
                }
            } else if(actionType == MarketAction.CLOSE) {
                return newTradeBalance;
            } else if(actionType == MarketAction.OPEN_LONG) {
                return newTradeBalance + sellUnit(currentMidPrice);
            } else if(actionType == MarketAction.OPEN_SHORT) {
                return newTradeBalance - buyUnit(currentMidPrice);
            } else if(actionType == MarketAction.REVERSE ) {
                switch (this.tradingSystemState) {
                    case LONG_POSITION:
                        return - buyUnit(currentMidPrice);
                    case SHORT_POSITION:
                        return + sellUnit(currentMidPrice);
                        default: throw EnumUtils.createExceptionForUnknownEnumValue(tradingSystemState);
                }
            } else if(actionType == MarketAction.UP || actionType == MarketAction.DOWN) {
                switch (this.tradingSystemState) {
                    case LONG_POSITION:
                        return newTradeBalance + sellUnit(nextBarPrice);
                    case SHORT_POSITION:
                        return newTradeBalance - buyUnit(nextBarPrice);
                    default: throw EnumUtils.createExceptionForUnknownEnumValue(tradingSystemState);
                }
            } else {
                throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
            }
        } else {
            return 0.0;
        }
    }

    private DoubleReward resolveReward(MarketAction actionType, double profitAndLoss) {
//        if(actionType == MarketAction.CLOSE) {
//            return new DoubleReward(profitAndLoss);
//        }
        return new DoubleReward(profitAndLoss);
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
        return tradeBalance <= -marketEnvironmentStaticPart.getSystemStopLoss();
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
        double nextStatePrice = calculateNextStatePrice(actionType);
        double newTradeBalance = calculateTradeBalance(actionType);
        double pnl = calculateProfitAndLoss(actionType, newTradeBalance, nextStatePrice);
        DoubleReward reward = resolveReward(actionType, pnl);
        switch (actionType) {
            case UP:
            case DOWN:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        true,
                        tradingSystemState,
                        marketEnvironmentStaticPart,
                        newTradeBalance,
                        false,
                        createNewLookback(actionType),
                        nextStatePrice,
                        actionType == MarketAction.UP ? RealMarketAction.MARKET_UP : RealMarketAction.MARKET_DOWN,
                        currentDataIndex + 1,
                        shiftToEndOfDataCount - 1),
                    reward);

            case NO_ACTION:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        tradingSystemState,
                        marketEnvironmentStaticPart,
                        newTradeBalance,
                        false,
                        this.lookback,
                        nextStatePrice,
                        currentMarketMovement,
                        currentDataIndex,
                        shiftToEndOfDataCount),
                    reward);

            case OPEN_LONG:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        TradingSystemState.LONG_POSITION,
                        marketEnvironmentStaticPart,
                        newTradeBalance,
                        false,
                        this.lookback,
                        nextStatePrice,
                        currentMarketMovement,
                        currentDataIndex,
                        shiftToEndOfDataCount),
                    reward);

            case OPEN_SHORT:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        TradingSystemState.SHORT_POSITION,
                        marketEnvironmentStaticPart,
                        newTradeBalance,
                        false,
                        this.lookback,
                        nextStatePrice,
                        currentMarketMovement,
                        currentDataIndex,
                        shiftToEndOfDataCount),
                    reward);

            case REVERSE:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        tradingSystemState == TradingSystemState.LONG_POSITION ? TradingSystemState.SHORT_POSITION : TradingSystemState.LONG_POSITION,
                        marketEnvironmentStaticPart,
                        newTradeBalance,
                        false,
                        this.lookback,
                        nextStatePrice,
                        currentMarketMovement,
                        currentDataIndex,
                        shiftToEndOfDataCount),
                    reward);

            case CLOSE:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        false,
                        TradingSystemState.NO_POSITION,
                        marketEnvironmentStaticPart,
                        newTradeBalance,
                        true,
                        this.lookback,
                        nextStatePrice,
                        currentMarketMovement,
                        currentDataIndex,
                        shiftToEndOfDataCount),
                    reward);

                default:
                    throw EnumUtils.createExceptionForUnknownEnumValue(actionType);
        }
    }

    private double calculateNextStatePrice(MarketAction actionType) {
        if(actionType.isPlayerAction()) {
            return currentMidPrice;
        } else {
            if(actionType == MarketAction.UP) {
                return currentMidPrice + marketEnvironmentStaticPart.getPriceRange() * (currentMarketMovement == RealMarketAction.MARKET_UP ? 1 : 2);
            } else if(actionType == MarketAction.DOWN){
                return currentMidPrice - marketEnvironmentStaticPart.getPriceRange() * (currentMarketMovement == RealMarketAction.MARKET_DOWN ? 1 : 2);
            } else {
                throw new IllegalStateException("Not expected enum value: [" + actionType + "]");
            }
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