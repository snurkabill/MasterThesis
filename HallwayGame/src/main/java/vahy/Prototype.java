package vahy;

import vahy.environment.agent.policy.random.UniformRandomWalkPolicy;
import vahy.environment.config.DefaultGameConfig;
import vahy.environment.episode.EpisodeAggregator;
import vahy.game.InitialStateInstanceFactory;
import vahy.game.NotValidGameStringRepresentationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.SplittableRandom;

public class Prototype {

    public static void main(String[] args) throws IOException, NotValidGameStringRepresentationException {

        ClassLoader classLoader = Prototype.class.getClassLoader();
        URL url = classLoader.getResource("examples/hallway0.txt");
        File file = new File(url.getFile());

        SplittableRandom random = new SplittableRandom(2);

        InitialStateInstanceFactory initialStateInstanceFactory = new InitialStateInstanceFactory(new DefaultGameConfig(), random);
//        Episode episode = new Episode(initialStateInstanceFactory.createInitialState(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())))),
//                new UniformRandomWalkPolicy(random));
//
//        episode.runEpisode();

        EpisodeAggregator episodeAggregator = new EpisodeAggregator(100, 100, new UniformRandomWalkPolicy(random), initialStateInstanceFactory);
        episodeAggregator.runSimulation(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));

        System.out.println("asdf");
    }

}
