package vahy.utils;

import java.util.Random;

public class EnumUtils {

    public static <T extends Enum<?>> T generateRandomEnumUniformly(Class<T> clazz, Random random){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    public static IllegalArgumentException createExceptionForUnknownEnumValue(Enum enumValue) {
        return new IllegalArgumentException("Unknown enum value: [" + enumValue + "]");
    }
}
