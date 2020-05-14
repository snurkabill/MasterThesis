package vahy.examples.bomberman;

public class BomberManStaticPart {

    private final boolean[][] walls;
    private final int startingPlayerCount;
    private final int startingEntityCount;
    private final int bombsPerPlayer;
//    private final int rewardsPerPlayer;
    private final int bombRange;
    private final int bombCountDown;
    private final int playerLivesAtStart;

    private final double[][] moveReward;
    private final double[] noEnvironmentActionReward;

    public BomberManStaticPart(boolean[][] walls, int startingPlayerCount, int startingEntityCount, int bombsPerPlayer, int rewardPerStep, int bombRange, int bombCountDown, int playerLivesAtStart) {
        this.walls = walls;
        this.startingPlayerCount = startingPlayerCount;
        this.startingEntityCount = startingEntityCount;
        this.bombsPerPlayer = bombsPerPlayer;
//        this.rewardsPerPlayer = rewardsPerPlayer;
        this.bombRange = bombRange;
        this.moveReward = new double[startingPlayerCount][];
        this.bombCountDown = bombCountDown;
        this.playerLivesAtStart = playerLivesAtStart;
        for (int i = 0; i < moveReward.length; i++) {
            moveReward[i] = new double[startingEntityCount];
            moveReward[i][i + 1] = -rewardPerStep;
        }
        this.noEnvironmentActionReward = new double[startingPlayerCount];
    }

    public double[][] getMoveReward() {
        return moveReward;
    }

    public boolean[][] getWalls() {
        return walls;
    }

    public int getStartingPlayerCount() {
        return startingPlayerCount;
    }

    public int getStartingEntityCount() {
        return startingEntityCount;
    }

    public int getBombsPerPlayer() {
        return bombsPerPlayer;
    }

//    public int getRewardsPerPlayer() {
//        return rewardsPerPlayer;
//    }

    public int getBombRange() {
        return bombRange;
    }

    public int getBombCountDown() {
        return bombCountDown;
    }

    public double[] getNoEnvironmentActionReward() {
        return noEnvironmentActionReward;
    }

    public int getPlayerLivesAtStart() {
        return playerLivesAtStart;
    }
}
