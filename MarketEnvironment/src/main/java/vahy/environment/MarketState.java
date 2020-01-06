package vahy.environment;

import vahy.api.model.StateRewardReturn;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.utils.EnumUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MarketState implements PaperState<MarketAction, DoubleVector, MarketProbabilities, MarketState> {

    private final boolean isAgentTurn;
    private final TradingSystemState tradingSystemState;
    private final MarketEnvironmentStaticPart marketEnvironmentStaticPart;
    private final double tradeBalance;
    private final double currentPnL;
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
                        double currentPnL,
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
        this.currentPnL = currentPnL;
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

    private double calculateNewTradeBalance(MarketAction actionType) {
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
            } else {
                return tradeBalance;
            }
        }
    }

    private double calculateNewProfitAndLoss(MarketAction actionType, double newTradeBalance, double nextBarPrice) {
        double closingFromLong = newTradeBalance + sellPrice(nextBarPrice) - baseCommission();
        double closingFromShort = newTradeBalance - buyPrice(nextBarPrice) - baseCommission();

        if(tradingSystemState.isOpenPosition()) {
            switch (actionType) {
                case NO_ACTION:
                case UP:
                case DOWN:
                    return tradingSystemState == TradingSystemState.LONG_POSITION ? closingFromLong : closingFromShort;
                case REVERSE:
                    return tradingSystemState == TradingSystemState.LONG_POSITION ? closingFromShort : closingFromLong; // swapped because reverse
                case CLOSE:
                    return newTradeBalance; // close is already settled
                default: throw EnumUtils.createExceptionForNotExpectedEnumValue(actionType);
            }
        } else {
            switch (actionType) {
                case DOWN:
                case UP:
                case NO_ACTION:
                    return newTradeBalance;
                case OPEN_LONG:
                    return closingFromLong;
                case OPEN_SHORT:
                    return closingFromShort;
                default: throw EnumUtils.createExceptionForNotExpectedEnumValue(actionType);
            }
        }
    }

    private double resolveReward(MarketAction actionType, double newProfitAndLoss) {
        return newProfitAndLoss - currentPnL;
    }

    private double[] createNewLookback(MarketAction action) {
        if(action.isPlayerAction()) {
            throw new IllegalStateException("New lookback cannot be created on player action");
        }
        double[] newLookback = new double[lookback.length];
        for (int i = 0; i < lookback.length - 1; i++) {
            newLookback[i] = lookback[i + 1] + (action == MarketAction.UP ? -1.0 : 1.0);
        }
        newLookback[lookback.length - 1] = 0.0;
        return newLookback;
    }

    @Override
    public boolean isRiskHit() {
        if(this.tradingSystemState.isOpenPosition()) {
            if(tradingSystemState == TradingSystemState.LONG_POSITION) {
                return this.tradeBalance + sellUnit(currentMidPrice) - baseCommission() <= -marketEnvironmentStaticPart.getSystemStopLoss();
            } else if(tradingSystemState == TradingSystemState.SHORT_POSITION) {
                return this.tradeBalance - buyUnit(currentMidPrice) - baseCommission() <= -marketEnvironmentStaticPart.getSystemStopLoss();
            } else {
                throw EnumUtils.createExceptionForUnknownEnumValue(tradingSystemState);
            }
        } else {
            return false;
        }
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
    public MarketAction[] getPossiblePlayerActions() {
        if(isAgentTurn) {
            if(!tradingSystemState.isOpenPosition()) {
                return MarketAction.noPositionPlayerActions;
            } else {
                return MarketAction.openPositionPlayerActions;
            }
        } else {
            return new MarketAction[0];
        }
    }

    @Override
    public MarketAction[] getPossibleOpponentActions() {
        return MarketAction.environmentActions;
    }

    @Override
    public StateRewardReturn<MarketAction, DoubleVector, MarketProbabilities, MarketState> applyAction(MarketAction actionType) {
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
        double newTradeBalance = calculateNewTradeBalance(actionType);
        double newPnl = calculateNewProfitAndLoss(actionType, newTradeBalance, nextStatePrice);
        double reward = resolveReward(actionType, newPnl);
        switch (actionType) {
            case UP:
            case DOWN:
                return new ImmutableStateRewardReturnTuple<>(
                    new MarketState(
                        true,
                        tradingSystemState,
                        marketEnvironmentStaticPart,
                        newTradeBalance,
                        newPnl,
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
                        newPnl,
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
                        newPnl,
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
                        newPnl,
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
                        newPnl,
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
                        newPnl,
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
    public DoubleVector getPlayerObservation() {
        int totalLenght = 0;
        totalLenght += lookback.length;
        totalLenght += 1; // tradeBalance;
        totalLenght += TradingSystemState.values().length; // position representation
        double[] observation = new double[totalLenght];
        for (int i = 0; i < lookback.length; i++) {
            observation[i] = lookback[i] / lookback.length;
        }
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
    public MarketProbabilities getOpponentObservation() {
        return new MarketProbabilities();
    }

    @Override
    public String readableStringRepresentation() {
        return "haha, no";
    }

    @Override
    public List<String> getCsvHeader() {
        return Collections.singletonList("Todo later");
    }

    @Override
    public List<String> getCsvRecord() {
        return Collections.singletonList("Todo later");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketState)) return false;

        MarketState that = (MarketState) o;

        if (getCurrentDataIndex() != that.getCurrentDataIndex()) return false;
        if (tradingSystemState != that.tradingSystemState) return false;
        if (isAgentTurn != that.isAgentTurn) return false;
        if (Double.compare(that.tradeBalance, tradeBalance) != 0) return false;
        if (isTradeDone != that.isTradeDone) return false;
        if (Double.compare(that.currentMidPrice, currentMidPrice) != 0) return false;
        if (shiftToEndOfDataCount != that.shiftToEndOfDataCount) return false;
        if (marketEnvironmentStaticPart != null ? !marketEnvironmentStaticPart.equals(that.marketEnvironmentStaticPart) : that.marketEnvironmentStaticPart != null) return false;
        if (!Arrays.equals(lookback, that.lookback)) return false;
        return currentMarketMovement == that.currentMarketMovement;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (isAgentTurn ? 1 : 0);
        result = 31 * result + (tradingSystemState != null ? tradingSystemState.hashCode() : 0);
        result = 31 * result + (marketEnvironmentStaticPart != null ? marketEnvironmentStaticPart.hashCode() : 0);
        temp = Double.doubleToLongBits(tradeBalance);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isTradeDone ? 1 : 0);
        result = 31 * result + Arrays.hashCode(lookback);
        temp = Double.doubleToLongBits(currentMidPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getCurrentDataIndex();
        result = 31 * result + (currentMarketMovement != null ? currentMarketMovement.hashCode() : 0);
        result = 31 * result + shiftToEndOfDataCount;
        return result;
    }
}
