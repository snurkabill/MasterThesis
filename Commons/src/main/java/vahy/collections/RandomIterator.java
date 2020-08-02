package vahy.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

// http://www.java2s.com/Tutorials/Java/Collection_How_to/Iterator/Create_random_Iterator.htm
public class RandomIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;

    public RandomIterator(final Iterator<T> i, SplittableRandom random) {
        final List<T> items = new ArrayList<>(0);
        while (i.hasNext()) {
            final T item;
            item = i.next();
            items.add(item);
        }
        Collections.shuffle(items, new Random(random.nextLong()));
        iterator = items.iterator();
    }

    @Override
    public boolean hasNext() {
        return (iterator.hasNext());
    }

    @Override
    public T next() {
        return (iterator.next());
    }

    @Override
    public void remove() {
        iterator.remove();
    }
}
