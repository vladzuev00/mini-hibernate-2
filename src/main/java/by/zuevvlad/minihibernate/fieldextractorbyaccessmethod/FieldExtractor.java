package by.zuevvlad.minihibernate.fieldextractorbyaccessmethod;

import by.zuevvlad.minihibernate.annotation.Id;
import by.zuevvlad.minihibernate.fieldextractorbyaccessmethod.exception.FieldExtractionByAccessMethodException;
import by.zuevvlad.minihibernate.fieldextractorbyaccessmethod.exception.FieldExtractionFieldOfIdException;
import by.zuevvlad.minihibernate.fieldsofclassfounder.FieldsOfClassFounder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class FieldExtractor
{
    private final FieldsOfClassFounder fieldsOfClassFounder;

    public static FieldExtractor createFieldExtractor()
    {
        if(FieldExtractor.fieldExtractor == null)
        {
            synchronized(FieldExtractor.class)
            {
                if(FieldExtractor.fieldExtractor == null)
                {
                    FieldExtractor.fieldExtractor = new FieldExtractor();
                }
            }
        }
        return FieldExtractor.fieldExtractor;
    }

    private static FieldExtractor fieldExtractor = null;

    private FieldExtractor()
    {
        super();
        this.fieldsOfClassFounder = FieldsOfClassFounder.createFieldsOfClassFounder();
    }

    public final Field extractFieldBySetterMethod(final Method setterMethod)
            throws FieldExtractionByAccessMethodException
    {
        try
        {
            final String nameOfMethod = setterMethod.getName();
            final String nameOfMethodWithoutPrefix = nameOfMethod.replaceFirst(
                    FieldExtractor.PREFIX_OF_SETTER_METHOD,
                    FieldExtractor.EMPTY_STRING_REPLACING_PREFIX_OF_ACCESS_METHOD);

            final char firstLetterOfNameOfMethodWithoutPrefix = nameOfMethodWithoutPrefix.charAt(
                    FieldExtractor.INDEX_OF_FIRST_LETTER_IN_STRING);
            final char firstLetterOfNameOfMethodWithoutPrefixInLowerCase = Character.toLowerCase
                    (firstLetterOfNameOfMethodWithoutPrefix);
            final String nameOfExtractedField = nameOfMethodWithoutPrefix.replaceFirst(
                    Character.toString(firstLetterOfNameOfMethodWithoutPrefix),
                    Character.toString(firstLetterOfNameOfMethodWithoutPrefixInLowerCase));

            final Class<?> declaringClass = setterMethod.getDeclaringClass();
            return declaringClass.getDeclaredField(nameOfExtractedField);
        }
        catch(final NoSuchFieldException cause)
        {
            throw new FieldExtractionByAccessMethodException(cause);
        }
    }

    private static final String PREFIX_OF_SETTER_METHOD = "set";
    private static final String EMPTY_STRING_REPLACING_PREFIX_OF_ACCESS_METHOD = "";
    private static final int INDEX_OF_FIRST_LETTER_IN_STRING = 0;

    public final Field extractFieldOfId(final Class<?> classOfExtractedField)
            throws FieldExtractionFieldOfIdException
    {
        final Set<Field> foundedFields = this.fieldsOfClassFounder.findFieldsOfType(classOfExtractedField);
        final Predicate<Field> predicateToBeId = (final Field field) ->
        {
            final Id idAnnotation = field.getAnnotation(Id.class);
            return idAnnotation != null;
        };
        final Optional<Field> optionalOfExtractedFieldOfId = foundedFields.stream().filter(predicateToBeId).findAny();
        if(optionalOfExtractedFieldOfId.isEmpty())
        {
            throw new FieldExtractionFieldOfIdException("Class '" + classOfExtractedField.getName() + "' doesn't have "
                    + "field annotated by '" + Id.class.getName() + "' annotation.");
        }
        return optionalOfExtractedFieldOfId.get();
    }
}
