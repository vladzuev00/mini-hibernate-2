package by.zuevvlad.minihibernate.querytransformer;

import by.zuevvlad.minihibernate.annotation.Column;
import by.zuevvlad.minihibernate.annotation.Table;
import by.zuevvlad.minihibernate.fieldsofclassfounder.FieldsOfClassFounder;

import java.lang.reflect.Field;
import java.util.Set;

public final class ObjectQueryToSqlTransformer implements QueryTransformer
{
    private final FieldsOfClassFounder fieldsOfClassFounder;

    private ObjectQueryToSqlTransformer(final FieldsOfClassFounder fieldsOfClassFounder)
    {
        super();
        this.fieldsOfClassFounder = FieldsOfClassFounder.createFieldsOfClassFounder();
    }

    //FROM Person -> SELECT id, name, surname, patronymic, email, age FROM persons
    @Override
    public final String transform(final String transformedObjectQuery)
    {
        final String nameOfEntity = this.findNameOfEntity(transformedObjectQuery);
        final Class<?> classOfEntity = null;  //TODO: достать из конфигурации класс по имени сущности

        final Table tableAnnotation = classOfEntity.getAnnotation(Table.class);
        final String nameOfTable = tableAnnotation.name();

        final Set<Field> fieldsOfClass = this.fieldsOfClassFounder.findFieldsOfType(classOfEntity);
        final String separatedNamesOfAssociatedColumns = this.findSeparatedNamesOfAssociatedColumns(fieldsOfClass);


    }

    private String findNameOfEntity(final String transformedObjectQuery)
    {
        final String transformedObjectQueryWithoutExtraSpace = transformedObjectQuery.replaceAll(
            ObjectQueryToSqlTransformer.REGEX_OF_TWO_OR_MORE_SPACES,
            Character.toString(ObjectQueryToSqlTransformer.SPACE_CHARACTER));
        final int indexOfFromAnalogStart = transformedObjectQueryWithoutExtraSpace.indexOf(
                ObjectQueryToSqlTransformer.FROM_ANALOG_IN_OBJECT_QUERY);
        final int lengthOfFromAnalog = ObjectQueryToSqlTransformer.FROM_ANALOG_IN_OBJECT_QUERY.length();
        final int indexOfFromAnalogEnd = indexOfFromAnalogStart + lengthOfFromAnalog - 1;
        final int indexOfEntityNameStart = indexOfFromAnalogEnd + 2;
        final int indexOfEntityNameEnd = this.findIndexOfNextSpace(transformedObjectQueryWithoutExtraSpace,
                indexOfEntityNameStart) - 1;
        return transformedObjectQuery.substring(indexOfEntityNameStart, indexOfEntityNameEnd + 1);
    }

    private static final String REGEX_OF_TWO_OR_MORE_SPACES = " {2,}";
    private static final char SPACE_CHARACTER = ' ';
    private static final String FROM_ANALOG_IN_OBJECT_QUERY = "FROM";    //TODO: выделить все аналоги в отдельный класс

    private int findIndexOfNextSpace(final String researchString, final int fromIndex)
    {
        int runnerIndex = fromIndex + 1;
        while(researchString.charAt(runnerIndex) != ObjectQueryToSqlTransformer.SPACE_CHARACTER)
        {
            runnerIndex++;
        }
        return runnerIndex;
    }

    private String findSeparatedNamesOfAssociatedColumns(final Set<Field> fields)
    {
        final StringBuilder separatedNamesOfAssociatedColumns = fields.stream().map((final Field field) ->
        {
            final Column columnAnnotation = field.getAnnotation(Column.class);
            final String nameOfAssociatedColumn = columnAnnotation.name();
            return new StringBuilder(nameOfAssociatedColumn);
        }).reduce(new StringBuilder(), (final StringBuilder accumulator, final StringBuilder appended) ->
        {
            accumulator.append(appended);
            accumulator.append(ObjectQueryToSqlTransformer.SEPARATOR_OF_NAMES_OF_COLUMNS);
            return accumulator;
        });
        this.deleteLastSeparator(separatedNamesOfAssociatedColumns);
        return separatedNamesOfAssociatedColumns.toString();
    }

    private static final String SEPARATOR_OF_NAMES_OF_COLUMNS = ", ";

    private void deleteLastSeparator(final StringBuilder source)
    {
        source.delete(source.length() - ObjectQueryToSqlTransformer.SEPARATOR_OF_NAMES_OF_COLUMNS.length() + 1,
                source.length());
    }

    private static final String TEMPLATE_OF_
}
