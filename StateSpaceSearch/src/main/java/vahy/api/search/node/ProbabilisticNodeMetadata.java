package vahy.api.search.node;

import vahy.api.model.Action;

import java.util.EnumMap;

public interface ProbabilisticNodeMetadata<TAction extends Enum<TAction> & Action> {

    EnumMap<TAction, Double> getChildPriorProbabilities();

    double getPriorProbability();

}
