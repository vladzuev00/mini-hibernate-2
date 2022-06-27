package by.zuevvlad.minihibernate.accessmethodextractor;

import by.zuevvlad.minihibernate.accessmethodextractor.exception.ExtractionAccessMethodException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class AccessMethodExtractor
{
    private final AccessMethodNameFounder accessMethodNameFounder;

    public static AccessMethodExtractor createAccessMethodExtractor()
    {
        if(AccessMethodExtractor.accessMethodExtractor == null)
        {
            synchronized(AccessMethodExtractor.class)
            {
                if(AccessMethodExtractor.accessMethodExtractor == null)
                {
                    final AccessMethodNameFounder accessMethodNameFounder = AccessMethodNameFounder
                            .createAccessMethodNameFounder();
                    AccessMethodExtractor.accessMethodExtractor = new AccessMethodExtractor(accessMethodNameFounder);
                }
            }
        }
        return AccessMethodExtractor.accessMethodExtractor;
    }

    private static AccessMethodExtractor accessMethodExtractor = null;

    private AccessMethodExtractor(final AccessMethodNameFounder accessMethodNameFounder)
    {
        super();
        this.accessMethodNameFounder = accessMethodNameFounder;
    }

    public final Method extractSetterMethod(final Field field)
            throws ExtractionAccessMethodException
    {
        try
        {
            final String nameOfSetterMethod = this.accessMethodNameFounder.findSetterMethodName(field);
            final Class<?> declaringClass = field.getDeclaringClass();
            final Class<?> typeOfField = field.getType();
            return declaringClass.getMethod(nameOfSetterMethod, typeOfField);
        }
        catch(final NoSuchMethodException cause)
        {
            throw new ExtractionAccessMethodException(cause);
        }
    }

    public final Method extractGetterMethod(final Field field)
            throws ExtractionAccessMethodException
    {
        try
        {
            final String nameOfGetterMethod = this.accessMethodNameFounder.findGetterMethodName(field);
            final Class<?> declaringClass = field.getDeclaringClass();
            return declaringClass.getMethod(nameOfGetterMethod);
        }
        catch(final NoSuchMethodException cause)
        {
            throw new ExtractionAccessMethodException(cause);
        }
    }

    private static final class AccessMethodNameFounder
    {
        public static AccessMethodNameFounder createAccessMethodNameFounder()
        {
            if(AccessMethodNameFounder.accessMethodNameFounder == null)
            {
                synchronized(AccessMethodNameFounder.class)
                {
                    if(AccessMethodNameFounder.accessMethodNameFounder == null)
                    {
                        AccessMethodNameFounder.accessMethodNameFounder = new AccessMethodNameFounder();
                    }
                }
            }
            return AccessMethodNameFounder.accessMethodNameFounder;
        }

        private static AccessMethodNameFounder accessMethodNameFounder = null;

        private AccessMethodNameFounder()
        {
            super();
        }

        public final String findSetterMethodName(final Field field)
        {
            final String nameOfField = field.getName();
            final char firstLetterInNameOfField = nameOfField.charAt(0);
            final String postfixOfSetterMethod = Character.toUpperCase(firstLetterInNameOfField)
                    + nameOfField.substring(1);
            return AccessMethodNameFounder.PREFIX_OF_SETTER_METHOD + postfixOfSetterMethod;
        }

        private static final String PREFIX_OF_SETTER_METHOD = "set";

        public final String findGetterMethodName(final Field field)
        {
            final String nameOfField = field.getName();
            final char firstLetterInNameOfField = nameOfField.charAt(0);
            final String postfixOfGetterMethod = Character.toUpperCase(firstLetterInNameOfField)
                    + nameOfField.substring(1);
            return AccessMethodNameFounder.PREFIX_OF_GETTER_METHOD + postfixOfGetterMethod;
        }

        private static final String PREFIX_OF_GETTER_METHOD = "get";
    }
}
