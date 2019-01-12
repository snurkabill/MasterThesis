package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.learning.model.Model;
import vahy.environment.HallwayAction;
import vahy.environment.state.HallwayStateImpl;
import vahy.game.HallwayGameInitialInstanceSupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelEvaluator {


    private static final Logger logger = LoggerFactory.getLogger(ModelEvaluator.class.getName());

    private final List<HallwayStateImpl> statesToBeEvaluated;

    public ModelEvaluator(List<HallwayStateImpl> statesToBeEvaluated) {
        this.statesToBeEvaluated = statesToBeEvaluated;
    }

    public void evaluate(Model model) {
        for (HallwayStateImpl hallwayState : statesToBeEvaluated) {
            logger.info("Environment state: ");
            logger.info(hallwayState.readableStringRepresentation());
            logger.info("Prediction: {}", model.predict(hallwayState.getPlayerObservation().getObservedVector()));
        }
    }

    public static List<HallwayStateImpl> getStates1(HallwayGameInitialInstanceSupplier supplier) {
//        InputStream resourceAsStream = ModelEvaluator.class.getClassLoader().getResourceAsStream("examples/benchmark/benchmark_05.txt");
//        byte[] bytes = resourceAsStream.readAllBytes();
//        GameConfigImpl gameConfig = new GameConfigImpl(100, 1, 1.0, 0.1, StateRepresentation.COMPACT);
//        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = new HallwayGameInitialInstanceSupplier(gameConfig, new SplittableRandom(), new String(bytes));

        List<HallwayStateImpl> stateList = new ArrayList<>();

        HallwayStateImpl initialState = supplier.createInitialState();
        List<HallwayAction> actions = Arrays.asList(
            HallwayAction.TURN_LEFT,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.TURN_LEFT,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD);
        stateList.add(initialState);
        HallwayStateImpl latest = initialState.applyAction(actions.get(0)).getState();
        stateList.add(latest);
        for (int i = 1; i < actions.size(); i++) {
            latest = latest.applyAction(HallwayAction.NO_ACTION).getState().applyAction(actions.get(i)).getState();
            stateList.add(latest);
        }
        return stateList;
    }

    public static List<HallwayStateImpl> getStates2(HallwayGameInitialInstanceSupplier supplier) {
//        InputStream resourceAsStream = ModelEvaluator.class.getClassLoader().getResourceAsStream("examples/benchmark/benchmark_05.txt");
//        byte[] bytes = resourceAsStream.readAllBytes();
//        GameConfigImpl gameConfig = new GameConfigImpl(100, 1, 1.0, 0.1, StateRepresentation.COMPACT);
//        HallwayGameInitialInstanceSupplier hallwayGameInitialInstanceSupplier = new HallwayGameInitialInstanceSupplier(gameConfig, new SplittableRandom(), new String(bytes));

        List<HallwayStateImpl> stateList = new ArrayList<>();

        HallwayStateImpl initialState = supplier.createInitialState();
        List<HallwayAction> actions = Arrays.asList(
            HallwayAction.TURN_LEFT,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.TURN_LEFT,
            HallwayAction.FORWARD,
            HallwayAction.TURN_LEFT,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.TURN_LEFT,
            HallwayAction.TURN_LEFT,
            HallwayAction.FORWARD,
            HallwayAction.TURN_LEFT,
            HallwayAction.FORWARD,
            HallwayAction.TURN_RIGHT,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD,
            HallwayAction.FORWARD);
        stateList.add(initialState);
        HallwayStateImpl latest = initialState.applyAction(actions.get(0)).getState();
        stateList.add(latest);
        for (int i = 1; i < actions.size(); i++) {
            latest = latest.applyAction(HallwayAction.NO_ACTION).getState().applyAction(actions.get(i)).getState();
            stateList.add(latest);
        }
        return stateList;
    }
}
