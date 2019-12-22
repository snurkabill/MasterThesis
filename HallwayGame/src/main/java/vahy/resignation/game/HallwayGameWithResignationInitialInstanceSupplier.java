package vahy.resignation.game;

import vahy.impl.episode.AbstractInitialStateSupplier;
import vahy.impl.model.observation.DoubleVector;
import vahy.original.environment.config.GameConfig;
import vahy.original.environment.state.StaticGamePart;
import vahy.original.game.cell.Cell;
import vahy.original.game.cell.CellType;
import vahy.resignation.environment.HallwayActionWithResign;
import vahy.resignation.environment.agent.AgentHeadingWithResign;
import vahy.resignation.environment.state.EnvironmentProbabilities;
import vahy.resignation.environment.state.HallwayStateWithResign;
import vahy.utils.ArrayUtils;
import vahy.utils.ImmutableTuple;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.Collectors;

public class HallwayGameWithResignationInitialInstanceSupplier extends AbstractInitialStateSupplier<GameConfig, HallwayActionWithResign, DoubleVector, EnvironmentProbabilities, HallwayStateWithResign> {

    public HallwayGameWithResignationInitialInstanceSupplier(GameConfig gameConfig, SplittableRandom random) {
        super(gameConfig, random);
    }

    @Override
    protected HallwayStateWithResign createState_inner(GameConfig problemConfig, SplittableRandom random) {
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

    private HallwayStateWithResign createImmutableInitialState(List<List<Cell>> gameSetup, GameConfig gameConfig, SplittableRandom random) {
        boolean[][] walls = new boolean[gameSetup.size()][gameSetup.get(0).size()];
        double[][] rewards = new double[gameSetup.size()][gameSetup.get(0).size()];
        double[][] trapProbabilities = new double[gameSetup.size()][gameSetup.get(0).size()];
        gameSetup.stream()
            .flatMap(List::stream)
            .forEach(cell -> {
                walls[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.WALL;
                rewards[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.GOAL ? gameConfig.getGoalReward() : 0.0;
                trapProbabilities[cell.getCellPosition().getX()][cell.getCellPosition().getY()] = cell.getCellType() == CellType.TRAP ? gameConfig.getTrapProbability() : 0.0;
            });
        int totalRewardsCount = (int) Arrays.stream(rewards).mapToLong(x -> Arrays.stream(x).filter(y -> y > 0.0).count()).sum();
        StaticGamePart staticGamePart = new StaticGamePart(gameConfig.getStateRepresentation(), trapProbabilities, ArrayUtils.cloneArray(rewards), walls, gameConfig.getStepPenalty(), gameConfig.getNoisyMoveProbability(), totalRewardsCount);
        ImmutableTuple<Integer, Integer> agentStartingPosition = generateInitialAgentCoordinates(gameSetup, random);
        return new HallwayStateWithResign(staticGamePart, rewards, agentStartingPosition.getFirst(), agentStartingPosition.getSecond(), AgentHeadingWithResign.NORTH);
    }




}
