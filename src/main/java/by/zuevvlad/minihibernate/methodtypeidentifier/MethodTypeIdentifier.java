package by.zuevvlad.minihibernate.methodtypeidentifier;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class MethodTypeIdentifier
{
    public static MethodTypeIdentifier createMethodTypeIdentifier()
    {
        if(MethodTypeIdentifier.methodTypeIdentifier == null)
        {
            synchronized(MethodTypeIdentifier.class)
            {
                if(MethodTypeIdentifier.methodTypeIdentifier == null)
                {
                    MethodTypeIdentifier.methodTypeIdentifier = new MethodTypeIdentifier();
                }
            }
        }
        return MethodTypeIdentifier.methodTypeIdentifier;
    }

    private static MethodTypeIdentifier methodTypeIdentifier = null;

    private MethodTypeIdentifier()
    {
        super();
    }

    public final boolean isSetterMethod(final Method method)
    {
        final String nameOfMethod = method.getName();
        final Class<?> returnTypeOfMethod = method.getReturnType();
        final int amountOfParametersOfMethod = method.getParameterCount();
        final int modifiersOfMethod = method.getModifiers();
        return     nameOfMethod.matches(MethodTypeIdentifier.REGEX_OF_NAME_OF_SETTER_METHOD)
                && returnTypeOfMethod == void.class
                && amountOfParametersOfMethod == MethodTypeIdentifier.AMOUNT_OF_PARAMETERS_OF_SETTER_METHOD
                && !Modifier.isStatic(modifiersOfMethod)
                && !Modifier.isAbstract(modifiersOfMethod)
                && Modifier.isPublic(modifiersOfMethod);
    }

    private static final String REGEX_OF_NAME_OF_SETTER_METHOD = "set[A-Z].*";
    private static final int AMOUNT_OF_PARAMETERS_OF_SETTER_METHOD = 1;
}
