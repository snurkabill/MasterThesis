package vahy.examples.bomberman;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BomberManState implements State<BomberManAction, DoubleVector, BomberManState> {

    public static int ENVIRONMENT_ENTITY_ID = 0;
    public static int ENTITY_ON_TURN_INDEX = 0;

    public static BomberManAction[] ENVIRONMENT_ACTION_ARRAY_NO_BOMB = new BomberManAction[] {BomberManAction.NO_ACTION};
    public static BomberManAction[] ENVIRONMENT_ACTION_ARRAY_DETONATE = new BomberManAction[] {BomberManAction.DETONATE_BOMB};
    public static BomberManAction[] REWARD_ACTION_ARRAY = new BomberManAction[] {BomberManAction.NO_ACTION_REWARD, BomberManAction.RESPAWN_REWARD};
    public static BomberManAction[] PLAYER_ACTION_ARRAY = new BomberManAction[] {BomberManAction.UP, BomberManAction.DOWN, BomberManAction.LEFT, BomberManAction.RIGHT, BomberManAction.DROP_BOMB};

    private static double getXPortion(BomberManStaticPart staticPart, int x) {
        int xTotal = staticPart.getWalls().length - 3;
        int xAgentFixed = x - 1;
        return xTotal == 0 ? 0.0 : ((xAgentFixed / (double) xTotal) - 0.5);
    }

    private static double getYPortion(BomberManStaticPart staticPart, int y) {
        int yTotal = staticPart.getWalls()[0].length - 3;
        int yAgentFixed = y - 1;
        return yTotal == 0 ? 0.0 : ((yAgentFixed / (double) yTotal) - 0.5);
    }

    private static int calculatePlayerId(int entityId, int totalGoldCount, int totalPlayerCount) {
        if(entityId == 0) {
            return -1;
        }
        if(entityId - 1 < totalGoldCount) {
            return -1;
        } else {
            return entityId - 1 - totalGoldCount;
        }
    }

    private static int calculateGoldId(int entityId, int totalGoldCount, int totalPlayerCount) {
        if(entityId == 0) {
            return -1;
        }
        if(entityId - 1 < totalGoldCount) {
            return entityId - 1;
        } else {
            return -1;
        }
    }

    private final BomberManStaticPart staticPart;
    private final int entityInGameCount;

    private final int entityIdOnTurn;
    private final int playerIdOnTurn;
    private final int goldIdOnTurn;

    private final boolean[] goldsInPlaceArray;

    private final DoubleVector observation;

    private final boolean[] isInGameArray;

    private final int[] playerLivesCount;
    private final int[] playerXCoordinates;

    private final int[] playerYCoordinates;
    private final int[] bombXCoordinates;
    private final int[] bombYCoordinates;
    private final int[] bombCountDowns;

    private final int droppedBombs;

    private final int stepsDone;

    public BomberManState(BomberManStaticPart staticPart,
                          boolean[] isInGameArray,
                          boolean[] goldsInPlaceArray,
                          int entityInGameCount,
                          int entityIdOnTurn,
                          int playerIdOnTurn,
                          int goldIdOnTurn,
                          int[] playerXCoordinates,
                          int[] playerYCoordinates,
                          int[] playerLivesCount,
                          int[] bombXCoordinates,
                          int[] bombYCoordinates,
                          int[] bombCountDowns,
                          int droppedBombs,
                          int stepsDone) {
        this.staticPart = staticPart;
        this.isInGameArray = isInGameArray;
        this.goldsInPlaceArray = goldsInPlaceArray;
        this.entityInGameCount = entityInGameCount;
        this.entityIdOnTurn = entityIdOnTurn;
        this.playerIdOnTurn = playerIdOnTurn;
        this.goldIdOnTurn = goldIdOnTurn;
        this.playerXCoordinates = playerXCoordinates;
        this.playerYCoordinates = playerYCoordinates;
        this.playerLivesCount = playerLivesCount;
        this.bombXCoordinates = bombXCoordinates;
        this.bombYCoordinates = bombYCoordinates;
        this.bombCountDowns = bombCountDowns;
        this.droppedBombs = droppedBombs;
        this.stepsDone = stepsDone;
        this.observation = createObservation();
    }

    public BomberManState(BomberManStaticPart staticPart,
                          int[] playerXCoordinates,
                          int[] playerYCoordinates,
                          int entityIdOnTurn) {
        var playerCount = staticPart.getStartingPlayerCount();
        goldIdOnTurn = calculateGoldId(entityIdOnTurn, staticPart.getGoldEntityCount(), playerCount);
        playerIdOnTurn = calculatePlayerId(entityIdOnTurn, staticPart.getGoldEntityCount(), playerCount);
        if (entityIdOnTurn == 0) {
            throw new IllegalStateException("Environment can't start");
        }
        if(goldIdOnTurn != -1) {
            throw new IllegalStateException("Gold entity can't start");
        }
        this.staticPart = staticPart;
        this.stepsDone = 0;
        var booleanArray = new boolean[playerCount];
        Arrays.fill(booleanArray, true);
        this.isInGameArray = booleanArray;
        this.entityInGameCount = staticPart.getStartingTotalEntityCount();
        this.entityIdOnTurn = entityIdOnTurn;
        this.playerXCoordinates = playerXCoordinates;
        this.playerYCoordinates = playerYCoordinates;
        var livesArray = new int[playerCount];
        Arrays.fill(livesArray, staticPart.getPlayerLivesAtStart());
        this.playerLivesCount = livesArray;

        this.droppedBombs = 0;
        var intArray = new int[playerCount * staticPart.getBombsPerPlayer()];
        Arrays.fill(intArray, -1);
        this.bombXCoordinates = intArray;
        var intArray2 = new int[playerCount * staticPart.getBombsPerPlayer()];
        Arrays.fill(intArray2, -1);
        this.bombYCoordinates = intArray2;
        var intArray3 = new int[playerCount * staticPart.getBombsPerPlayer()];
        Arrays.fill(intArray3, -1);
        this.bombCountDowns = intArray3;

        var goldArray = new boolean[staticPart.getGoldEntityCount()];
        Arrays.fill(goldArray, true);
        this.goldsInPlaceArray = goldArray;
        this.observation = createObservation();
    }

    private DoubleVector createObservation() {
        var maxPlayerCount = this.staticPart.getStartingPlayerCount();
        var maxBombCount = maxPlayerCount * this.staticPart.getBombsPerPlayer();
        var playerInfo = maxPlayerCount * 3; //X, Y, Lives
        var bombInfo = maxBombCount * 3; // X, Y, Countdonw
        var newObservationArray = new double[1 + playerInfo + bombInfo + goldsInPlaceArray.length]; // one for entity on turn

        newObservationArray[ENTITY_ON_TURN_INDEX] = entityIdOnTurn;
        int index = 1;

        for (int i = 0; i < maxPlayerCount; i++) {
            newObservationArray[index + i] = this.playerXCoordinates[i];
        }
        index += maxPlayerCount;
        for (int i = 0; i < maxPlayerCount; i++) {
            newObservationArray[index + i] = this.playerYCoordinates[i];
        }
        index += maxPlayerCount;
        for (int i = 0; i < maxPlayerCount; i++) {
            newObservationArray[index + i] = this.playerLivesCount[i];
        }
        index += maxPlayerCount;

        for (int i = 0; i < maxBombCount; i++) {
            newObservationArray[index + i] = this.bombXCoordinates[i];
        }
        index += maxBombCount;
        for (int i = 0; i < maxBombCount; i++) {
            newObservationArray[index + i] = this.bombYCoordinates[i];
        }
        index += maxBombCount;
        for (int i = 0; i < maxBombCount; i++) {
            newObservationArray[index + i] = this.bombCountDowns[i];
        }
        index += maxBombCount;


        for (int i = 0; i < goldsInPlaceArray.length; i++) {
            newObservationArray[index] = goldsInPlaceArray[i] ? 1.0 : 0.0;
            index++;
        }


//        for (int i = 0; i < maxPlayerCount; i++) {
//            newObservationArray[index] = this.playerXCoordinates[i]; // getXPortion(this.staticPart, this.playerXCoordinates[i]);
//            newObservationArray[index + 1] = this.playerYCoordinates[i]; // getYPortion(this.staticPart, this.playerYCoordinates[i]);
//            newObservationArray[index + 2] = this.playerLivesCount[i];
//            index += 3;
//        }
//
//        for (int i = 0; i < maxBombCount; i++) {
//            newObservationArray[index] = this.bombXCoordinates[i]; // getXPortion(this.staticPart, this.bombXCoordinates[i]);
//            newObservationArray[index + 1] = this.bombYCoordinates[i]; //getYPortion(this.staticPart, this.bombYCoordinates[i]);
//            newObservationArray[index + 2] = this.bombCountDowns[i];
//            index += 3;
//        }
//
//        for (int i = 0; i < goldsInPlaceArray.length; i++) {
//            newObservationArray[index] = goldsInPlaceArray[i] ? 1.0 : 0.0;
//            index++;
//        }
        return new DoubleVector(newObservationArray);
    }

//    private DoubleVector createObservation() {
//        var maxPlayerCount = this.staticPart.getStartingPlayerCount();
//        var maxBombCount = maxPlayerCount * this.staticPart.getBombsPerPlayer();
//        var playerInfo = maxPlayerCount * 3; //X, Y, Lives
//        var bombInfo = maxBombCount * 3; // X, Y, Countdonw
//        var newObservationArray = new double[1 + playerInfo + bombInfo + goldsInPlaceArray.length]; // one for entity on turn
//
//        newObservationArray[ENTITY_ON_TURN_INDEX] = entityIdOnTurn;
//        int index = 1;
//        for (int i = 0; i < maxPlayerCount; i++) {
//            newObservationArray[index] = getXPortion(this.staticPart, this.playerXCoordinates[i]);
//            newObservationArray[index + 1] = getYPortion(this.staticPart, this.playerYCoordinates[i]);
//            newObservationArray[index + 2] = this.playerLivesCount[i];
//            index += 3;
//        }
//
//        for (int i = 0; i < bombInfo; i++) {
//            newObservationArray[index] = getXPortion(this.staticPart, this.bombXCoordinates[i]);
//            newObservationArray[index + 1] = getYPortion(this.staticPart, this.bombYCoordinates[i]);
//            newObservationArray[index + 2] = this.bombCountDowns[i];
//            index += 3;
//        }
//
//        for (int i = 0; i < goldsInPlaceArray.length; i++) {
//            newObservationArray[index] = 1.0;
//            index++;
//        }
//
//        return new DoubleVector(newObservationArray);
//    }

    @Override
    public BomberManAction[] getAllPossibleActions() {
        if (entityIdOnTurn == ENVIRONMENT_ENTITY_ID) {
            var anyBombCountDownToZero = false;
            for (int i = 0; i < bombCountDowns.length; i++) {
                if(bombCountDowns[i] == 0) {
                    anyBombCountDownToZero = true;
                    break;
                }
            }
            if(anyBombCountDownToZero) {
                return ENVIRONMENT_ACTION_ARRAY_DETONATE;
            } else {
                return ENVIRONMENT_ACTION_ARRAY_NO_BOMB;
            }
        } else if(goldIdOnTurn != -1) {
            return REWARD_ACTION_ARRAY;
        } else {
            return PLAYER_ACTION_ARRAY;
        }
    }

    private ImmutableTuple<Integer, Integer> tryMakeMove(BomberManAction action, int x, int y) {
        switch (action) {
            case UP:
                return new ImmutableTuple<>(x, y + 1);
            case DOWN:
                return new ImmutableTuple<>(x, y - 1);
            case RIGHT:
                return new ImmutableTuple<>(x - 1, y);
            case LEFT:
                return new ImmutableTuple<>(x + 1, y);
            default:
                throw EnumUtils.createExceptionForNotExpectedEnumValue(action);
        }
    }

    private boolean isMoveDoable(int x, int y) {
        return !staticPart.getWalls()[x][y];
    }

    @Override
    public int getTotalEntityCount() {
        return staticPart.getStartingTotalEntityCount();
    }

    private int nextEntityIdOnTurn() {
        var index = entityIdOnTurn;
        do {
            index++;
            if(index >= staticPart.getStartingTotalEntityCount()) {
                index = 0;
            }
            if(index < staticPart.getGoldWithEnvironmentEntityCount()) {
                return index;
            }
        } while (!isInGameArray[index - staticPart.getGoldWithEnvironmentEntityCount()]);
        if (index == entityIdOnTurn) {
            throw new IllegalStateException("There is only one entity left. Should not happen. ");
        }
        return index;
    }

    private int getFreeBombSlotIndex(int playerId) {
        var minBombIndex = playerId * staticPart.getBombsPerPlayer();

        for (int i = 0; i < staticPart.getBombsPerPlayer(); i++) {
            if (bombYCoordinates[minBombIndex + i] == -1) {
                return minBombIndex + i;
            }
        }
        return -1;
    }

    private void addBombSquare(int x, int y, Map<Integer, Set<Integer>> map) {
        if (!map.containsKey(x)) {
            var set = new HashSet<Integer>();
            set.add(y);
            map.put(x, set);
        } else {
            var set = map.get(x);
            set.add(y);
        }
    }

    private void addBombRange(int x, int y, Map<Integer, Set<Integer>> map) {

        for (int i = 0; i < staticPart.getBombRange(); i++) {
            var xCoord = x + i;
            if (staticPart.getWalls()[xCoord][y]) {
                break;
            }
            if (xCoord > 0 && xCoord < staticPart.getWalls().length) {
                addBombSquare(xCoord, y, map);
            }
        }
        for (int i = 0; i < staticPart.getBombRange(); i++) {
            var xCoord = x - i;
            if (staticPart.getWalls()[xCoord][y]) {
                break;
            }
            if (xCoord > 0 && xCoord < staticPart.getWalls().length) {
                addBombSquare(xCoord, y, map);
            }
        }

        for (int i = 0; i < staticPart.getBombRange(); i++) {
            var yCoord = y + i;
            if (staticPart.getWalls()[x][yCoord]) {
                break;
            }
            if (yCoord > 0 && yCoord < staticPart.getWalls().length) {
                addBombSquare(x, yCoord, map);
            }
        }

        for (int i = 0; i < staticPart.getBombRange(); i++) {
            var yCoord = y - i;
            if (staticPart.getWalls()[x][yCoord]) {
                break;
            }
            if (yCoord > 0 && yCoord < staticPart.getWalls().length) {
                addBombSquare(x, yCoord, map);
            }
        }
    }

    private StateRewardReturn<BomberManAction, DoubleVector, BomberManState> playEnvironmentAction(BomberManAction actionType, int newIdOnTurn, int newPlayerIdOnTurn, int newGoldIdOnTurn) {
        if (entityIdOnTurn != ENVIRONMENT_ENTITY_ID) {
            throw new IllegalStateException("Inconsistency. Check");
        }

        var copyC = Arrays.copyOf(bombCountDowns, bombCountDowns.length);
        for (int i = 0; i < copyC.length; i++) {
            if (copyC[i] > 0) {
                copyC[i]--;
            } else if (copyC[i] == 0 && actionType != BomberManAction.DETONATE_BOMB) {
                throw new IllegalStateException("Bomb should have detonate");
            }
        }

        if (actionType == BomberManAction.NO_ACTION) {
            return new ImmutableStateRewardReturn<>(
                new BomberManState(
                    staticPart,
                    isInGameArray,
                    goldsInPlaceArray,
                    entityInGameCount,
                    newIdOnTurn,
                    newPlayerIdOnTurn,
                    newGoldIdOnTurn,
                    playerXCoordinates,
                    playerYCoordinates,
                    playerLivesCount,
                    bombXCoordinates,
                    bombYCoordinates,
                    copyC,
                    droppedBombs,
                    stepsDone + 1),
                staticPart.getNoEnvironmentActionReward());

        } else if (actionType == BomberManAction.DETONATE_BOMB) {
            var newDroppedBombs = this.droppedBombs;
            var newBombCountDowns = Arrays.copyOf(bombCountDowns, bombCountDowns.length);
            var newBombXCoordinates = Arrays.copyOf(bombXCoordinates, bombXCoordinates.length);
            var newBombYCoordinates = Arrays.copyOf(bombYCoordinates, bombYCoordinates.length);

            Map<Integer, Set<Integer>> affectedSquares = new HashMap<>();

            for (int i = 0; i < this.bombCountDowns.length; i++) {
                if (newBombCountDowns[i] == 0) {
                    addBombRange(newBombXCoordinates[i], newBombYCoordinates[i], affectedSquares);
                    newBombCountDowns[i] = -1;
                    newBombXCoordinates[i] = -1;
                    newBombYCoordinates[i] = -1;
                    newDroppedBombs--;
                }
            }

            var isNotSaturated = true;
            while (isNotSaturated) {
                isNotSaturated = false;
                for (int i = 0; i < this.bombCountDowns.length; i++) {
                    if (newBombCountDowns[i] > 0) {
                        if (affectedSquares.containsKey(newBombXCoordinates[i]) && affectedSquares.get(newBombXCoordinates[i]).contains(newBombYCoordinates[i])) {
                            addBombRange(newBombXCoordinates[i], newBombYCoordinates[i], affectedSquares);
                            newBombCountDowns[i] = -1;
                            newBombXCoordinates[i] = -1;
                            newBombYCoordinates[i] = -1;
                            newDroppedBombs--;
                            isNotSaturated = true;
                        }
                    }
                }
            }

            var newEntityInGameCount = entityInGameCount;
            var newIsInGameArray = Arrays.copyOf(isInGameArray, isInGameArray.length);
            var copyX = Arrays.copyOf(playerXCoordinates, playerXCoordinates.length);
            var copyY = Arrays.copyOf(playerYCoordinates, playerYCoordinates.length);
            var newPlayerLivesCount = Arrays.copyOf(playerLivesCount, playerLivesCount.length);
            for (int i = 0; i < staticPart.getStartingPlayerCount(); i++) {
                if (affectedSquares.containsKey(playerXCoordinates[i]) && affectedSquares.get(playerXCoordinates[i]).contains(playerYCoordinates[i])) {
                    newPlayerLivesCount[i]--;
                    if (newPlayerLivesCount[i] == 0) {
                        newIsInGameArray[i] = false;
                        copyX[i] = -1;
                        copyY[i] = -1;
                        newEntityInGameCount--;
                    }
                }
            }

            var nextIdOnTurn = nextEntityIdOnTurn();

            return new ImmutableStateRewardReturn<>(
                new BomberManState(
                    staticPart,
                    newIsInGameArray,
                    goldsInPlaceArray,
                    newEntityInGameCount,
                    nextIdOnTurn,
                    newPlayerIdOnTurn,
                    newGoldIdOnTurn,
                    copyX,
                    copyY,
                    newPlayerLivesCount,
                    newBombXCoordinates,
                    newBombYCoordinates,
                    newBombCountDowns,
                    newDroppedBombs,
                    stepsDone + 1),
                staticPart.getNoEnvironmentActionReward());
        } else {
            throw EnumUtils.createExceptionForNotExpectedEnumValue(actionType);
        }
    }

    private StateRewardReturn<BomberManAction, DoubleVector, BomberManState> playPlayerAction(BomberManAction actionType, int newIdOnTurn, int newPlayerIdOnTurn, int newGoldIdOnTurn) {
        if (playerIdOnTurn == -1) {
            throw new IllegalStateException("Inconsistency. Check");
        }
        var playerIndexOnTurn = entityIdOnTurn - staticPart.getGoldWithEnvironmentEntityCount();
        if(playerIdOnTurn != playerIndexOnTurn) {
            throw new IllegalStateException("Inconsistency 2. Check");
        }

        var rewardArray = staticPart.getMoveReward()[playerIndexOnTurn];

        if (actionType == BomberManAction.DROP_BOMB) {
            var freeBombSlotIndex = getFreeBombSlotIndex(playerIndexOnTurn);
            if (freeBombSlotIndex >= 0) {

                var copyX = Arrays.copyOf(bombXCoordinates, bombXCoordinates.length);
                var copyY = Arrays.copyOf(bombYCoordinates, bombYCoordinates.length);
                var copyC = Arrays.copyOf(bombCountDowns, bombCountDowns.length);

                copyX[freeBombSlotIndex] = playerXCoordinates[playerIndexOnTurn];
                copyY[freeBombSlotIndex] = playerYCoordinates[playerIndexOnTurn];
                copyC[freeBombSlotIndex] = staticPart.getBombCountDown();

                return new ImmutableStateRewardReturn<>(
                    new BomberManState(
                        staticPart,
                        isInGameArray,
                        goldsInPlaceArray,
                        entityInGameCount,
                        newIdOnTurn,
                        newPlayerIdOnTurn,
                        newGoldIdOnTurn,
                        playerXCoordinates,
                        playerYCoordinates,
                        playerLivesCount,
                        copyX,
                        copyY,
                        copyC,
                        droppedBombs + 1,
                        stepsDone + 1),
                    rewardArray);
            } else {
                return new ImmutableStateRewardReturn<>(
                    new BomberManState(
                        staticPart,
                        isInGameArray,
                        goldsInPlaceArray,
                        entityInGameCount,
                        newIdOnTurn,
                        newPlayerIdOnTurn,
                        newGoldIdOnTurn,
                        playerXCoordinates,
                        playerYCoordinates,
                        playerLivesCount,
                        bombXCoordinates,
                        bombYCoordinates,
                        bombCountDowns,
                        droppedBombs,
                        stepsDone + 1),
                    rewardArray);
            }
        } else {
            var coordinates = tryMakeMove(actionType, playerXCoordinates[playerIndexOnTurn], playerYCoordinates[playerIndexOnTurn]);
            var isMoveDoable = isMoveDoable(coordinates.getFirst(), coordinates.getSecond());

            if (isMoveDoable) {

                var newX = coordinates.getFirst();
                var newY = coordinates.getSecond();

                var copyX = Arrays.copyOf(playerXCoordinates, playerXCoordinates.length);
                var copyY = Arrays.copyOf(playerYCoordinates, playerYCoordinates.length);

                copyX[playerIndexOnTurn] = newX;
                copyY[playerIndexOnTurn] = newY;

                if (staticPart.getGoldEntitiesArray()[newX][newY]) {
                    var goldId = staticPart.getGoldEntitiesReferenceArray()[newX][newY];

                    if (goldsInPlaceArray[goldId]) {
                        var newGoldsInPlaceArray = Arrays.copyOf(goldsInPlaceArray, goldsInPlaceArray.length);
                        newGoldsInPlaceArray[goldId] = false;

                        var newRewardArray = Arrays.copyOf(rewardArray, rewardArray.length);
                        newRewardArray[entityIdOnTurn] += staticPart.getRewardPerGold();

                        return new ImmutableStateRewardReturn<>(
                            new BomberManState(
                                staticPart,
                                isInGameArray,
                                newGoldsInPlaceArray,
                                entityInGameCount,
                                newIdOnTurn,
                                newPlayerIdOnTurn,
                                newGoldIdOnTurn,
                                copyX,
                                copyY,
                                playerLivesCount,
                                bombXCoordinates,
                                bombYCoordinates,
                                bombCountDowns,
                                droppedBombs,
                                stepsDone + 1),
                            newRewardArray);
                    }
                }
                return new ImmutableStateRewardReturn<>(
                    new BomberManState(
                        staticPart,
                        isInGameArray,
                        goldsInPlaceArray,
                        entityInGameCount,
                        newIdOnTurn,
                        newPlayerIdOnTurn,
                        newGoldIdOnTurn,
                        copyX,
                        copyY,
                        playerLivesCount,
                        bombXCoordinates,
                        bombYCoordinates,
                        bombCountDowns,
                        droppedBombs,
                        stepsDone + 1),
                    rewardArray);
            } else {
                return new ImmutableStateRewardReturn<>(
                    new BomberManState(
                        staticPart,
                        isInGameArray,
                        goldsInPlaceArray,
                        entityInGameCount,
                        newIdOnTurn,
                        newPlayerIdOnTurn,
                        newGoldIdOnTurn,
                        playerXCoordinates,
                        playerYCoordinates,
                        playerLivesCount,
                        bombXCoordinates,
                        bombYCoordinates,
                        bombCountDowns,
                        droppedBombs,
                        stepsDone + 1),
                    rewardArray);
            }
        }
    }

    private StateRewardReturn<BomberManAction, DoubleVector, BomberManState> playGoldAction(BomberManAction actionType, int newIdOnTurn, int newPlayerIdOnTurn, int newGoldIdOnTurn) {
        if (goldIdOnTurn == -1) {
            throw new IllegalStateException("Inconsistency. Check");
        }
        if (actionType == BomberManAction.NO_ACTION_REWARD) {
            return new ImmutableStateRewardReturn<>(
                new BomberManState(
                    staticPart,
                    isInGameArray,
                    goldsInPlaceArray,
                    entityInGameCount,
                    newIdOnTurn,
                    newPlayerIdOnTurn,
                    newGoldIdOnTurn,
                    playerXCoordinates,
                    playerYCoordinates,
                    playerLivesCount,
                    bombXCoordinates,
                    bombYCoordinates,
                    bombCountDowns,
                    droppedBombs,
                    stepsDone + 1),
                staticPart.getNoEnvironmentActionReward());
        } else {
            var newGoldsInPlaceArray = Arrays.copyOf(goldsInPlaceArray, goldsInPlaceArray.length);
            newGoldsInPlaceArray[entityIdOnTurn - 1] = true;
            return new ImmutableStateRewardReturn<>(
                new BomberManState(
                    staticPart,
                    isInGameArray,
                    newGoldsInPlaceArray,
                    entityInGameCount,
                    newIdOnTurn,
                    newPlayerIdOnTurn,
                    newGoldIdOnTurn,
                    playerXCoordinates,
                    playerYCoordinates,
                    playerLivesCount,
                    bombXCoordinates,
                    bombYCoordinates,
                    bombCountDowns,
                    droppedBombs,
                    stepsDone + 1),
                staticPart.getNoEnvironmentActionReward());
        }
    }

    @Override
    public StateRewardReturn<BomberManAction, DoubleVector, BomberManState> applyAction(BomberManAction actionType) {
        var newIdOnTurn = nextEntityIdOnTurn();
        var newPlayerIdOnTurn = calculatePlayerId(newIdOnTurn, staticPart.getGoldEntityCount(), staticPart.getStartingPlayerCount());
        var newGoldIdOnTurn = calculateGoldId(newIdOnTurn, staticPart.getGoldEntityCount(), staticPart.getStartingPlayerCount());
        if (actionType.isEnvironmentalAction()) {
            return playEnvironmentAction(actionType, newIdOnTurn, newPlayerIdOnTurn, newGoldIdOnTurn);
        } else if (actionType.isGoldAction()) {
            return playGoldAction(actionType, newIdOnTurn, newPlayerIdOnTurn, newGoldIdOnTurn);
        } else {
            return playPlayerAction(actionType, newIdOnTurn, newPlayerIdOnTurn, newGoldIdOnTurn);
        }
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return observation;
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return observation;
    }

    @Override
    public Predictor<BomberManState> getKnownModelWithPerfectObservationPredictor() {

        return new Predictor<>() {

            private final double[] environmentBombExplosionProbs = new double[] {1.0};
            private final double[] environmentNoActionProbs = new double[] {1.0};
            private final double[] goldRespawnProbs = new double[] {1.0 - staticPart.getGoldRespawnProbability(), staticPart.getGoldRespawnProbability()};
            private final double[] goldInPlaceProbs = new double[] {1.0, 0.0};

            @Override
            public double[] apply(BomberManState observation) {
                if(observation.entityIdOnTurn == ENVIRONMENT_ENTITY_ID) {
                    var anyBombCountDownToZero = false;
                    for (int i = 0; i < observation.bombCountDowns.length; i++) {
                        if(observation.bombCountDowns[i] == 0) {
                            anyBombCountDownToZero = true;
                            break;
                        }
                    }
                    if(anyBombCountDownToZero) {
                        return environmentBombExplosionProbs;
                    } else {
                        return environmentNoActionProbs;
                    }
                } else if (observation.goldIdOnTurn != -1) {
                    if(observation.goldsInPlaceArray[observation.entityIdOnTurn - 1]) {
                        return goldInPlaceProbs;
                    } else {
                        return goldRespawnProbs;
                    }
                } else {
                    throw new IllegalStateException("KnownPredictor for BomberManState can't predict player turn");
                }
            }

            @Override
            public double[][] apply(BomberManState[] observationArray) {
                var prediction = new double[observationArray.length][];
                for (int i = 0; i < prediction.length; i++) {
                    prediction[i] = apply(observationArray[i]);
                }
                return prediction;
            }

            @Override
            public List<double[]> apply(List<BomberManState> observationArray) {
                var output = new ArrayList<double[]>(observationArray.size());
                for (int i = 0; i < observationArray.size(); i++) {
                    output.add(apply(observationArray.get(i)));
                }
                return output;
            }
        };
    }

    @Override
    public String readableStringRepresentation() {

        boolean[][] walls = staticPart.getWalls();
        boolean[][] golds = staticPart.getGoldEntitiesArray();
        int[][] goldReference = staticPart.getGoldEntitiesReferenceArray();
        char[][] matrix = new char[walls.length][];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new char[walls[i].length];
            Arrays.fill(matrix[i], ' ');
            for (int j = 0; j < matrix[i].length; j++) {
                if(walls[i][j]) {
                    matrix[i][j] = '∎';
                }
            }
        }
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if(golds[i][j]) {
                    var idx = goldReference[i][j];
                    if(goldsInPlaceArray[idx]) {
                        matrix[i][j] = 'G';
                    }
                }
            }
        }
        for (int i = 0; i < bombXCoordinates.length; i++) {
            var x = bombXCoordinates[i];
            var y = bombYCoordinates[i];
            if(bombCountDowns[i] != -1) {
                if(matrix[x][y] == 'G') {
                    matrix[x][y] = 'g';
                } else {
                    matrix[x][y] = 'B';
                }
            }
        }
        for (int i = 0; i < playerLivesCount.length; i++) {
            if(playerLivesCount[i] > 0) {
               var x = playerXCoordinates[i];
               var y = playerYCoordinates[i];
               if(matrix[x][y] == 'G') {
                   matrix[x][y] = 'p';
               } else if(matrix[x][y] == 'g') {
                   matrix[x][y] = 'q';
               } else if(matrix[x][y] == 'B') {
                   matrix[x][y] = 'b';
               } else {
                   matrix[x][y] = 'P';
               }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            sb.append(matrix[i]).append(System.lineSeparator());
        }
        sb.append(Arrays.toString(playerLivesCount)).append(System.lineSeparator());
        sb.append(Arrays.toString(bombCountDowns)).append(System.lineSeparator());
        return sb.toString();
    }

    @Override
    public List<String> getCsvHeader() {
        return List.of("Nope");
    }

    @Override
    public List<String> getCsvRecord() {
        return List.of("Nope");
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return entityIdOnTurn;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return entityIdOnTurn < staticPart.getGoldWithEnvironmentEntityCount();
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        if (inGameEntityId < staticPart.getGoldWithEnvironmentEntityCount()) {
            return true; // throw new IllegalStateException("Environment is always in game. WTF. In game entityId: [" + inGameEntityId + "]");
        }
        return isInGameArray[inGameEntityId - staticPart.getGoldWithEnvironmentEntityCount()];
    }

    @Override
    public boolean isFinalState() {
        return entityInGameCount == staticPart.getGoldWithEnvironmentEntityCount() || staticPart.getTotalStepsAllowed() <= stepsDone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BomberManState that = (BomberManState) o;

        if (entityInGameCount != that.entityInGameCount) return false;
        if (entityIdOnTurn != that.entityIdOnTurn) return false;
        if (playerIdOnTurn != that.playerIdOnTurn) return false;
        if (goldIdOnTurn != that.goldIdOnTurn) return false;
        if (droppedBombs != that.droppedBombs) return false;
        if (stepsDone != that.stepsDone) return false;
        if (!staticPart.equals(that.staticPart)) return false;
        if (!Arrays.equals(goldsInPlaceArray, that.goldsInPlaceArray)) return false;
        if (!observation.equals(that.observation)) return false;
        if (!Arrays.equals(isInGameArray, that.isInGameArray)) return false;
        if (!Arrays.equals(playerLivesCount, that.playerLivesCount)) return false;
        if (!Arrays.equals(playerXCoordinates, that.playerXCoordinates)) return false;
        if (!Arrays.equals(playerYCoordinates, that.playerYCoordinates)) return false;
        if (!Arrays.equals(bombXCoordinates, that.bombXCoordinates)) return false;
        if (!Arrays.equals(bombYCoordinates, that.bombYCoordinates)) return false;
        return Arrays.equals(bombCountDowns, that.bombCountDowns);
    }

    @Override
    public int hashCode() {
        int result = staticPart.hashCode();
        result = 31 * result + entityInGameCount;
        result = 31 * result + entityIdOnTurn;
        result = 31 * result + playerIdOnTurn;
        result = 31 * result + goldIdOnTurn;
        result = 31 * result + Arrays.hashCode(goldsInPlaceArray);
        result = 31 * result + observation.hashCode();
        result = 31 * result + Arrays.hashCode(isInGameArray);
        result = 31 * result + Arrays.hashCode(playerLivesCount);
        result = 31 * result + Arrays.hashCode(playerXCoordinates);
        result = 31 * result + Arrays.hashCode(playerYCoordinates);
        result = 31 * result + Arrays.hashCode(bombXCoordinates);
        result = 31 * result + Arrays.hashCode(bombYCoordinates);
        result = 31 * result + Arrays.hashCode(bombCountDowns);
        result = 31 * result + droppedBombs;
        result = 31 * result + stepsDone;
        return result;
    }
}
