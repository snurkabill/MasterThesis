package vahy;

import vahy.game.HallwayGame;
import vahy.game.HallwayGameFactory;
import vahy.game.NotValidGameStringRepresentationException;
import vahy.environment.config.DefaultGameConfig;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Prototype {

    public static void main(String[] args) throws IOException, NotValidGameStringRepresentationException {


        ClassLoader classLoader = Prototype.class.getClassLoader();
        URL url = classLoader.getResource("examples/hallway0.txt");
        File file = new File(url.getFile());

        HallwayGameFactory hallwayGameFactory = new HallwayGameFactory(new DefaultGameConfig());
        HallwayGame hallwayGame = hallwayGameFactory.createGame(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));

        System.out.println("asdf");
    }

}
