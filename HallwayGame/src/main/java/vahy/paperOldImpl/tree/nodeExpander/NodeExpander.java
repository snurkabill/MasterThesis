package vahy.paperOldImpl.tree.nodeExpander;

import vahy.paperOldImpl.tree.SearchNode;

public interface NodeExpander {

    void expandNode(SearchNode node);

    int getNodesExpandedCount();
}
