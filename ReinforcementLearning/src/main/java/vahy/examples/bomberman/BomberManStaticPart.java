package vahy.examples.bomberman;

import java.util.Arrays;
import java.util.EnumMap;

public class BomberManStaticPart {

    private static final int OBSERVATION_ONTURN_INDEX = 0;

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

    private final int totalStepsAllowed;

    private final int[] observation_playerIndexes;
    private final int[] observation_bombIndexes;
    private final int observation_goldInPlaceIndex;


    private final EnumMap<BomberManAction, BomberManAction[]> observedActionMap;


    public BomberManStaticPart(boolean[][] walls, int startingPlayerCount, int startingTotalEntityCount, int bombsPerPlayer, int rewardPerStep, int goldEntityCount, boolean[][] goldEntitiesArray, int[][] goldEntitiesReferenceArray, int rewardPerGold, double goldRespawnProbability, int bombRange, int bombCountDown, int playerLivesAtStart, int totalStepsAllowed) {
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
        this.totalStepsAllowed = totalStepsAllowed;
        if(startingPlayerCount + goldWithEnvironmentEntityCount != startingTotalEntityCount) {
            throw new IllegalStateException("Summary of entities in game does not match");
        }
        this.moveReward = new double[startingPlayerCount][];
        for (int i = 0; i < moveReward.length; i++) {
            moveReward[i] = new double[startingTotalEntityCount];
            moveReward[i][i + goldWithEnvironmentEntityCount] = -rewardPerStep;
        }
        this.noEnvironmentActionReward = new double[startingTotalEntityCount];

        this.observation_playerIndexes = new int[startingPlayerCount];
        for (int i = 0; i < startingPlayerCount; i++) {
            observation_playerIndexes[i] = 1 + 3 * i;
        }

        this.observation_bombIndexes = new int[startingPlayerCount * bombsPerPlayer];
        for (int i = 0; i < startingPlayerCount * bombsPerPlayer; i++) {
            observation_bombIndexes[i] = 1 + 3 * startingPlayerCount + 3 * i;
        }
        this.observation_goldInPlaceIndex = 1 + 3 * startingPlayerCount + 3 * startingPlayerCount * bombsPerPlayer;


        this.observedActionMap = new EnumMap<BomberManAction, BomberManAction[]>(BomberManAction.class);
        for (BomberManAction value : BomberManAction.values()) {
            var array = new BomberManAction[startingTotalEntityCount];
            Arrays.fill(array, value);
            observedActionMap.put(value, array);
        }
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

    public int getTotalStepsAllowed() {
        return totalStepsAllowed;
    }

    public int getObservation_onTurnIndex() {
        return OBSERVATION_ONTURN_INDEX;
    }

    public int[] getObservation_playerIndexes() {
        return observation_playerIndexes;
    }

    public int[] getObservation_bombIndexes() {
        return observation_bombIndexes;
    }

    public int getObservation_goldInPlaceIndex() {
        return observation_goldInPlaceIndex;
    }

    public BomberManAction[] getObservedActionArray(BomberManAction action) {
        return observedActionMap.get(action);
    }
}
