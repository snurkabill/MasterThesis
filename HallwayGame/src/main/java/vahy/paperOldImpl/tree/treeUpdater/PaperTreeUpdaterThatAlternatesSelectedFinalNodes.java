package vahy.paperOldImpl.tree.treeUpdater;

import vahy.paperOldImpl.tree.SearchNode;

public class PaperTreeUpdaterThatAlternatesSelectedFinalNodes extends PaperTreeUpdater {

    @Override
    public void updateTree(SearchNode expandedNode) {
        if(expandedNode.isFinalNode()) {
            if(!expandedNode.getWrappedState().isAgentKilled()) {
                expandedNode.setEstimatedRisk(0.0d);
            }
        }
        super.updateTree(expandedNode);
    }
}
