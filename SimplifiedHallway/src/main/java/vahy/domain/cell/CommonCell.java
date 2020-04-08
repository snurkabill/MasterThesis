package vahy.domain.cell;

public class CommonCell implements Cell {

    private final CellType cellType;
    private final CellPosition cellPosition;

    public CommonCell(CellType cellType, CellPosition cellPosition) {
        this.cellType = cellType;
        this.cellPosition = cellPosition;
    }

    @Override
    public CellType getCellType() {
        return cellType;
    }

    @Override
    public CellPosition getCellPosition() {
        return cellPosition;
    }
}
