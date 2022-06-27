package by.zuevvlad.minihibernate.fieldsofclassfounder;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class FieldsOfClassFounder
{
    public static FieldsOfClassFounder createFieldsOfClassFounder()
    {
        if(FieldsOfClassFounder.fieldsOfClassFounder == null)
        {
            synchronized(FieldsOfClassFounder.class)
            {
                if(FieldsOfClassFounder.fieldsOfClassFounder == null)
                {
                    FieldsOfClassFounder.fieldsOfClassFounder = new FieldsOfClassFounder();
                }
            }
        }
        return FieldsOfClassFounder.fieldsOfClassFounder;
    }

    private static FieldsOfClassFounder fieldsOfClassFounder = null;

    private FieldsOfClassFounder()
    {
        super();
    }

    public final Set<Field> findFieldsOfType(final Class<?> type)
    {
        final Set<Field> fieldsOfType = new LinkedHashSet<Field>();
        Class<?> runnerType = type;
        Field[] fieldsOfCurrentRunnerType;
        while(runnerType != Object.class)
        {
            fieldsOfCurrentRunnerType = runnerType.getDeclaredFields();
            fieldsOfType.addAll(Arrays.asList(fieldsOfCurrentRunnerType));
            runnerType = runnerType.getSuperclass();
        }
        return fieldsOfType;
    }

    public final Set<Field> findFieldsOfTypeByPredicate(final Class<?> type, final Predicate<Field> predicate)
    {
        final Set<Field> fieldsOfType = this.findFieldsOfType(type);
        return fieldsOfType.stream().filter(predicate).collect(Collectors.toSet());
    }

    public final Optional<Field> findOptionalOfFieldByPredicate(final Class<?> type, final Predicate<Field> predicate)
    {
        final Set<Field> fieldsOfType = this.findFieldsOfType(type);
        return fieldsOfType.stream().filter(predicate).findAny();
    }
}