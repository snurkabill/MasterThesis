package vahy.examples.bomberman;

public class BomberManStaticPart {

    private final boolean[][] walls;

    private final boolean[][] goldEntitiesArray;
    private final int[][] goldEntitiesReferenceArray;
    private final int rewardPerGold;

    private final int goldEntityCount;
    private final int startingPlayerCount;
    private final int startingTotalEntityCount;
    private final double goldRespawnProbability;

    private final int bombsPerPlayer;
    private final int bombRange;
    private final int bombCountDown;

    private final int playerLivesAtStart;

    private final double[][] moveReward;
    private final double[] noEnvironmentActionReward;

    private final int goldWithEnvironmentEntityCount;

    public BomberManStaticPart(boolean[][] walls, int startingPlayerCount, int startingTotalEntityCount, int bombsPerPlayer, int rewardPerStep, int goldEntityCount, boolean[][] goldEntitiesArray, int[][] goldEntitiesReferenceArray, int rewardPerGold, double goldRespawnProbability, int bombRange, int bombCountDown, int playerLivesAtStart) {
        this.walls = walls;
        this.goldEntitiesArray = goldEntitiesArray;
        this.goldEntitiesReferenceArray = goldEntitiesReferenceArray;
        this.rewardPerGold = rewardPerGold;
        this.goldRespawnProbability = goldRespawnProbability;
        this.startingPlayerCount = startingPlayerCount;
        this.startingTotalEntityCount = startingTotalEntityCount;
        this.bombsPerPlayer = bombsPerPlayer;
        this.goldEntityCount = goldEntityCount;
        this.bombRange = bombRange;
        this.bombCountDown = bombCountDown;
        this.playerLivesAtStart = playerLivesAtStart;
        this.goldWithEnvironmentEntityCount = 1 + goldEntityCount;
        if(startingPlayerCount + goldWithEnvironmentEntityCount != startingTotalEntityCount) {
            throw new IllegalStateException("Summary of entities in game does not match");
        }
        this.moveReward = new double[startingPlayerCount][];
        for (int i = 0; i < moveReward.length; i++) {
            moveReward[i] = new double[startingTotalEntityCount];
            moveReward[i][i + goldWithEnvironmentEntityCount] = -rewardPerStep;
        }
        this.noEnvironmentActionReward = new double[startingTotalEntityCount];
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

    public int getStartingTotalEntityCount() {
        return startingTotalEntityCount;
    }

    public int getBombsPerPlayer() {
        return bombsPerPlayer;
    }

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

    public int getGoldEntityCount() {
        return goldEntityCount;
    }

    public boolean[][] getGoldEntitiesArray() {
        return goldEntitiesArray;
    }

    public int[][] getGoldEntitiesReferenceArray() {
        return goldEntitiesReferenceArray;
    }

    public int getRewardPerGold() {
        return rewardPerGold;
    }

    public double getGoldRespawnProbability() {
        return goldRespawnProbability;
    }

    public int getGoldWithEnvironmentEntityCount() {
        return goldWithEnvironmentEntityCount;
    }
}
