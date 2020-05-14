package vahy.examples.bomberman;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateRewardReturn;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.EnumUtils;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BomberManState implements State<BomberManAction, DoubleVector, BomberManState> {

    public static int ENVIRONMENT_ENTITY_ID = 0;

    public static int ENTITY_ON_TURN_INDEX = 0;


    public static BomberManAction[] ENVIRONMENT_ACTION_ARRAY = (BomberManAction[]) Arrays.stream(BomberManAction.values()).filter(BomberManAction::isEnvironmentalAction).toArray();
    public static BomberManAction[] PLAYER_ACTION_ARRAY = (BomberManAction[]) Arrays.stream(BomberManAction.values()).filter(x -> !x.isEnvironmentalAction()).toArray();

    private final BomberManStaticPart staticPart;
    private final boolean[] isInGameArray;

    private final int entityInGameCount;
    private final int entityIdOnTurn;

    private final DoubleVector observation;

    private final int[] playerXCoordinates;
    private final int[] playerYCoordinates;
    private final int[] playerLivesCount;

//    private final boolean[] rewardsInPlaceArray;

    private final int[] bombXCoordinates;
    private final int[] bombYCoordinates;
    private final int[] bombCountDowns;
    private final int droppedBombs;

    public BomberManState(BomberManStaticPart staticPart,
                          boolean[] isInGameArray,
                          int entityInGameCount,
                          int entityIdOnTurn,
                          DoubleVector observation,
                          int[] playerXCoordinates,
                          int[] playerYCoordinates,
                          int[] playerLivesCount,
                          int[] bombXCoordinates,
                          int[] bombYCoordinates,
                          int[] bombCountDowns,
                          int droppedBombs)
    {
        this.staticPart = staticPart;
        this.isInGameArray = isInGameArray;
        this.entityInGameCount = entityInGameCount;
        this.entityIdOnTurn = entityIdOnTurn;
        this.observation = observation;
        this.playerXCoordinates = playerXCoordinates;
        this.playerYCoordinates = playerYCoordinates;
        this.playerLivesCount = playerLivesCount;
//        this.rewardsInPlaceArray = rewardsInPlaceArray;
        this.bombXCoordinates = bombXCoordinates;
        this.bombYCoordinates = bombYCoordinates;
        this.bombCountDowns = bombCountDowns;
        this.droppedBombs = droppedBombs;
    }

    public BomberManState(BomberManStaticPart staticPart,
                          int entityInGameCount,
                          int[] playerXCoordinates,
                          int[] playerYCoordinates,
                          int entityIdOnTurn)
    {
        if(entityIdOnTurn == 0) {
            throw new IllegalStateException("Environment can't start");
        }
        var playerCount = entityInGameCount - 1;
        this.staticPart = staticPart;
        var booleanArray = new boolean[playerCount];
        Arrays.fill(booleanArray, true);
        this.isInGameArray = booleanArray;
        this.entityInGameCount = entityInGameCount;
        this.entityIdOnTurn = entityIdOnTurn;
        this.playerXCoordinates = playerXCoordinates;
        this.playerYCoordinates = playerYCoordinates;
        var livesArray = new int[playerCount];
        Arrays.fill(livesArray, staticPart.getPlayerLivesAtStart());
        this.playerLivesCount = livesArray;
//        this.rewardsInPlaceArray = new boolean[(entityInGameCount - 1) * staticPart.getRewardsPerPlayer()];

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
        this.observation = createObservation();
    }

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

    private DoubleVector createObservation() {
        var maxPlayerCount = this.staticPart.getStartingPlayerCount();
        var maxBombCount = maxPlayerCount * this.staticPart.getBombsPerPlayer();
//        var maxRewardCount = maxPlayerCount * this.staticPart.getRewardsPerPlayer();


//        var playerCoordinatesCount = maxPlayerCount * 2;
        var playerInfo = maxPlayerCount * 3; //X, Y, Lives

//        var bombCoordinatesCount = maxBombCount * 2;
        var bombInfo = maxBombCount * 3; // X, Y, Countdonw

//        var newObservationArray = new double[1 + playerCoordinatesCount + totalBombInfo + maxRewardCount];
        var newObservationArray = new double[1 + playerInfo + bombInfo]; // one for entity on turn

        newObservationArray[ENTITY_ON_TURN_INDEX] = entityIdOnTurn;
        int index = 1;
        for (int i = 0; i < maxPlayerCount; i++) {
            newObservationArray[index] = getXPortion(this.staticPart, this.playerXCoordinates[i]);
            newObservationArray[index + 1] = getYPortion(this.staticPart, this.playerYCoordinates[i]);
            newObservationArray[index + 2] = this.playerLivesCount[i];
            index += 3;
        }

        for (int i = 0; i < bombInfo; i++) {
            newObservationArray[index] = getXPortion(this.staticPart, this.bombXCoordinates[i]);
            newObservationArray[index + 1] = getYPortion(this.staticPart, this.bombYCoordinates[i]);
            newObservationArray[index + 2] = this.bombCountDowns[i];
            index += 3;
        }

//        for (int i = 0; i < maxRewardCount; i++) {
//            newObservationArray[index] = rewardsInPlaceArray[i] ? 0.5 : -0.5;
//            index++;
//        }
        return new DoubleVector(newObservationArray);
    }

    @Override
    public BomberManAction[] getAllPossibleActions() {
        if(entityIdOnTurn == ENVIRONMENT_ENTITY_ID) {
            return ENVIRONMENT_ACTION_ARRAY;
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

    private int nextEntityIdOnTurn() {
        var index = entityIdOnTurn;
        do {
            index++;
            if(index >= isInGameArray.length) {
                index = 0;
            }
        } while(!isInGameArray[index]);
        if(index == entityIdOnTurn) {
            throw new IllegalStateException("There is only one entity left. Should not happen. ");
        }
        return index;
    }

    private int getFreeBombSlotIndex(int playerId) {
        var minBombIndex = playerId * staticPart.getBombsPerPlayer();

        for (int i = 0; i < staticPart.getBombsPerPlayer(); i++) {
            if(bombYCoordinates[minBombIndex + i] == -1) {
                return minBombIndex + i;
            }
        }
        return -1;
    }

    private void addBombSquare(int x, int y, Map<Integer, Set<Integer>> map) {
        if(!map.containsKey(x)) {
            var set = new HashSet<Integer>();
            set.add(y);
            map.put(x, set);
        } else {
            var set = map.get(x);
            set.add(y);
        }
    }

    private void addBombRange(int x, int y, Map<Integer, Set<Integer>> map) {
        for (int i = 0; i <= staticPart.getBombRange(); i++) {
            var xCoord = x + i;
            if(xCoord > 0 && xCoord < staticPart.getWalls().length) {
                addBombSquare(xCoord, y, map);
            }
            xCoord = x - i;
            if(xCoord > 0 && xCoord < staticPart.getWalls().length) {
                addBombSquare(xCoord, y, map);
            }
            var yCoord = y + i;
            if(yCoord > 0 && yCoord < staticPart.getWalls()[0].length) {
                addBombSquare(x, yCoord, map);
            }
            yCoord = y - i;
            if(yCoord > 0 && yCoord < staticPart.getWalls()[0].length) {
                addBombSquare(x, yCoord, map);
            }
        }
    }

    @Override
    public StateRewardReturn<BomberManAction, DoubleVector, BomberManState> applyAction(BomberManAction actionType) {

        var newObservation = Arrays.copyOf(observation.getObservedVector(), observation.getObservedVector().length);
        var wrappedNewObservation = new DoubleVector(newObservation);
        var newIdOnTurn = nextEntityIdOnTurn();
        newObservation[ENTITY_ON_TURN_INDEX] = newIdOnTurn;


        if(actionType.isEnvironmentalAction()) {
            if(entityIdOnTurn != ENVIRONMENT_ENTITY_ID) {
                throw new IllegalStateException("Inconsistency. Check");
            }

            var copyC = Arrays.copyOf(bombCountDowns, bombCountDowns.length);
            for (int i = 0; i < copyC.length; i++) {
                if(copyC[i] > 0) {
                    copyC[i]--;
                } else if(copyC[i] == 0) {
                    throw new IllegalStateException("Bomb should have detonate");
                }
            }

            var bombInfoStart = 1 + staticPart.getStartingPlayerCount() * 3;
            for (int i = 0; i < staticPart.getStartingPlayerCount(); i++) {
                newObservation[bombInfoStart + 2] = newObservation[bombInfoStart + 2] - 1;
            }

            if(actionType == BomberManAction.NO_ACTION) {
                return new ImmutableStateRewardReturn<>(
                    new BomberManState(
                        staticPart,
                        isInGameArray,
                        entityInGameCount,
                        newIdOnTurn,
                        wrappedNewObservation,
                        playerXCoordinates,
                        playerYCoordinates,
                        playerLivesCount,
                        bombXCoordinates,
                        bombYCoordinates,
                        copyC,
                        droppedBombs),
                    staticPart.getNoEnvironmentActionReward());

            } else if(actionType == BomberManAction.DETONATE_BOMB) {
                var newDroppedBombs = this.droppedBombs;
                var newBombCountDowns = Arrays.copyOf(bombCountDowns, bombCountDowns.length);
                var newBombXCoordinates = Arrays.copyOf(bombXCoordinates, bombXCoordinates.length);
                var newBombYCoordinates = Arrays.copyOf(bombYCoordinates, bombYCoordinates.length);

                if(bombCountDowns[0] != 0) {
                    throw new IllegalStateException("Countdown is not on zero");
                }
                Map<Integer, Set<Integer>> affectedSquares = new HashMap<>();

                for (int i = 0; i < this.bombCountDowns.length; i++) {
                    if(bombCountDowns[i] == 0) {
                        addBombRange(bombXCoordinates[i], bombYCoordinates[i], affectedSquares);
                        newBombCountDowns[i] = -1;
                        newBombXCoordinates[i] = -1;
                        newBombYCoordinates[i] = -1;
                        newDroppedBombs--;
                    }
                }

                var isNotSaturated = true;
                while(isNotSaturated) {
                    isNotSaturated = false;
                    for (int i = 0; i < this.bombCountDowns.length; i++) {
                        if(bombCountDowns[i] > 0) {
                            if(affectedSquares.containsKey(bombXCoordinates[i]) && affectedSquares.get(bombXCoordinates[i]).contains(bombYCoordinates[i])) {
                                addBombRange(bombXCoordinates[i], bombYCoordinates[i], affectedSquares);
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
                for (int i = 0; i < staticPart.getPlayerLivesAtStart(); i++) {
                    if(affectedSquares.containsKey(playerXCoordinates[i]) && affectedSquares.get(playerXCoordinates[i]).contains(playerYCoordinates[i])) {
                        newPlayerLivesCount[i]--;
                        if(newPlayerLivesCount[i] == 0) {
                            newIsInGameArray[i] = false;
                            copyX[i] = -1;
                            copyY[i] = -1;
                        }
                    }
                }

                var nextIdOnTurn = nextEntityIdOnTurn();

                return new ImmutableStateRewardReturn<>(new BomberManState(
                    staticPart,
                    newIsInGameArray,
                    newEntityInGameCount,
                    nextIdOnTurn,
                    wrappedNewObservation,
                    copyX,
                    copyY,
                    newPlayerLivesCount,
                    newBombXCoordinates,
                    newBombYCoordinates,
                    newBombCountDowns,
                    newDroppedBombs
                ), staticPart.getNoEnvironmentActionReward());
            } else {
                throw EnumUtils.createExceptionForNotExpectedEnumValue(actionType);
            }
        } else {
            var playerIndexOnTurn = entityIdOnTurn - 1;

            var rewardArray = staticPart.getMoveReward()[playerIndexOnTurn];

            if(actionType == BomberManAction.DROP_BOMB) {
                var freeBombSlotIndex = getFreeBombSlotIndex(playerIndexOnTurn);
                if(freeBombSlotIndex >= 0) {

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
                            entityInGameCount,
                            newIdOnTurn,
                            wrappedNewObservation,
                            playerXCoordinates,
                            playerYCoordinates,
                            playerLivesCount,
                            copyX,
                            copyY,
                            copyC,
                            droppedBombs + 1),
                        rewardArray);
                } else {
                    return new ImmutableStateRewardReturn<>(
                        new BomberManState(
                            staticPart,
                            isInGameArray,
                            entityInGameCount,
                            newIdOnTurn,
                            wrappedNewObservation,
                            playerXCoordinates,
                            playerYCoordinates,
                            playerLivesCount,
                            bombXCoordinates,
                            bombYCoordinates,
                            bombCountDowns,
                            droppedBombs),
                        rewardArray);
                }
            } else {
                var coordinates = tryMakeMove(actionType, playerXCoordinates[playerIndexOnTurn], playerYCoordinates[playerIndexOnTurn]);
                var isMoveDoable = isMoveDoable(coordinates.getFirst(), coordinates.getSecond());

                if(isMoveDoable) {

                    var newX = coordinates.getFirst();
                    var newY = coordinates.getSecond();

                    newObservation[1 + playerIndexOnTurn] = getXPortion(staticPart, newX);
                    newObservation[1 + playerIndexOnTurn + 1] = getYPortion(staticPart, newY);

                    var copyX = Arrays.copyOf(playerXCoordinates, playerXCoordinates.length);
                    var copyY = Arrays.copyOf(playerYCoordinates, playerYCoordinates.length);

                    copyX[playerIndexOnTurn] = newX;
                    copyY[playerIndexOnTurn] = newY;

                    return new ImmutableStateRewardReturn<>(
                        new BomberManState(
                            staticPart,
                            isInGameArray,
                            entityInGameCount,
                            newIdOnTurn,
                            wrappedNewObservation,
                            copyX,
                            copyY,
                            playerLivesCount,
                            bombXCoordinates,
                            bombYCoordinates,
                            bombCountDowns,
                            droppedBombs),
                        rewardArray);
                } else {
                    return new ImmutableStateRewardReturn<>(
                        new BomberManState(
                            staticPart,
                            isInGameArray,
                            entityInGameCount,
                            newIdOnTurn,
                            wrappedNewObservation,
                            playerXCoordinates,
                            playerYCoordinates,
                            playerLivesCount,
                            bombXCoordinates,
                            bombYCoordinates,
                            bombCountDowns,
                            droppedBombs),
                        rewardArray);
                }
            }
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
        return null;
    }

    @Override
    public String readableStringRepresentation() {
        return "Nope";
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
    public int getTotalPlayerCount() {
        return entityInGameCount;
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return entityIdOnTurn;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        if(inGameEntityId == 0) {
            throw new IllegalStateException("Environment is always in game. WTF");
        }
        return isInGameArray[inGameEntityId - 1];
    }

    @Override
    public boolean isFinalState() {
        return entityInGameCount <= 2;
    }
}
