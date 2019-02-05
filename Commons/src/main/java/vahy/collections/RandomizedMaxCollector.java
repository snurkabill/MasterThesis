package vahy.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class RandomizedMaxCollector<elementType> implements Collector<elementType, List<elementType>, elementType> {

    private final Comparator<elementType> comparator;
    private final SplittableRandom random;

    public RandomizedMaxCollector(Comparator<elementType> comparator, SplittableRandom random) {
        this.comparator = comparator;
        this.random = random;
    }

    @Override
    public Supplier<List<elementType>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<elementType>, elementType> accumulator() {
        return (list, element) -> {
            if(list.isEmpty()) {
                list.add(element);
            }
            int compared = comparator.compare(list.get(0), element);
            switch (compared) {
                case (0):
                    list.add(element);
                    break;
                case (1):
                    break; // leaving list as is
                case (-1): {
                    list.clear();
                    list.add(element);
                    break;
                }
                default:
                    throw new IllegalStateException("Comparator returns [" + compared + "] which is not defined");
            }
        };
    }

    @Override
    public BinaryOperator<List<elementType>> combiner() {
        return (firstList, secondList) -> {
            if(firstList.isEmpty()) {
                return secondList;
            }
            if(secondList.isEmpty()) {
                return firstList;
            }
            int compared = comparator.compare(firstList.get(0), secondList.get(0));
            switch (compared) {
                case(0):
                    firstList.addAll(secondList);
                    return firstList;
                case(1):
                    return firstList;
                case(-1):
                    return secondList;
                default:
                    throw new IllegalStateException("Comparator returns [" + compared + "] which is not defined");
            }
        };
    }

    @Override
    public Function<List<elementType>, elementType> finisher() {
        return list -> {
            if(list.isEmpty()) {
                throw new IllegalStateException("Max element does not exist");
            }
            return list.get(random.nextInt(list.size()));
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }
}
