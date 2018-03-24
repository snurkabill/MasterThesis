package vahy;

import vahy.environment.agent.policy.random.UniformRandomWalkPolicy;
import vahy.environment.config.DefaultGameConfig;
import vahy.environment.episode.Episode;
import vahy.game.HallwayGame;
import vahy.game.HallwayGameFactory;
import vahy.game.InitialStateInstanceFactory;
import vahy.game.NotValidGameStringRepresentationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Prototype {

    public static void main(String[] args) throws IOException, NotValidGameStringRepresentationException {


        ClassLoader classLoader = Prototype.class.getClassLoader();
        URL url = classLoader.getResource("examples/hallway0.txt");
        File file = new File(url.getFile());

        HallwayGameFactory hallwayGameFactory = new HallwayGameFactory(new DefaultGameConfig());
        HallwayGame hallwayGame = hallwayGameFactory.createGame(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));

        Random random = new Random(0);

        InitialStateInstanceFactory initialStateInstanceFactory = new InitialStateInstanceFactory(new DefaultGameConfig(), random);
        Episode episode = new Episode(initialStateInstanceFactory.createInitialState(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())))),
                new UniformRandomWalkPolicy(random));

        episode.runEpisode();

        System.out.println("TOTAL REWARD: [" + episode.getTotalEpisodicReward() + "]");
        System.out.println("Reward history: [" + episode.getRewardHistory() + "]");


        System.out.println("asdf");
    }

}
