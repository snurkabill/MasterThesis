package vahy.data;

import vahy.environment.config.GameConfig;
import vahy.game.HallwayGameInitialInstanceSupplier;
import vahy.game.NotValidGameStringRepresentationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.SplittableRandom;

public class HallwayGameSupplierFactory {

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    public HallwayGameInitialInstanceSupplier getInstanceProvider(HallwayInstance hallwayInstance,
                                                                  GameConfig gameConfig,
                                                                  SplittableRandom random) throws IOException, NotValidGameStringRepresentationException {
        InputStream resourceAsStream = classLoader.getResourceAsStream(hallwayInstance.getPath());
        byte[] bytes = resourceAsStream.readAllBytes();
        return new HallwayGameInitialInstanceSupplier(gameConfig, random, new String(bytes));
    }
}
