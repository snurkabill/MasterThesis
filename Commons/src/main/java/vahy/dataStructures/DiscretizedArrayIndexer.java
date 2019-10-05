package vahy.dataStructures;

import java.util.ArrayList;
import java.util.List;

public class DiscretizedArrayIndexer<Type> {

    private final ArrayList<Type> indexedList;
    private final int listSize;
    private final double bucketSize;

    public DiscretizedArrayIndexer(ArrayList<Type> indexedList) {
        this.indexedList = indexedList;
        this.listSize = indexedList.size();
        this.bucketSize = 1.0 / listSize;
    }

    public Type getElement(double index) {
        if(index < 0.0 || index >= 1.0) {
           throw new IllegalArgumentException("Index [" + index + "] is out of bounds. Expected index interval: [0; 1)");
        }
        int arrayIndex = (int) (index / bucketSize);
        return indexedList.get(arrayIndex);
    }

    public ArrayList<Type> getInnerList() {
        return indexedList;
    }
}
