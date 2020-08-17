package vahy.examples.bomberman;

import vahy.impl.episode.InvalidInstanceSetupException;
import vahy.utils.ArrayUtils;
import vahy.utils.ImmutableTuple;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public enum BomberManInstance {

    BM_00("examples/bomberman/00.txt"),
    BM_01("examples/bomberman/01.txt"),
    BM_02("examples/bomberman/02.txt"),
    BM_05("examples/bomberman/05.txt");

    private final String path;

    BomberManInstance(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public ImmutableTuple<char[][], Integer> getAsPlayground() throws IOException, InvalidInstanceSetupException {
        InputStream resourceAsStream = this.getDeclaringClass().getClassLoader().getResourceAsStream(getPath());
        try {
            var bytes = resourceAsStream.readAllBytes();
            var representation = new String(bytes, Charset.defaultCharset());
            Pattern LINE_SPLIT_PATTERN = Pattern.compile("\\n");
            String[] lines = LINE_SPLIT_PATTERN.split(representation.replace("\r\n", "\n").replace("\r", "\n"));
            return deserialize(Arrays.asList(lines));
        } catch (IOException | InvalidInstanceSetupException e) {
            throw new RuntimeException(e);
        }

//
//        var path = resource.getPath();
//        System.out.println("====================================================================");
//        var file = resource.getFile();
//        System.out.println("====================================================================");
//        URI uri = null;
//        try {
//            uri = resource.toURI();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException("LALALA", e);
//        }
//        System.out.println("====================================================================");
//        System.out.println("====================================================================");
//        System.out.println("====================================================================");
//        System.out.println("====================================================================");
//        System.out.println(path);
//        System.out.println(file);
//        System.out.println(uri.toString());
//
////        var path2 = Paths.get(uri).toFile();
////        System.out.println(path2.toString());
////        System.out.println(path2.getAbsolutePath());
////
////        try(var is = new InputStreamReader(this.getDeclaringClass().getResourceAsStream(getPath()), Charset.defaultCharset());
////            var br = new BufferedReader(is)) {
////            var stream = br.lines();
////            var lines = stream.collect(Collectors.toList());
////
//////        var lines = Files.readAllLines(Paths.get(resource.getPath()));
////            return deserialize(lines);
////
////        }
//
//                var lines = Files.readAllLines(Paths.get(resource.getPath()));
//        return deserialize(lines);
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
