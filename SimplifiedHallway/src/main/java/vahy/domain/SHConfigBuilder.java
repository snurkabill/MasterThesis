package vahy.domain;

import vahy.domain.cell.Cell;
import vahy.domain.cell.CellPosition;
import vahy.domain.cell.CellType;
import vahy.domain.cell.CommonCell;
import vahy.domain.cell.GoalCell;
import vahy.domain.cell.TrapCell;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.utils.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SHConfigBuilder {

    private int maximalStepCountBound;
    private boolean isModelKnown = true;
    private double goalReward;
    private double stepPenalty;
    private double trapProbability;
    private SHInstance hallwayGameInstance;

    public SHConfigBuilder reward(double goalReward) {
        this.goalReward = goalReward;
        return this;
    }

    public SHConfigBuilder stepPenalty(double stepPenalty) {
        this.stepPenalty = stepPenalty;
        return this;
    }

    public SHConfigBuilder trapProbability(double trapProbability) {
        this.trapProbability = trapProbability;
        return this;
    }

    public SHConfigBuilder gameStringRepresentation(SHInstance hallwayInstance) {
        this.hallwayGameInstance = hallwayInstance;
        return this;
    }

    public SHConfigBuilder maximalStepCountBound(int maximalStepCountBound) {
        this.maximalStepCountBound = maximalStepCountBound; return this;
    }

    public SHConfigBuilder isModelKnown(boolean isModelKnown) {
        this.isModelKnown = isModelKnown;
        return this;
    }

    public SHConfig buildConfig() {

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(hallwayGameInstance.getPath());
        try {
            var bytes = resourceAsStream.readAllBytes();
            var representation = new String(bytes);
            return new SHConfig(maximalStepCountBound, isModelKnown, goalReward, stepPenalty, trapProbability, representation, deserialize(representation));
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
