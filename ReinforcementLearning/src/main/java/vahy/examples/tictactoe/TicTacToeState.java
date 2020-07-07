package vahy.examples.tictactoe;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TicTacToeState implements State<TicTacToeAction, DoubleVector, TicTacToeState>, Observation {

    public static final double[] PLAYER_ZERO_WON_REWARD = new double[] {1.0, -1.0};
    public static final double[] PLAYER_ONE_WON_REWARD = new double[] {-1.0, 1.0};
    public static final double[] IN_GAME_REWARD = new double[] {0.0, 0.0};

    private enum Player_inner {
        PLAYER_ZERO(Symbol.PLAYER_ZERO_SYMBOL),
        PLAYER_ONE(Symbol.PLAYER_ONE_SYMBOL);

        private final Symbol symbol;

        Player_inner(Symbol symbol) {
            this.symbol = symbol;
        }

        public Symbol getSymbol() {
            return symbol;
        }
    }

    public enum Symbol {
        PLAYER_ZERO_SYMBOL(1),
        PLAYER_ONE_SYMBOL(2),
        EMPTY(0);

        private final int symbol;

        Symbol(int symbol) {
            this.symbol = symbol;
        }

        public int getSymbol() {
            return symbol;
        }
    }

    private final int dimension;
    private final Symbol[][] playground;
    private final boolean isPlayerZeroOnTurn;
    private final int turnsLeft;

    private final List<TicTacToeAction> enabledActions;

    public TicTacToeState(int dimension, Symbol[][] playground, boolean isPlayerZeroOnTurn, int turnsLeft, List<TicTacToeAction> enabledActions) {
        this.dimension = dimension;
        for (int i = 0; i < playground.length; i++) {
            if(playground[i].length != playground.length) {
                throw new IllegalArgumentException("Playground is not square-like");
            }
        }
        this.playground = playground;
        this.isPlayerZeroOnTurn = isPlayerZeroOnTurn;
        this.turnsLeft = turnsLeft;
        this.enabledActions = enabledActions;
    }

    private Symbol checkVerticalLine(int verticalIndex, Symbol[][] playground) {
        Symbol first = playground[verticalIndex][0];
        for (int i = 0; i < playground[verticalIndex].length; i++) {
            if(playground[verticalIndex][i] != first) {
                return Symbol.EMPTY;
            }
        }
        return first;
    }

    private Symbol checkHorizontalLine(int horizontalIndex, Symbol[][] playground) {
        Symbol first = playground[0][horizontalIndex];
        for (int i = 0; i < playground[0].length; i++) {
            if(playground[i][horizontalIndex] != first) {
                return Symbol.EMPTY;
            }
        }
        return first;
    }

    private Symbol checkDiagonalLine(Symbol[][] playground) {
        Symbol first = playground[0][0];
        for (int i = 0; i < playground[0].length; i++) {
            if(playground[i][i] != first) {
                break;
            }
            if(i == playground[0].length - 1) {
                return first;
            }
        }
        first = playground[0][playground.length - 1];
        for (int i = 0; i < playground[0].length; i++) {
            if(playground[i][playground.length - 1 - i] != first) {
                return Symbol.EMPTY;
            }
        }
        return first;
    }

    private boolean hasOneWin(Player_inner player, Symbol[][] playground) {
        for (int i = 0; i < playground[0].length; i++) {
            var result = checkVerticalLine(i, playground);
            if(player.getSymbol() == result) {
                return true;
            }
        }
        for (int i = 0; i < playground[0].length; i++) {
            var result = checkHorizontalLine(i, playground);
            if(player.getSymbol() == result) {
                return true;
            }
        }
        var result = checkDiagonalLine(playground);
        return player.getSymbol() == result;
    }

    @Override
    public TicTacToeAction[] getAllPossibleActions() {
        return enabledActions.toArray(new TicTacToeAction[0]);
    }

    @Override
    public int getTotalEntityCount() {
        return 2;
    }

    @Override
    public StateRewardReturn<TicTacToeAction, DoubleVector, TicTacToeState> applyAction(TicTacToeAction actionType) {
        if (isFinalState()) {
            throw new IllegalStateException("Cannot apply actions on final state");
        }

        if(isPlayerZeroOnTurn) {
            var x = actionType.getX();
            var y = actionType.getY();

            if(playground[x][y] != Symbol.EMPTY) {
                throw new IllegalArgumentException("Can't play on already filled index: [" + x + ", " + y + "]");
            }

            Symbol[][] newPlayground = new Symbol[this.playground.length][this.playground.length];

            for (int i = 0; i < newPlayground.length; i++) {
                for (int j = 0; j < newPlayground.length; j++) {
                    newPlayground[i][j] = this.playground[i][j];
                }
            }
            newPlayground[x][y] = Symbol.PLAYER_ZERO_SYMBOL;
            var newActions = enabledActions.stream().filter(item -> item.getX() != x || item.getY() != y).collect(Collectors.toList());
            double[] reward = hasOneWin(Player_inner.PLAYER_ZERO, newPlayground) ? PLAYER_ZERO_WON_REWARD : IN_GAME_REWARD;
            return new ImmutableStateRewardReturn<>(
                new TicTacToeState(
                    dimension,
                    newPlayground,
                    false,
                    turnsLeft - 1,
                    newActions
                ),
                reward
            );
        } else {
            var x = actionType.getX();
            var y = actionType.getY();
            if(playground[x][y] != Symbol.EMPTY) {
                throw new IllegalArgumentException("Can't play on already filled index: [" + x + ", " + y + "]");
            }
            Symbol[][] newPlayground = new Symbol[this.playground.length][this.playground.length];
            for (int i = 0; i < newPlayground.length; i++) {
                for (int j = 0; j < newPlayground.length; j++) {
                    newPlayground[i][j] = this.playground[i][j];
                }
            }
            newPlayground[x][y] = Symbol.PLAYER_ONE_SYMBOL;
            var newActions = enabledActions.stream().filter(item -> item.getX() != x || item.getY() != y).collect(Collectors.toList());
            double[] reward = hasOneWin(Player_inner.PLAYER_ONE, newPlayground) ? PLAYER_ONE_WON_REWARD : IN_GAME_REWARD;
            return new ImmutableStateRewardReturn<>(
                new TicTacToeState(
                    dimension,
                    newPlayground,
                    true,
                    turnsLeft - 1,
                    newActions
                ),
                reward
            );
        }
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        var observationVector = new double[dimension * dimension + 1];
        for (int i = 0; i < playground.length; i++) {
            for (int j = 0; j < playground[i].length; j++) {
                observationVector[i * playground[j].length + j] = playground[i][j].symbol;
            }
        }
//        observationVector[observationVector.length - 1] = isPlayerZeroOnTurn ? 0 : 1;
        return new DoubleVector(observationVector);
//        return new DoubleVector(Arrays.stream(playground).flatMap(Arrays::stream).mapToDouble(x -> x.symbol).toArray());
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        var observationVector = new double[dimension * dimension + 1];
        for (int i = 0; i < playground.length; i++) {
            for (int j = 0; j < playground[i].length; j++) {
                observationVector[i * playground[j].length + j] = playground[i][j].symbol;
            }
        }
//        observationVector[observationVector.length - 1] = isPlayerZeroOnTurn ? 0 : 1;
        return new DoubleVector(observationVector);
    }

    @Override
    public Predictor<TicTacToeState> getKnownModelWithPerfectObservationPredictor() {
        throw new UnsupportedOperationException("TicTacToe does not have fixed model");
    }

    @Override
    public String readableStringRepresentation() {
        StringBuilder builder = new StringBuilder((dimension + 2) * (dimension + 2));
        for (int i = 0; i < dimension + 2; i++) {
            for (int j = 0; j < dimension + 2; j++) {
                if(i == 0 || i == dimension + 1) {
                    builder.append("-");
                } else if(j == 0 || j == dimension + 1) {
                    builder.append("|");
                } else {
                    switch (playground[i - 1][j - 1]) {
                        case EMPTY:
                            builder.append(" ");
                            break;
                        case PLAYER_ZERO_SYMBOL:
                            builder.append("0");
                            break;
                        case PLAYER_ONE_SYMBOL:
                            builder.append("X");
                            break;
                        default: throw new IllegalStateException("wtf" + playground[i - 1][j - 1]);
                    }
                }
            }
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    @Override
    public List<String> getCsvHeader() {
        return Arrays.asList("Dummy");
    }

    @Override
    public List<String> getCsvRecord() {
        return Arrays.asList("Dummy");
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return isPlayerZeroOnTurn ? 0 : 1;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return false;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        return true;
    }

    @Override
    public boolean isFinalState() {
        return turnsLeft == 0 || hasOneWin(Player_inner.PLAYER_ZERO, playground) || hasOneWin(Player_inner.PLAYER_ONE, playground);
    }

    public TicTacToeResult getResult() {
        if(!isFinalState()) {
            throw new IllegalStateException("can't get result since state is not final");
        }
        return hasOneWin(Player_inner.PLAYER_ZERO, playground) ? TicTacToeResult.WIN_0 : hasOneWin(Player_inner.PLAYER_ONE, playground) ? TicTacToeResult.WIN_1 : TicTacToeResult.DRAW; // TODO: cache values
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TicTacToeState that = (TicTacToeState) o;

        if (dimension != that.dimension) return false;
        if (isPlayerZeroOnTurn != that.isPlayerZeroOnTurn) return false;
        if (turnsLeft != that.turnsLeft) return false;
        if (!Arrays.deepEquals(playground, that.playground)) return false;
        return enabledActions.equals(that.enabledActions);
    }

    @Override
    public int hashCode() {
        int result = dimension;
        result = 31 * result + Arrays.deepHashCode(playground);
        result = 31 * result + (isPlayerZeroOnTurn ? 1 : 0);
        result = 31 * result + turnsLeft;
        result = 31 * result + enabledActions.hashCode();
        return result;
    }
}
