package vahy.utils;

import java.lang.reflect.Array;

public class ReflectionHacks {

    @SuppressWarnings("unchecked")
    public static <Type> Type[] arrayFromGenericClass(final Class<Type> typeClass, int length)
    {
        return (Type[]) Array.newInstance(arrayClassFromClass(typeClass), length);
    }

    @SuppressWarnings("unchecked")
    public static <Type> Class<Type[]> arrayClassFromClass(final Class<Type> typeClass)
    {
        Type[] typeArray = (Type[]) Array.newInstance(typeClass, 0);
        return (Class<Type[]>) typeArray.getClass();
    }
}
