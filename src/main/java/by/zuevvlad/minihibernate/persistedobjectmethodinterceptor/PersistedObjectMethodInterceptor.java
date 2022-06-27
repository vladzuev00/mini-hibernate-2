package by.zuevvlad.minihibernate.persistedobjectmethodinterceptor;

import by.zuevvlad.minihibernate.annotation.Column;
import by.zuevvlad.minihibernate.annotation.Table;
import by.zuevvlad.minihibernate.fieldextractorbyaccessmethod.FieldExtractor;
import by.zuevvlad.minihibernate.frameworkproperty.FrameworkProperty;
import by.zuevvlad.minihibernate.methodtypeidentifier.MethodTypeIdentifier;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

public final class PersistedObjectMethodInterceptor implements MethodInterceptor
{
    private final Connection connection;
    private final MethodTypeIdentifier methodTypeIdentifier;
    private final FieldExtractor fieldExtractor;
    private final Logger logger;

    public PersistedObjectMethodInterceptor(final Connection connection)
    {
        super();
        this.connection = connection;
        this.methodTypeIdentifier = MethodTypeIdentifier.createMethodTypeIdentifier();
        this.fieldExtractor = FieldExtractor.createFieldExtractor();
        this.logger = Logger.getLogger(PersistedObjectMethodInterceptor.class.getName());
    }

    @Override
    public final Object intercept(final Object proxiedObject, final Method interceptedMethod,
                                  final Object[] methodArguments, final MethodProxy methodProxy)
            throws Throwable
    {
        if(this.methodTypeIdentifier.isSetterMethod(interceptedMethod))
        {
            final Class<?> declaringClassOfMethod = interceptedMethod.getDeclaringClass();
            final Table tableAnnotationOfDeclaringClass = declaringClassOfMethod.getAnnotation(Table.class);
            final String nameOfTableOfUpdatedColumn = tableAnnotationOfDeclaringClass.name();

            final Field updatedField = this.fieldExtractor.extractFieldBySetterMethod(interceptedMethod);
            final Column columnAnnotationOfUpdatedField = updatedField.getAnnotation(Column.class);
            final String nameOfUpdatedColumn = columnAnnotationOfUpdatedField.name();

            final Object newValueOfUpdatedColumn = methodArguments[PersistedObjectMethodInterceptor
                    .INDEX_OF_NEW_VALUE_OF_UPDATED_COLUMN_IN_ARGUMENTS];

            final Field fieldOfIdOfClassOfUpdatedField = this.fieldExtractor.extractFieldOfId(
                    declaringClassOfMethod);
            final Column columnAnnotationOfFieldOfIdOfClassOfUpdatedField = fieldOfIdOfClassOfUpdatedField
                    .getAnnotation(Column.class);
            final String nameOfColumnOfIdOfTableOfUpdatedColumn = columnAnnotationOfFieldOfIdOfClassOfUpdatedField
                    .name();

            fieldOfIdOfClassOfUpdatedField.setAccessible(true);
            final long idOfUpdatedRow;
            try
            {
                idOfUpdatedRow = (long)fieldOfIdOfClassOfUpdatedField.get(proxiedObject);       //сделать через get-р
            }
            finally
            {
                fieldOfIdOfClassOfUpdatedField.setAccessible(false);
            }

            final String sqlQueryToUpdateColumn = String.format(
                    PersistedObjectMethodInterceptor.TEMPLATE_OF_PREPARED_STATEMENT_TO_UPDATE_COLUMN,
                    nameOfTableOfUpdatedColumn, nameOfUpdatedColumn, nameOfColumnOfIdOfTableOfUpdatedColumn);

            try(final PreparedStatement preparedStatement = this.connection.prepareStatement(sqlQueryToUpdateColumn))
            {
                preparedStatement.setObject(
                        PersistedObjectMethodInterceptor.PARAMETER_INDEX_OF_NEW_VALUE_OF_UPDATED_COLUMN,
                        newValueOfUpdatedColumn);
                preparedStatement.setLong(
                        PersistedObjectMethodInterceptor.PARAMETER_INDEX_OF_VALUE_OF_ID_OF_UPDATED_ROW,
                        idOfUpdatedRow);

                final String loggerInfoMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                        + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue() + sqlQueryToUpdateColumn;
                this.logger.info(loggerInfoMessage);
                preparedStatement.executeUpdate();
            }
        }
        return methodProxy.invokeSuper(proxiedObject, methodArguments);
    }

    private static final int INDEX_OF_NEW_VALUE_OF_UPDATED_COLUMN_IN_ARGUMENTS = 0;
    /*
        First %s - name of table of updated column
        Second %s - name of updated column
        Third %s - name of column of id
     */
    private static final String TEMPLATE_OF_PREPARED_STATEMENT_TO_UPDATE_COLUMN = "UPDATE %s SET %s = ? WHERE %s = ?";
    private static final int PARAMETER_INDEX_OF_NEW_VALUE_OF_UPDATED_COLUMN = 1;
    private static final int PARAMETER_INDEX_OF_VALUE_OF_ID_OF_UPDATED_ROW = 2;
}
