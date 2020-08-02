package vahy.utils;

import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JUnitParameterizedTestHelper {

    private JUnitParameterizedTestHelper() {
    }

    /**
     * creates the cartesian product of two argument streams
     * <p>
     * look at ParameterizedTestHelperTest for usage examples
     *
     * @param a stream of JUnit5 arguments
     * @param b stream of JUnit5 arguments
     * @return the cartesian product where each tuple of arguments of stream a is multiplied by the tuple of arguments of stream b
     */
    public static <A, B> Stream<Arguments> cartesian(Stream<A> a, Stream<B> b) {
        List<A> argumentsA = a.collect(Collectors.toList());
        List<B> argumentsB = b.collect(Collectors.toList());

        List<Arguments> result = cartesian(argumentsA, argumentsB);

        return result.stream();
    }

    private static <A, B> List<Arguments> cartesian(List<A> argumentsA, List<B> argumentsB) {
        List<Arguments> result = new ArrayList<>(argumentsA.size());
        for (Object o : argumentsA) {
            Object[] objects = asArray(o);
            for (Object o1 : argumentsB) {
                Object[] objects1 = asArray(o1);

                Object[] arguments = ArrayUtils.addAll(objects, objects1);
                result.add(Arguments.of(arguments));
            }
        }
        return result;
    }

    private static Object[] asArray(Object o) {
        Object[] objects;
        if (o instanceof Arguments) {
            objects = ((Arguments) o).get();
        } else {
            objects = new Object[]{o};
        }
        return objects;
    }


}
