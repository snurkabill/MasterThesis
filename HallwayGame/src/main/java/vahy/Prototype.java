package vahy;

import vahy.game.NotValidGameStringRepresentationException;

import java.io.IOException;

public class Prototype {

    public static void main(String[] args) throws IOException, NotValidGameStringRepresentationException {

//
//        ClassLoader classLoader = Prototype.class.getClassLoader();
//        URL url = classLoader.getResource("examples/hallway0.txt");
//        File file = new File(url.getFile());
//
//        HallwayGameFactory hallwayGameFactory = new HallwayGameFactory(new DefaultGameConfig());
//        HallwayGame hallwayGame = hallwayGameFactory.createGame(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
//
//        Random random = new Random(0);
//
//        InitialStateInstanceFactory initialStateInstanceFactory = new InitialStateInstanceFactory(new DefaultGameConfig(), random);
//        Episode episode = new Episode(initialStateInstanceFactory.createInitialState(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())))),
//                new UniformRandomWalkPolicy(random));
//
//        episode.runEpisode();
//
//        System.out.println("TOTAL REWARD: [" + episode.getTotalEpisodicReward() + "]");
//        System.out.println("Reward history: [" + episode.getRewardHistory() + "]");
//
//
//        System.out.println("asdf");
    }

}
