package vahy.impl.search.tree.treeUpdateCondition;

import vahy.api.search.tree.treeUpdateCondition.TreeUpdateCondition;
import vahy.api.search.tree.treeUpdateCondition.TreeUpdateConditionFactory;

public class FixedUpdateCountTreeConditionFactory implements TreeUpdateConditionFactory {

    private final int countOfEnabledUpdates;

    public FixedUpdateCountTreeConditionFactory(int countOfEnabledUpdates) {
        this.countOfEnabledUpdates = countOfEnabledUpdates;
    }

    @Override
    public TreeUpdateCondition create() {
        return new TreeUpdateConditionSuplierCountBased(countOfEnabledUpdates);
    }
}
