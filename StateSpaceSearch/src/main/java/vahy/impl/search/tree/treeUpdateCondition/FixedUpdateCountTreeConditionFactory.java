package vahy.impl.search.tree.treeUpdateCondition;

import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;

public class FixedUpdateCountTreeConditionFactory implements TreeUpdateConditionFactory {

    private final int countOfEnabledUpdates;

    public FixedUpdateCountTreeConditionFactory(int countOfEnabledUpdates) {
        if(countOfEnabledUpdates <= 0) {
            throw new IllegalArgumentException("Tree update count must be positive. Value: [" + countOfEnabledUpdates + "]");
        }
        this.countOfEnabledUpdates = countOfEnabledUpdates;
    }

    @Override
    public TreeUpdateCondition create() {
        return new TreeUpdateConditionSuplierCountBased(countOfEnabledUpdates);
    }


    @Override
    public String toString() {
        return "FixedUpdateCountTreeConditionFactory{" +
            "countOfEnabledUpdates=" + countOfEnabledUpdates +
            '}';
    }
}
