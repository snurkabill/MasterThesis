package vahy.impl.testdomain.tictactoe;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TicTacToeState implements State<TicTacToeAction, DoubleVector, TicTacToeState, TicTacToeState>, Observation {

    private enum Player_inner {
        PLAYER(Symbol.PLAYER),
        OPPONENT(Symbol.OPPONENT);

        private final Symbol symbol;

        Player_inner(Symbol symbol) {
            this.symbol = symbol;
        }

        public Symbol getSymbol() {
            return symbol;
        }
    }

    public enum Symbol {
        PLAYER(1),
        OPPONENT(2),
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
    private final boolean isAgentTurn;
    private final int turnsLeft;

    private final List<TicTacToeAction> enabledActions;

    public TicTacToeState(Symbol[][] playground, boolean isAgentTurn, int turnsLeft, List<TicTacToeAction> enabledActions) {
        for (int i = 0; i < playground.length; i++) {
            if(playground[i].length != playground.length) {
                throw new IllegalArgumentException("Playground is not square-like");
            }
        }
        this.playground = playground;
        this.isAgentTurn = isAgentTurn;
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
    public TicTacToeAction[] getPossiblePlayerActions() {
        return getAllPossibleActions();
    }

    @Override
    public TicTacToeAction[] getPossibleOpponentActions() {
        return getAllPossibleActions();
    }

    @Override
    public StateRewardReturn<TicTacToeAction, DoubleVector, TicTacToeState, TicTacToeState> applyAction(TicTacToeAction actionType) {
        if (isFinalState()) {
            throw new IllegalStateException("Cannot apply actions on final state");
        }

        if(isAgentTurn) {
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
            newPlayground[x][y] = Symbol.PLAYER;
            var newActions = enabledActions.stream().filter(item -> item.getX() != x || item.getY() != y).collect(Collectors.toList());
            double reward = hasOneWin(Player_inner.PLAYER, newPlayground) ? 1 : 0;
            return new ImmutableStateRewardReturnTuple<>(
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
            newPlayground[x][y] = Symbol.OPPONENT;
            var newActions = enabledActions.stream().filter(item -> item.getX() != x || item.getY() != y).collect(Collectors.toList());
            double reward = hasOneWin(Player_inner.OPPONENT, newPlayground) ? -1 : 0;
            return new ImmutableStateRewardReturnTuple<>(
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
    public DoubleVector getPlayerObservation() {
        return new DoubleVector(Arrays.stream(playground).flatMap(Arrays::stream).mapToDouble(x -> x.symbol).toArray());
    }

    @Override
    public TicTacToeState getOpponentObservation() {
        return this;
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
    public boolean isOpponentTurn() {
        return !isAgentTurn;
    }

    @Override
    public boolean isFinalState() {
        return turnsLeft == 0 || hasOneWin(Player_inner.PLAYER, playground) || hasOneWin(Player_inner.OPPONENT, playground);
    }
}
