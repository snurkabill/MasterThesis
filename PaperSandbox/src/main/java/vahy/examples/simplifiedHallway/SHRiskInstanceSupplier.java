package vahy.examples.simplifiedHallway;

import vahy.api.policy.PolicyMode;
import vahy.examples.simplifiedHallway.cell.Cell;
import vahy.examples.simplifiedHallway.cell.CellType;
import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class SHRiskInstanceSupplier extends AbstractInitialStateSupplier<SHConfig, SHAction,  DoubleVector, SHRiskState> {

    public SHRiskInstanceSupplier(SHConfig config, SplittableRandom random) {
        super(config, random);
    }

    @Override
    protected SHRiskState createState_inner(SHConfig problemConfig, SplittableRandom random, PolicyMode policyMode) {
        return createImmutableInitialState(problemConfig.getGameMatrix(), problemConfig, random);
    }

    private ImmutableTuple<Integer, Integer> generateInitialAgentCoordinates(List<List<Cell>> gameSetup, SplittableRandom random) {
        List<Cell> startingLocations = gameSetup.stream()
            .flatMap(List::stream)
            .filter(cell -> cell.getCellType() == CellType.STARTING_LOCATION)
            .collect(Collectors.toList());
        Cell startingLocation = startingLocations.get(random.nextInt(startingLocations.size()));
        return new ImmutableTuple<>(startingLocation.getCellPosition().getX(), startingLocation.getCellPosition().getY());
    }

    private SHRiskState createImmutableInitialState(List<List<Cell>> gameSetup, SHConfig config, SplittableRandom random) {
        boolean[][] walls = new boolean[gameSetup.size()][gameSetup.get(0).size()];
        double[][] rewards = new double[gameSetup.size()][gameSetup.get(0).size()];
        double[][] trapProbabilities = new double[gameSetup.size()][gameSetup.get(0).size()];
        gameSetup.stream()
            .flatMap(List::stream)
            .forEach(cell -> {
                walls[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.WALL;
                rewards[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.GOAL ? config.getGoalReward() : 0.0;
                trapProbabilities[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.TRAP ? config.getTrapProbability() : 0.0;
            });


        int[][] rewardIds = new int[rewards.length][rewards[0].length];

        int rewardCounter = 0;
        for (int i = 0; i < rewardIds.length; i++) {
            for (int j = 0; j < rewardIds[i].length; j++) {
                if(rewards[i][j] != 0.0) {
                    rewardIds[i][j] = rewardCounter;
                    rewardCounter++;
                }
            }
        }

        SHStaticPart staticGamePart = new SHStaticPart(walls, trapProbabilities, rewardIds, config.getStepPenalty());
        ImmutableTuple<Integer, Integer> agentStartingPosition = generateInitialAgentCoordinates(gameSetup, random);
        return new SHRiskState(new SHState(staticGamePart, agentStartingPosition.getFirst(), agentStartingPosition.getSecond(), true, false, rewards, rewardCounter));
    }




}
