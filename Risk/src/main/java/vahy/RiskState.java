package vahy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

public interface RiskState<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> extends State<TAction, TObservation, TState> {

    boolean isRiskHit(int playerId);

    boolean[] getRiskVector();
}
