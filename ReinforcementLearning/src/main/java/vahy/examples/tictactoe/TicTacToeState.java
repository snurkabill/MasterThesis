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

    private final Symbol[][] playground;
    private final boolean isPlayerZeroOnTurn;
    private final int turnsLeft;

    private final List<TicTacToeAction> enabledActions;

    public TicTacToeState(Symbol[][] playground, boolean isPlayerZeroOnTurn, int turnsLeft, List<TicTacToeAction> enabledActions) {
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
    public DoubleVector getPlayerObservation(int playerId) {
        return new DoubleVector(Arrays.stream(playground).flatMap(Arrays::stream).mapToDouble(x -> x.symbol).toArray());
    }

    @Override
    public DoubleVector getCommonObservation(int playerId) {
        return new DoubleVector(Arrays.stream(playground).flatMap(Arrays::stream).mapToDouble(x -> x.symbol).toArray());
    }

    @Override
    public Predictor<TicTacToeState> getKnownModelWithPerfectObservationPredictor() {
        throw new UnsupportedOperationException("TicTacToe does not have fixed model");
    }

    @Override
    public String readableStringRepresentation() {
        return "DummySoFar";
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
    public TicTacToeAction[] getAllEnvironmentActions() {
        return new TicTacToeAction[0];
    }

    @Override
    public TicTacToeAction[] getAllPlayerActions() {
        return TicTacToeAction.values();
    }

    @Override
    public int getTotalPlayerCount() {
        return 2;
    }

    @Override
    public int getPlayerIdOnTurn() {
        return isPlayerZeroOnTurn ? 0 : 1;
    }

    @Override
    public boolean isInGame(int playerId) {
        return true;
    }

    @Override
    public boolean isFinalState() {
        return turnsLeft == 0 || hasOneWin(Player_inner.PLAYER_ZERO, playground) || hasOneWin(Player_inner.PLAYER_ONE, playground);
    }
}
