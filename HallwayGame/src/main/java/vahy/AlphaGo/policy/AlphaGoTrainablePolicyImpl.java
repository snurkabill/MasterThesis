package vahy.AlphaGo.policy;

import vahy.AlphaGo.tree.AlphaGoSearchTree;

import java.util.SplittableRandom;

public class AlphaGoTrainablePolicyImpl extends AlphaGoPolicyImpl {



    public AlphaGoTrainablePolicyImpl(SplittableRandom random, AlphaGoSearchTree searchTree, int updateTreeCount) {
        super(random, searchTree, updateTreeCount);
    }
}
