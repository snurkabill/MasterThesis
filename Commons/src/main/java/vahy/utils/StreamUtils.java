package vahy.utils;

import vahy.collections.RandomizedMaxCollector;
import vahy.vizualization.LabelData;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.SplittableRandom;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {

    private StreamUtils() {
    }

    public static Stream<Long> getSeedStream(long seed, int streamSize) {
        return new SplittableRandom(seed).longs(streamSize).boxed();
    }

    public static Stream<Long> getSeedStream(int streamSize) {
        return getSeedStream(0, streamSize);
    }

    // From SO: https://stackoverflow.com/a/23529010/1815451
    public static<elementType, B, C> Stream<C> zip(Stream<? extends elementType> a,
                                                   Stream<? extends B> b,
                                                   BiFunction<? super elementType, ? super B, ? extends C> zipper) {
        Objects.requireNonNull(zipper);
        Spliterator<? extends elementType> aSpliterator = Objects.requireNonNull(a).spliterator();
        Spliterator<? extends B> bSpliterator = Objects.requireNonNull(b).spliterator();

        // Zipping looses DISTINCT and SORTED characteristics
        int characteristics = aSpliterator.characteristics() & bSpliterator.characteristics() &
            ~(Spliterator.DISTINCT | Spliterator.SORTED);

        long zipSize = ((characteristics & Spliterator.SIZED) != 0)
            ? Math.min(aSpliterator.getExactSizeIfKnown(), bSpliterator.getExactSizeIfKnown())
            : -1;

        Iterator<elementType> aIterator = Spliterators.iterator(aSpliterator);
        Iterator<B> bIterator = Spliterators.iterator(bSpliterator);
        Iterator<C> cIterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return aIterator.hasNext() && bIterator.hasNext();
            }

            @Override
            public C next() {
                return zipper.apply(aIterator.next(), bIterator.next());
            }
        };

        Spliterator<C> split = Spliterators.spliterator(cIterator, zipSize, characteristics);
        return (a.isParallel() || b.isParallel())
            ? StreamSupport.stream(split, true)
            : StreamSupport.stream(split, false);
    }

    public static<elementType> RandomizedMaxCollector<elementType> toRandomizedMaxCollector(Comparator<elementType> comparator, SplittableRandom random) {
        return new RandomizedMaxCollector<>(comparator, random);
    }


    public static List<LabelData> labelWrapperFunction(List<Double> x) {
        return StreamUtils.zip(
            IntStream.range(0, x.size()).boxed(),
            x.stream(), (y, z) -> new LabelData(String.valueOf(y), z))
            .collect(Collectors.toList());
    };

}
