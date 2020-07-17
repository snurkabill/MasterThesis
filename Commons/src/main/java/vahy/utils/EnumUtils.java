package vahy.utils;

import java.util.Random;

public class EnumUtils {

    private EnumUtils() {
    }

    public static <T extends Enum<?>> T generateRandomEnumUniformly(Class<T> clazz, Random random){
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    public static <T extends Enum<T>> boolean[] createMask(T[] actionArray, int totalActionCount) {
        if(actionArray.length == 0) {
            throw new IllegalArgumentException("No action is allowed here. Undefined state");
        }
        boolean[] mask = new boolean[totalActionCount];
        for (int i = 0; i < actionArray.length; i++) {
            mask[actionArray[i].ordinal()] = true;
        }
        return mask;
    }

    public static <A extends Enum<A>> IllegalArgumentException createExceptionForUnknownEnumValue(Enum<A> enumValue) {
        return new IllegalArgumentException("Unknown enum value: [" + enumValue + "]");
    }

    public static <A extends Enum<A>> IllegalStateException createExceptionForNotExpectedEnumValue(Enum<A> enumValue) {
        return new IllegalStateException("Not expected enum value: [" + enumValue + "])");
    }

}
