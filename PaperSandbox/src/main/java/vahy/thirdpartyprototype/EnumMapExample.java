package vahy.thirdpartyprototype;

import java.util.EnumMap;
import java.util.Map;

public class EnumMapExample {

    private enum MyTestEnum {
        A,
        B,
        C,
        D,
        E,
        F;
    }

    public static void main(String[] args) {

        EnumMap<MyTestEnum, Integer> enumMap = new EnumMap<>(MyTestEnum.class);


        enumMap.put(MyTestEnum.C, 2);
        enumMap.put(MyTestEnum.B, 1);

        print(enumMap);

        enumMap.put(MyTestEnum.F, 5);

        print(enumMap);

        enumMap.put(MyTestEnum.F, 6);

        print(enumMap);

        enumMap.put(MyTestEnum.E, 4);
        enumMap.put(MyTestEnum.A, 0);

        print(enumMap);


    }

    private static void print(Map<MyTestEnum, Integer> enumMap) {
        System.out.println("PRINT:");
        System.out.println("---------------");
        for (Map.Entry<MyTestEnum, Integer> myTestEnumIntegerEntry : enumMap.entrySet()) {
            System.out.println(myTestEnumIntegerEntry.getKey() + " " + myTestEnumIntegerEntry.getValue());
        }
        System.out.println("ITERATION:");
        for (int i = 0; i < enumMap.size(); i++) {

        }
    }
}
