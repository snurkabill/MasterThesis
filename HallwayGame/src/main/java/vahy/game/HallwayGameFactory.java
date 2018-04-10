package vahy.game;

import vahy.environment.config.GameConfig;
import vahy.game.cell.Cell;
import vahy.game.cell.CellPosition;
import vahy.game.cell.CellType;
import vahy.game.cell.CommonCell;
import vahy.game.cell.GoalCell;
import vahy.game.cell.TrapCell;

import java.util.ArrayList;
import java.util.List;

public class HallwayGameFactory {

    private GameConfig gameConfig;

    public HallwayGameFactory(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public HallwayGame createGame(String gameStringRepresentation) throws NotValidGameStringRepresentationException {
        return new HallwayGame(deserialize(gameStringRepresentation));
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
        return list;
    }

    private int parseIntWithException(String cellRepresentation) throws NotValidGameStringRepresentationException {
        try {
            return Integer.parseInt(cellRepresentation);
        } catch(NumberFormatException e) {
            throw new NotValidGameStringRepresentationException("Unable to parse [" + cellRepresentation +"]", e);
        }
    }

    private Cell createCell(String cellRepresentation, int xIndex, int yIndex) throws NotValidGameStringRepresentationException {
        if(cellRepresentation.equals("1")) {
            return new CommonCell(CellType.WALL, new CellPosition(xIndex, yIndex));
        }
        if(cellRepresentation.equals("0")) {
            return new CommonCell(CellType.EMPTY, new CellPosition(xIndex, yIndex));
        }
        if(cellRepresentation.equals("+")) {
            return new CommonCell(CellType.STARTING_LOCATION, new CellPosition(xIndex, yIndex));
        }
        if(cellRepresentation.equals("x")) {
            return new TrapCell(new CellPosition(xIndex, yIndex), gameConfig.getTrapProbability());
        }
        if(cellRepresentation.equals("g")) {
            return new GoalCell(new CellPosition(xIndex, yIndex), gameConfig.getGoalReward());
        }
        int reward = parseIntWithException(cellRepresentation);
        if(reward > 1) {
            return new GoalCell(new CellPosition(xIndex, yIndex), reward);
        } else {
            throw new NotValidGameStringRepresentationException("Reward [" + reward + "] is not valid reward");
        }
    }
}
