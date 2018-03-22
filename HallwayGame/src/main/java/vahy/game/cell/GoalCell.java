package vahy.game.cell;

public class GoalCell extends CommonCell {

    private final int reward;

    public GoalCell(CellPosition cellPosition, int reward) {
        super(CellType.GOAL, cellPosition);
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }
}
