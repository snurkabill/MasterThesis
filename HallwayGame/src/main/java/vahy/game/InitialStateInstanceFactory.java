package vahy.game;

import vahy.environment.agent.AgentHeading;
import vahy.environment.config.IGameConfig;
import vahy.environment.state.ImmutableStateImpl;
import vahy.environment.state.StaticGamePart;
import vahy.game.cell.Cell;
import vahy.game.cell.CellPosition;
import vahy.game.cell.CellType;
import vahy.game.cell.CommonCell;
import vahy.game.cell.GoalCell;
import vahy.game.cell.TrapCell;
import vahy.utils.ArrayUtils;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class InitialStateInstanceFactory {

    private final IGameConfig gameConfig;
    private final SplittableRandom random;

    public InitialStateInstanceFactory(IGameConfig gameConfig, SplittableRandom random) {
        this.gameConfig = gameConfig;
        this.random = random;
    }

    public ImmutableStateImpl createInitialState(String gameStringRepresentation) throws NotValidGameStringRepresentationException {
//        return new HallwayGame(deserialize(gameStringRepresentation));
        return createImmutableInitialState(deserialize(gameStringRepresentation));
    }

    private void checkGameShape(List<List<Cell>> gameSetup) {
        if (!ArrayUtils.hasRectangleShape(gameSetup)) {
            throw new IllegalArgumentException("Game is not in rectangle-like shape.");
        }
    }

    private ImmutableTuple<Integer, Integer> generateInitialAgentCoordinates(List<List<Cell>> gameSetup) {
        List<Cell> startingLocations = gameSetup.stream()
            .flatMap(List::stream)
            .filter(cell -> cell.getCellType() == CellType.STARTING_LOCATION)
            .collect(Collectors.toList());
        Cell startingLocation = startingLocations.get(random.nextInt(startingLocations.size()));
        return new ImmutableTuple<>(startingLocation.getCellPosition().getX(), startingLocation.getCellPosition().getY());
    }

    private ImmutableStateImpl createImmutableInitialState(List<List<Cell>> gameSetup) {
        boolean[][] walls = new boolean[gameSetup.size()][gameSetup.get(0).size()];
        double[][] rewards = new double[gameSetup.size()][gameSetup.get(0).size()];
        double[][] trapProbabilities = new double[gameSetup.size()][gameSetup.get(0).size()];
        gameSetup.stream()
            .flatMap(List::stream)
            .forEach(cell -> {
                walls[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.WALL;
                rewards[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.GOAL ? gameConfig.getDefaultGoalReward() : 0.0;
                trapProbabilities[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.TRAP ? gameConfig.getDefaultTrapProbability() : 0.0;
            });
        StaticGamePart staticGamePart = new StaticGamePart(random, trapProbabilities, walls, gameConfig.getDefaultStepPenalty(), gameConfig.getDefaultNoisyMoveProbability());
        ImmutableTuple<Integer, Integer> agentStartingPosition = generateInitialAgentCoordinates(gameSetup);
        return new ImmutableStateImpl(staticGamePart, rewards, agentStartingPosition.getFirst(), agentStartingPosition.getSecond(), AgentHeading.NORTH);
    }

    private List<List<Cell>> deserialize(String stringRepresentation) throws NotValidGameStringRepresentationException {
        String[] lines = stringRepresentation.split("\\n");
        List<List<Cell>> list = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String[] cells = lines[i].split(" ");
            List<Cell> innerList = new ArrayList<>();

            for (int j = 0; j < cells.length; j++) {
                innerList.add(createCell(cells[j], i, j));
            }
            list.add(innerList);
        }
        checkGameShape(list);
        return list;
    }

    private int parseIntWithException(String cellRepresentation) throws NotValidGameStringRepresentationException {
        try {
            return Integer.parseInt(cellRepresentation);
        } catch (NumberFormatException e) {
            throw new NotValidGameStringRepresentationException("Unable to parse [" + cellRepresentation + "]", e);
        }
    }

    private Cell createCell(String cellRepresentation, int xIndex, int yIndex) throws NotValidGameStringRepresentationException {
        if (cellRepresentation.equals("1")) {
            return new CommonCell(CellType.WALL, new CellPosition(xIndex, yIndex));
        }
        if (cellRepresentation.equals("0")) {
            return new CommonCell(CellType.EMPTY, new CellPosition(xIndex, yIndex));
        }
        if (cellRepresentation.equals("+")) {
            return new CommonCell(CellType.STARTING_LOCATION, new CellPosition(xIndex, yIndex));
        }
        if (cellRepresentation.equals("x")) {
            return new TrapCell(new CellPosition(xIndex, yIndex), gameConfig.getDefaultTrapProbability());
        }
        if (cellRepresentation.equals("g")) {
            return new GoalCell(new CellPosition(xIndex, yIndex), gameConfig.getDefaultGoalReward());
        }
        int reward = parseIntWithException(cellRepresentation);
        if (reward > 1) {
            return new GoalCell(new CellPosition(xIndex, yIndex), reward);
        } else {
            throw new NotValidGameStringRepresentationException("Reward [" + reward + "] is not valid reward");
        }
    }
}
