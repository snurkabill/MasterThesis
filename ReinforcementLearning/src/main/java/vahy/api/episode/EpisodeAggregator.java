package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface EpisodeAggregator<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    List<List<TReward>> runSimulation(String stringGameRepresentation); // TODO remake string to some general interface...
}
