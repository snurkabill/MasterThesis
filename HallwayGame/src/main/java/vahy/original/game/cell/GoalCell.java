package vahy.original.game.cell;

public class GoalCell extends CommonCell {

    private final double reward;

    public GoalCell(CellPosition cellPosition, double reward) {
        super(CellType.GOAL, cellPosition);
        this.reward = reward;
    }

    public double getReward() {
        return reward;
    }
}
