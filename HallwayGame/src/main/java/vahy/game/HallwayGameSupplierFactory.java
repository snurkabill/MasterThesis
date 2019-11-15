package vahy.game;

import vahy.environment.config.GameConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.SplittableRandom;

public class HallwayGameSupplierFactory {

    private final ClassLoader classLoader = this.getClass().getClassLoader();

    public HallwayGameInitialInstanceSupplier getInstanceProvider(HallwayInstance hallwayInstance,
                                                                  GameConfig gameConfig,
                                                                  SplittableRandom random) throws IOException {
        InputStream resourceAsStream = classLoader.getResourceAsStream(hallwayInstance.getPath());
        byte[] bytes = resourceAsStream.readAllBytes();
        return new HallwayGameInitialInstanceSupplier(gameConfig, random);
    }
}
