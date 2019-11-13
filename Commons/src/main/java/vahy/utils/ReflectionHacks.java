package vahy.utils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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

    public static <Type> Type createTypeInstance(final Class<Type> typeClass, final Class<?>[] argumentTypeArray, final Object[] argumentArray) {
        try {
            return typeClass.getConstructor(argumentTypeArray).newInstance(argumentArray);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Unable to create instance of " + typeClass.getName(), e);
        }
    }

    public static <TInvokedOn, TReturnValue> TReturnValue invokeMethod(final TInvokedOn object,
                                             final String methodName,
                                             final Class<?>[] argumentTypeArray,
                                             final Object[] argumentArray) {
        try {
            Method method = object.getClass().getMethod(methodName, argumentTypeArray);
            Object returnValue = method.invoke(object, argumentArray);
            @SuppressWarnings("unchecked")
            TReturnValue returnValue1 = (TReturnValue) returnValue;
            return returnValue1;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
