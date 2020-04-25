package vahy.original.environment.config;

import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.original.environment.state.StateRepresentation;
import vahy.original.game.HallwayInstance;
import vahy.original.game.cell.Cell;
import vahy.original.game.cell.CellPosition;
import vahy.original.game.cell.CellType;
import vahy.original.game.cell.CommonCell;
import vahy.original.game.cell.GoalCell;
import vahy.original.game.cell.TrapCell;
import vahy.utils.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConfigBuilder {

    private int maximalStepCountBound;
    private boolean isModelKnown = true;
    private double goalReward;
    private double stepPenalty;
    private double trapProbability;
    private double noisyMoveProbability;
    private StateRepresentation stateRepresentation;
    private HallwayInstance hallwayGameInstance;

    public ConfigBuilder reward(double goalReward) {
        this.goalReward = goalReward;
        return this;
    }

    public ConfigBuilder stepPenalty(double stepPenalty) {
        this.stepPenalty = stepPenalty;
        return this;
    }

    public ConfigBuilder trapProbability(double trapProbability) {
        this.trapProbability = trapProbability;
        return this;
    }

    public ConfigBuilder noisyMoveProbability(double noisyMoveProbability) {
        this.noisyMoveProbability = noisyMoveProbability;
        return this;
    }

    public ConfigBuilder stateRepresentation(StateRepresentation stateRepresentation) {
        this.stateRepresentation = stateRepresentation;
        return this;
    }

    public ConfigBuilder gameStringRepresentation(HallwayInstance hallwayInstance) {
        this.hallwayGameInstance = hallwayInstance;
        return this;
    }

    public ConfigBuilder maximalStepCountBound(int maximalStepCountBound) {
        this.maximalStepCountBound = maximalStepCountBound; return this;
    }

    public ConfigBuilder isModelKnown(boolean isModelKnown) {
        this.isModelKnown = isModelKnown;
        return this;
    }

    public GameConfig buildConfig() {

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(hallwayGameInstance.getPath());
        try {
            var bytes = resourceAsStream.readAllBytes();
            var representation = new String(bytes);
            return new GameConfig(maximalStepCountBound, isModelKnown, goalReward, stepPenalty, trapProbability, noisyMoveProbability, stateRepresentation, representation, deserialize(representation));
        } catch (IOException | InvalidInstanceSetupException e) {
            throw new RuntimeException(e);
        }
    }

    private List<List<Cell>> deserialize(String representation) throws InvalidInstanceSetupException {
        String[] lines = representation.replace("\r\n", "\n").replace("\r", "\n").split("\\n");
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

    private void checkGameShape(List<List<Cell>> gameSetup) {
        if (!ArrayUtils.hasRectangleShape(gameSetup)) {
            throw new IllegalArgumentException("Game is not in rectangle-like shape.");
        }
    }

    private int parseIntWithException(String cellRepresentation) throws InvalidInstanceSetupException {
        try {
            return Integer.parseInt(cellRepresentation);
        } catch (NumberFormatException e) {
            throw new InvalidInstanceSetupException("Unable to parse [" + cellRepresentation + "]", e);
        }
    }

    private Cell createCell(String cellRepresentation, int xIndex, int yIndex) throws InvalidInstanceSetupException {
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
            return new TrapCell(new CellPosition(xIndex, yIndex), trapProbability);
        }
        if (cellRepresentation.equals("g")) {
            return new GoalCell(new CellPosition(xIndex, yIndex), goalReward);
        }
        int reward = parseIntWithException(cellRepresentation);
        if (reward > 1) {
            return new GoalCell(new CellPosition(xIndex, yIndex), reward);
        } else {
            throw new InvalidInstanceSetupException("Reward [" + reward + "] is not valid reward");
        }
    }

}
