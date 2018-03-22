package vahy.game.cell;

public class TrapCell extends CommonCell {

    private final double trapProbability;

    public TrapCell(CellPosition cellPosition, double trapProbability) {
        super(CellType.TRAP, cellPosition);
        this.trapProbability = trapProbability;
    }

    public double getTrapProbability() {
        return trapProbability;
    }
}
