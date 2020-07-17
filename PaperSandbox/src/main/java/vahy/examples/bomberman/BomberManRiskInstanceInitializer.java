package vahy.examples.bomberman;

import vahy.api.policy.PolicyMode;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.SplittableRandom;

public class BomberManRiskInstanceInitializer extends AbstractInitialStateSupplier<BomberManConfig, BomberManAction, DoubleVector, BomberManRiskState> {

    private final BomberManStaticPart staticPart;
    private final ArrayList<ImmutableTuple<Integer, Integer>> freeSpotCoordinates;

    public BomberManRiskInstanceInitializer(BomberManConfig problemConfig, SplittableRandom random) {
        super(problemConfig, random);

        var staticPartWithFreeCoordinates = initialize(problemConfig);
        this.staticPart = staticPartWithFreeCoordinates.getFirst();
        this.freeSpotCoordinates = staticPartWithFreeCoordinates.getSecond();
    }

    private ImmutableTuple<BomberManStaticPart, ArrayList<ImmutableTuple<Integer, Integer>>> initialize(BomberManConfig  problemConfig) {
        var gameMatrix = problemConfig.getGameMatrix();
        var startingPlayerCount = problemConfig.getPlayerCount();

        var walls = new boolean[gameMatrix.length][];
        var goldSpawnSpots = new boolean[gameMatrix.length][];
        var goldReference = new int[gameMatrix.length][];
        var goldId = 0;
        var freeSpotCount = 0;
        var freeSpotCoordinates = new ArrayList<ImmutableTuple<Integer, Integer>>();
        for (int i = 0; i < walls.length; i++) {
            walls[i] = new boolean[gameMatrix[i].length];
            goldSpawnSpots[i] = new boolean[gameMatrix[i].length];
            goldReference[i] = new int[gameMatrix[i].length];

            for (int j = 0; j < walls[i].length; j++) {
                walls[i][j] = gameMatrix[i][j] == '∎';
                goldSpawnSpots[i][j] = gameMatrix[i][j] == 'G';
                if(gameMatrix[i][j] == 'G') {
                    goldReference[i][j] = goldId;
                    goldId++;
                } else {
                    goldReference[i][j] = -1;
                }
                if(gameMatrix[i][j] == ' ') {
                    freeSpotCount++;
                    freeSpotCoordinates.add(new ImmutableTuple<>(i, j));
                }
            }
        }

        var environmentEntitiesCount = goldId + 1;
        var totalEntitiesCount = startingPlayerCount + environmentEntitiesCount;

        var staticPart = new BomberManStaticPart(
            walls,
            startingPlayerCount,
            totalEntitiesCount,
            problemConfig.getBombsPerPlayer(),
            problemConfig.getStepPenalty(),
            goldId,
            goldSpawnSpots,
            goldReference,
            problemConfig.getGoldReward(),
            problemConfig.getGoldRespawnProbability(),
            problemConfig.getBombRange(),
            problemConfig.getBombCountDown(),
            problemConfig.getPlayerLivesAtStart(),
            problemConfig.getMaximalStepCountBound());
        return new ImmutableTuple<>(staticPart, freeSpotCoordinates);
    }

    @Override
    protected BomberManRiskState createState_inner(BomberManConfig problemConfig, SplittableRandom random, PolicyMode policyMode) {
        var startingPlayerCount = problemConfig.getPlayerCount();
        var playerXCoordinates = new int[startingPlayerCount];
        var playerYCoordinates = new int[startingPlayerCount];
        for (int i = 0; i < startingPlayerCount; i++) {
            int index = random.nextInt(freeSpotCoordinates.size());
            var coordinates = freeSpotCoordinates.remove(index);
            playerXCoordinates[i] = coordinates.getFirst();
            playerYCoordinates[i] = coordinates.getSecond();
        }
        var innerState = new BomberManState(staticPart, playerXCoordinates, playerYCoordinates, staticPart.getGoldWithEnvironmentEntityCount());
        return new BomberManRiskState(innerState);
    }
}
