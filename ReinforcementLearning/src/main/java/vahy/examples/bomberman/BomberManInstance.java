package vahy.examples.bomberman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.utils.ArrayUtils;
import vahy.utils.ImmutableTuple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public enum BomberManInstance {

    BM_00("examples/bomberman/00.txt"),
    BM_01("examples/bomberman/01.txt"),
    BM_02("examples/bomberman/02.txt"),
    BM_05("examples/bomberman/05.txt");

    private static final Logger logger = LoggerFactory.getLogger(BomberManInstance.class.getName());

    private final String path;

    BomberManInstance(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public ImmutableTuple<char[][], Integer> getAsPlayground() throws IOException, InvalidInstanceSetupException {
        var resource = BomberManInstance.class.getClassLoader().getResource(getPath());
        logger.error(resource.getPath());
        var lines = Files.readAllLines(Paths.get(resource.getPath()));
        return deserialize(lines);
    }

    private ImmutableTuple<char[][], Integer> deserialize(List<String> lines) throws InvalidInstanceSetupException {
        List<List<Character>> list = new ArrayList<>(0);
        int goldCount = 0;
        for (int i = 0; i < lines.size(); i++) {
            char[] chars = lines.get(i).toCharArray();
            List<Character> innerList = new ArrayList<>(0);
            for (int j = 0; j < chars.length; j++) {
                innerList.add(chars[j]);
                if(chars[j] == 'G') {
                    goldCount++;
                }
            }
            list.add(innerList);
        }
        checkGameShape(list);
        var matrix = new char[list.size()][];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new char[list.get(i).size()];
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = list.get(i).get(j);
            }
        }
        return new ImmutableTuple<>(matrix, goldCount);
    }


    private void checkGameShape(List<List<Character>> gameSetup) {
        if (!ArrayUtils.hasRectangleShape(gameSetup)) {
            throw new IllegalArgumentException("Game is not in rectangle-like shape.");
        }
    }

}
