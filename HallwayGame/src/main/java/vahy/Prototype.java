package vahy;

import vahy.environment.agent.policy.exhaustive.BfsPolicy;
import vahy.environment.config.ConfigBuilder;
import vahy.environment.config.GameConfig;
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
//        URL url = classLoader.getResource("examples/hallway_demo.txt");
        URL url = classLoader.getResource("examples/hallway0.txt");
        File file = new File(url.getFile());
        SplittableRandom random = new SplittableRandom(2);
        GameConfig gameConfig = new ConfigBuilder().reward(100).noisyMoveProbability(0.05).stepPenalty(1).trapProbability(0.0).buildConfig();
        InitialStateInstanceFactory initialStateInstanceFactory = new InitialStateInstanceFactory(gameConfig, random);
        EpisodeAggregator episodeAggregator = new EpisodeAggregator(
            1,
            10,
//            new UniformRandomWalkPolicy(random),
            new BfsPolicy(),
            initialStateInstanceFactory);
        episodeAggregator.runSimulation(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
    }
}
