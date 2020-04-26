package vahy.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionUtils {

    public static<T> List<List<T>> split(List<T> list, int index) {
        return new ArrayList<>(
            list.stream()
                .collect(Collectors.groupingBy(s -> list.indexOf(s) > index))
                .values()
        );
    }

}
