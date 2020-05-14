package vahy.paperGenerics;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

public interface PaperState<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> extends State<TAction, TObservation, TState> {

    boolean isRiskHit(int playerId);
}
