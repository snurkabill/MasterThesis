package vahy.impl.search.tree.treeUpdateCondition;

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
