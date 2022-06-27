package by.zuevvlad.minihibernate.session;

import by.zuevvlad.minihibernate.accessmethodextractor.AccessMethodExtractor;
import by.zuevvlad.minihibernate.accessmethodextractor.exception.ExtractionAccessMethodException;
import by.zuevvlad.minihibernate.annotation.AutoIncrement;
import by.zuevvlad.minihibernate.annotation.Column;
import by.zuevvlad.minihibernate.annotation.Id;
import by.zuevvlad.minihibernate.annotation.Table;
import by.zuevvlad.minihibernate.databaseconnectionpool.DataBaseConnectionPool;
import by.zuevvlad.minihibernate.fieldsofclassfounder.FieldsOfClassFounder;
import by.zuevvlad.minihibernate.frameworkproperty.FrameworkProperty;
import by.zuevvlad.minihibernate.persistedobjectmethodinterceptor.PersistedObjectMethodInterceptor;
import by.zuevvlad.minihibernate.session.exception.*;
import net.sf.cglib.proxy.Enhancer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Session implements AutoCloseable
{
    private final Connection connection;
    private final DataBaseConnectionPool dataBaseConnectionPool;
    private final Logger logger;
    private final SQLQueryCreator sqlQueryCreator;
    private final ResultSetMapperToProxyCollection resultSetMapperToProxyCollection;
    private final ResultSetMapperToProxyEntity resultSetMapperToProxyEntity;
    private final FieldsOfClassFounder fieldsOfClassFounder;
    private final AccessMethodExtractor accessMethodExtractor;
    private final FieldsIntoPreparedStatementBinder fieldsIntoPreparedStatementBinder;
    private final FounderGeneratedIdByDataBase founderGeneratedIdByDataBase;

    public Session(final Connection connection, final DataBaseConnectionPool dataBaseConnectionPool)
    {
        super();
        this.connection = connection;
        this.dataBaseConnectionPool = dataBaseConnectionPool;
        this.logger = Logger.getLogger(Session.class.getName());
        this.sqlQueryCreator = SQLQueryCreator.createSQLQueryCreator();
        this.resultSetMapperToProxyCollection = new ResultSetMapperToProxyCollection();
        this.resultSetMapperToProxyEntity = new ResultSetMapperToProxyEntity();
        this.fieldsOfClassFounder = FieldsOfClassFounder.createFieldsOfClassFounder();
        this.accessMethodExtractor = AccessMethodExtractor.createAccessMethodExtractor();
        this.fieldsIntoPreparedStatementBinder = new FieldsIntoPreparedStatementBinder();
        this.founderGeneratedIdByDataBase = FounderGeneratedIdByDataBase.createFounderGeneratedIdByDataBase();
    }

    public final <TypeOfAddedEntity> void addEntity(final TypeOfAddedEntity addedEntity)
            throws SessionAddingEntityException
    {
        try
        {
            final Class<?> typeOfAddedEntity = addedEntity.getClass();
            final String sqlQueryToAddEntity = this.sqlQueryCreator.createAddEntityQuery(typeOfAddedEntity);
            try(final PreparedStatement preparedStatement = this.connection.prepareStatement(sqlQueryToAddEntity,
                    Statement.RETURN_GENERATED_KEYS))
            {
                final Set<Field> fieldsOfTypeOfAddedEntityExceptAutoincrementFields = this.fieldsOfClassFounder
                        .findFieldsOfTypeByPredicate(typeOfAddedEntity, Session.PREDICATE_TO_BE_NOT_AUTOINCREMENT_FIELD);
                this.fieldsIntoPreparedStatementBinder.bind(fieldsOfTypeOfAddedEntityExceptAutoincrementFields,
                        preparedStatement, addedEntity);

                this.logger.info(FrameworkProperty.FRAMEWORK_NAME.getValue()
                        + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                        + sqlQueryToAddEntity);
                preparedStatement.executeUpdate();

                final Optional<Field> optionalOfFieldOfId = this.fieldsOfClassFounder.findOptionalOfFieldByPredicate(
                        typeOfAddedEntity, Session.PREDICATE_TO_BY_ID_FIELD);
                if(optionalOfFieldOfId.isPresent())
                {
                    final Field fieldOfId = optionalOfFieldOfId.get();
                    final Column columnAnnotationOfFieldOfId = fieldOfId.getAnnotation(Column.class);
                    final String nameOfColumnOfId = columnAnnotationOfFieldOfId.name();
                    final Object generatedId = this.founderGeneratedIdByDataBase.findGeneratedIdInLastInserting(
                            preparedStatement, nameOfColumnOfId);
                    fieldOfId.setAccessible(true);
                    try
                    {
                        fieldOfId.set(addedEntity, generatedId);
                    }
                    finally
                    {
                        fieldOfId.setAccessible(false);
                    }
                }
            }
        }
        catch(final SQLException | FieldsIntoPreparedStatementBindingException
                | FindingGeneratedIdByDataBaseException | IllegalAccessException cause)
        {
            throw new SessionAddingEntityException(cause);
        }
    }

    private static final Predicate<Field> PREDICATE_TO_BE_AUTOINCREMENT_FIELD = (final Field researchField) ->
    {
        final AutoIncrement autoIncrementAnnotation = researchField.getAnnotation(AutoIncrement.class);
        return autoIncrementAnnotation != null;
    };
    private static final Predicate<Field> PREDICATE_TO_BE_NOT_AUTOINCREMENT_FIELD
            = Session.PREDICATE_TO_BE_AUTOINCREMENT_FIELD.negate();

    private static final Predicate<Field> PREDICATE_TO_BY_ID_FIELD = (final Field researchField) ->
    {
        final Id idAnnotation = researchField.getAnnotation(Id.class);
        return idAnnotation != null;
    };

    public final <TypeOfFoundEntity> Collection<TypeOfFoundEntity> findAllEntities(
            final Class<TypeOfFoundEntity> typeOfFoundEntity)
            throws SessionFindingEntitiesException
    {
        final String sqlQueryToFindAllEntities = this.sqlQueryCreator.createFindAllEntitiesQuery(typeOfFoundEntity);
        try(final Statement statement = this.connection.createStatement())
        {
            final String loggerInfoMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                    + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                    + sqlQueryToFindAllEntities;
            this.logger.info(loggerInfoMessage);

            final ResultSet resultSet = statement.executeQuery(sqlQueryToFindAllEntities);
            return this.resultSetMapperToProxyCollection.map(resultSet, typeOfFoundEntity);
        }
        catch(final SQLException | ResultSetMappingToCollectionException cause)
        {
            throw new SessionFindingEntitiesException(cause);
        }
    }

    public final <TypeOfFoundEntity, TypeOfId> Optional<TypeOfFoundEntity> findEntityById(
            final TypeOfId id, final Class<TypeOfFoundEntity> typeOfFoundEntity)
            throws SessionFindingEntityException
    {
        final String sqlQueryToFindEntityById = this.sqlQueryCreator.createFindEntityByIdQuery(typeOfFoundEntity);
        try(final PreparedStatement preparedStatement = this.connection.prepareStatement(sqlQueryToFindEntityById))
        {
            preparedStatement.setObject(Session.PARAMETER_INDEX_OF_ID_TO_FIND_ENTITY_BY_ID_QUERY, id);

            final String loggerInfoMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                    + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                    + sqlQueryToFindEntityById;
            this.logger.info(loggerInfoMessage);

            final ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next()
                    ? Optional.of(this.resultSetMapperToProxyEntity.map(resultSet, typeOfFoundEntity))
                    : Optional.empty();
        }
        catch(final SQLException cause)
        {
            throw new SessionFindingEntityException(cause);
        }
    }

    private static final int PARAMETER_INDEX_OF_ID_TO_FIND_ENTITY_BY_ID_QUERY = 1;

    public final <TypeOfId, TypeOfDeletedEntity> void deleteEntityById(final TypeOfId idOfDeletedEntity,
                                                                       final Class<TypeOfDeletedEntity> typeOfDeletedEntity)
            throws SessionDeletingEntityException
    {
        final String sqlQueryToDeleteEntityById = this.sqlQueryCreator.createDeleteEntityByIdQuery(typeOfDeletedEntity);
        try(final PreparedStatement preparedStatement = this.connection.prepareStatement(sqlQueryToDeleteEntityById))
        {
            preparedStatement.setObject(Session.PARAMETER_INDEX_OF_ID_TO_DELETE_ENTITY_BY_ID_QUERY, idOfDeletedEntity);

            final String loggerInfoMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                    + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                    + sqlQueryToDeleteEntityById;
            this.logger.info(loggerInfoMessage);

            preparedStatement.executeUpdate();
        }
        catch(final SQLException cause)
        {
            throw new SessionDeletingEntityException(cause);
        }
    }

    private static final int PARAMETER_INDEX_OF_ID_TO_DELETE_ENTITY_BY_ID_QUERY = 1;

    public final void beginTransaction()
            throws SessionTransactionManagingException
    {
        try
        {
            final String loggerInfoMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                    + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                    + Session.MESSAGE_OF_STARTING_TRANSACTION;
            this.logger.info(loggerInfoMessage);

            this.connection.setAutoCommit(false);
        }
        catch(final SQLException cause)
        {
            throw new SessionTransactionManagingException(cause);
        }
    }

    private static final String MESSAGE_OF_STARTING_TRANSACTION = "transaction was began";

    public final void commit()
            throws SessionTransactionManagingException
    {
        try
        {
            final String loggerInfoMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                    + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                    + Session.MESSAGE_OF_COMMIT_TRANSACTION;
            this.logger.info(loggerInfoMessage);

            this.connection.commit();
            this.connection.setAutoCommit(true);
        }
        catch(final SQLException cause)
        {
            throw new SessionTransactionManagingException(cause);
        }
    }

    private static final String MESSAGE_OF_COMMIT_TRANSACTION = "transaction was committed";

    public final void rollback()
            throws SessionTransactionManagingException
    {
        try
        {
            final String loggerInfoMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                    + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                    + Session.MESSAGE_OF_ROLLBACK_TRANSACTION;
            this.logger.info(loggerInfoMessage);

            this.connection.rollback();
            this.connection.setAutoCommit(true);
        }
        catch(final SQLException cause)
        {
            throw new SessionTransactionManagingException(cause);
        }
    }

    private static final String MESSAGE_OF_ROLLBACK_TRANSACTION = "transaction was canceled";

    @Override
    public final void close()
            throws SessionClosingException
    {
        try
        {
            if(!this.connection.getAutoCommit())
            {
                this.connection.setAutoCommit(true);
            }
            this.dataBaseConnectionPool.returnConnectionToPool(this.connection);
        }
        catch(final SQLException cause)
        {
            throw new SessionClosingException(cause);
        }
    }

    private static final class SQLQueryCreator
    {
        private final FieldsOfClassFounder fieldsOfClassFounder;

        public static SQLQueryCreator createSQLQueryCreator()
        {
            if(SQLQueryCreator.sqlQueryCreator == null)
            {
                synchronized(SQLQueryCreator.class)
                {
                    if(SQLQueryCreator.sqlQueryCreator == null)
                    {
                        final FieldsOfClassFounder fieldsOfClassFounder = FieldsOfClassFounder
                                .createFieldsOfClassFounder();
                        SQLQueryCreator.sqlQueryCreator = new SQLQueryCreator(fieldsOfClassFounder);
                    }
                }
            }
            return SQLQueryCreator.sqlQueryCreator;
        }

        private static SQLQueryCreator sqlQueryCreator = null;

        private SQLQueryCreator(final FieldsOfClassFounder fieldsOfClassFounder)
        {
            super();
            this.fieldsOfClassFounder = fieldsOfClassFounder;
        }

        public final String createAddEntityQuery(final Class<?> typeOfAddedEntity)
        {
            final Table tableAnnotation = typeOfAddedEntity.getAnnotation(Table.class);
            final String nameOfTable = tableAnnotation.name();

            final Set<Field> fieldsOfTypeOfAddedEntity = this.fieldsOfClassFounder.findFieldsOfType(typeOfAddedEntity);
            final Set<Field> fieldsOfTypeOfAddedEntityExceptAutoincrementFields = fieldsOfTypeOfAddedEntity.stream()
                    .filter((final Field researchField) ->
                    {
                        final AutoIncrement autoIncrementAnnotation = researchField.getAnnotation(AutoIncrement.class);
                        return autoIncrementAnnotation == null;
                    }).collect(Collectors.toSet());

            final String namesOfColumnsSeparatedBySeparator = this.findNamesOfColumnsSeparatedBySeparator(
                    fieldsOfTypeOfAddedEntityExceptAutoincrementFields);

            final int amountOfPreparedStatementParameters = fieldsOfTypeOfAddedEntityExceptAutoincrementFields.size();
            final String preparedStatementParametersSeparatedBySeparator
                    = this.createStringOfPreparedStatementParametersSeparatedBySeparator(
                            amountOfPreparedStatementParameters);

            return String.format(SQLQueryCreator.TEMPLATE_OF_SQL_QUERY_TO_ADD_ENTITY, nameOfTable,
                    namesOfColumnsSeparatedBySeparator, preparedStatementParametersSeparatedBySeparator);
        }

        private String createStringOfPreparedStatementParametersSeparatedBySeparator(final int amountOfParameters)
        {
            final StringBuilder result = new StringBuilder();
            int runnerIndex = 0;
            while(runnerIndex < amountOfParameters)
            {
                result.append(SQLQueryCreator.DESCRIPTION_OF_PARAMETER_OF_PREPARED_STATEMENT);
                if(runnerIndex != amountOfParameters - 1)
                {
                    result.append(SQLQueryCreator.SEPARATOR_OF_PARAMETERS_IN_PREPARED_STATEMENT);
                }
                runnerIndex++;
            }
            return result.toString();
        }

        private static final String DESCRIPTION_OF_PARAMETER_OF_PREPARED_STATEMENT = "?";
        private static final String SEPARATOR_OF_PARAMETERS_IN_PREPARED_STATEMENT = ", ";

        /*
            First %s - name of table
            Second %s - name of columns separated by separator
            Third %s - parameters('?') of prepared statement
         */
        private static final String TEMPLATE_OF_SQL_QUERY_TO_ADD_ENTITY = "INSERT INTO %s(%s) VALUES(%s)";

        public final String createFindAllEntitiesQuery(final Class<?> typeOfFoundEntity)
        {
            final Table tableAnnotation = typeOfFoundEntity.getAnnotation(Table.class);
            final String nameOfTable = tableAnnotation.name();

            final Set<Field> fieldsOfTypeOfFoundEntity = this.fieldsOfClassFounder.findFieldsOfType(typeOfFoundEntity);
            final String namesOfColumnsSeparatedBySeparator = this.findNamesOfColumnsSeparatedBySeparator(
                    fieldsOfTypeOfFoundEntity);

            return String.format(SQLQueryCreator.TEMPLATE_OF_SQL_QUERY_TO_FIND_ALL_ENTITIES,
                    namesOfColumnsSeparatedBySeparator, nameOfTable);
        }

        private String findNamesOfColumnsSeparatedBySeparator(final Set<Field> fieldsOfType)
        {
            final StringBuilder namesOfColumnsSeparatedBySeparator = fieldsOfType.stream()
                    .map((final Field field) ->
                    {
                        final Column columnAnnotation = field.getAnnotation(Column.class);
                        final String nameOfColumn = columnAnnotation.name();
                        return new StringBuilder(nameOfColumn);
                    }).reduce(new StringBuilder(), (final StringBuilder accumulator, final StringBuilder appended) ->
                    {
                        accumulator.append(appended);
                        accumulator.append(SQLQueryCreator.SEPARATOR_OF_NAMES_OF_COLUMNS);
                        return accumulator;
                    });
            namesOfColumnsSeparatedBySeparator.delete(namesOfColumnsSeparatedBySeparator.length() - 2,
                    namesOfColumnsSeparatedBySeparator.length());
            return namesOfColumnsSeparatedBySeparator.toString();
        }

        private static final String SEPARATOR_OF_NAMES_OF_COLUMNS = ", ";
        /*
            First %s - names of columns separated by separator
            Second %s - name of table
         */
        private static final String TEMPLATE_OF_SQL_QUERY_TO_FIND_ALL_ENTITIES = "SELECT %s FROM %s";

        public final String createFindEntityByIdQuery(final Class<?> typeOfFoundEntity)
        {
            final Table tableAnnotation = typeOfFoundEntity.getAnnotation(Table.class);
            final String nameOfTable = tableAnnotation.name();

            final Set<Field> fieldsOfTypeOfFoundEntity = this.fieldsOfClassFounder.findFieldsOfType(typeOfFoundEntity);
            final String namesOfColumnsSeparatedBySeparator = this.findNamesOfColumnsSeparatedBySeparator(
                    fieldsOfTypeOfFoundEntity);

            final Field fieldOfId = fieldsOfTypeOfFoundEntity.stream().filter((final Field field) ->
            {
                final Id idAnnotation = field.getAnnotation(Id.class);
                return idAnnotation != null;
            }).findAny().orElseThrow(() ->
            {
                return new NoIdFieldException("Field annotated by '" + Id.class.getName() + "' wasn't found in class '"
                        + typeOfFoundEntity.getName() + "'.");
            });
            final Column columnAnnotation = fieldOfId.getAnnotation(Column.class);
            final String nameOfColumnOfId = columnAnnotation.name();

            return String.format(SQLQueryCreator.TEMPLATE_OF_SQL_QUERY_TO_FIND_ENTITY_BY_ID,
                    namesOfColumnsSeparatedBySeparator, nameOfTable, nameOfColumnOfId);
        }

        /*
            First %s - names of columns separated by separator
            Second %s - name of table
            Third %s - name of column of id
         */
        private static final String TEMPLATE_OF_SQL_QUERY_TO_FIND_ENTITY_BY_ID = "SELECT %S FROM %s WHERE %s = ?";

        public final String createDeleteEntityByIdQuery(final Class<?> typeOfDeletedEntity)
        {
            final Table tableAnnotation = typeOfDeletedEntity.getAnnotation(Table.class);
            final String nameOfTable = tableAnnotation.name();

            final Set<Field> fieldsOfTypeOfDeletedEntity = this.fieldsOfClassFounder.findFieldsOfType(
                    typeOfDeletedEntity);
            final Field fieldOfId = fieldsOfTypeOfDeletedEntity.stream().filter((final Field researchField) ->
            {
                final Id idAnnotation = researchField.getAnnotation(Id.class);
                return idAnnotation != null;
            }).findAny().orElseThrow(() ->
            {
                return new NoIdFieldException("Field annotated by '" + Id.class.getName() + "' wasn't found in class '"
                        + typeOfDeletedEntity.getName() + "'.");
            });
            final Column columnAnnotation = fieldOfId.getAnnotation(Column.class);
            final String nameOfColumnOfId = columnAnnotation.name();

            return String.format(SQLQueryCreator.TEMPLATE_OF_SQL_QUERY_TO_DELETE_ENTITY_BY_ID, nameOfTable,
                    nameOfColumnOfId);
        }

        /*
            First %s - name of table
            Second %s - name of column of id
         */
        private static final String TEMPLATE_OF_SQL_QUERY_TO_DELETE_ENTITY_BY_ID = "DELETE FROM %s WHERE %s = ?";
    }

    private final class ResultSetMapperToProxyCollection
    {
        private final ResultSetMapperToProxyEntity resultSetMapperToProxyEntity;

        public ResultSetMapperToProxyCollection()
        {
            super();
            this.resultSetMapperToProxyEntity = new ResultSetMapperToProxyEntity();
        }

        public final <TypeOfProxiedEntity> Collection<TypeOfProxiedEntity> map(final ResultSet resultSet,
                                                                               final Class<TypeOfProxiedEntity> classOfProxiedEntity)
                throws ResultSetMappingToCollectionException
        {
            try
            {
                final Collection<TypeOfProxiedEntity> mappedProxyEntities = new LinkedHashSet<TypeOfProxiedEntity>();
                TypeOfProxiedEntity mappedProxyEntity;
                while(resultSet.next())
                {
                    mappedProxyEntity = this.resultSetMapperToProxyEntity.map(resultSet, classOfProxiedEntity);
                    mappedProxyEntities.add(mappedProxyEntity);
                }
                return mappedProxyEntities;
            }
            catch(final SQLException cause)
            {
                throw new ResultSetMappingToCollectionException(cause);
            }
        }
    }

    private final class ResultEntityProxyCreator
    {
        public ResultEntityProxyCreator()
        {
            super();
        }

        public final <TypeOfProxiedEntity> TypeOfProxiedEntity createProxy(
                final Class<TypeOfProxiedEntity> classOfProxiedEntity)
        {
            final Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(classOfProxiedEntity);
            enhancer.setCallback(new PersistedObjectMethodInterceptor(Session.this.connection));
            return classOfProxiedEntity.cast(enhancer.create());
        }
    }

    private final class ResultSetMapperToProxyEntity
    {
        private final ResultEntityProxyCreator resultEntityProxyCreator;
        private final FieldsOfClassFounder fieldsOfClassFounder;

        public ResultSetMapperToProxyEntity()
        {
            super();
            this.resultEntityProxyCreator = new ResultEntityProxyCreator();
            this.fieldsOfClassFounder = FieldsOfClassFounder.createFieldsOfClassFounder();
        }

        public final <TypeOfProxiedEntity> TypeOfProxiedEntity map(final ResultSet resultSet,
                                                                   final Class<TypeOfProxiedEntity> classOfProxiedEntity)
        {
            final TypeOfProxiedEntity resultProxyEntity = this.resultEntityProxyCreator.createProxy(
                    classOfProxiedEntity);
            final Set<Field> fieldsOfClassOfEntity = this.fieldsOfClassFounder.findFieldsOfType(
                    classOfProxiedEntity);
            fieldsOfClassOfEntity.forEach((final Field field) ->
            {
                try
                {
                    final Column columnAnnotation = field.getAnnotation(Column.class);
                    final String nameOfColumn = columnAnnotation.name();
                    final Object valueOfField = resultSet.getObject(nameOfColumn);
                    field.setAccessible(true);
                    try
                    {
                        field.set(resultProxyEntity, valueOfField);
                    }
                    finally
                    {
                        field.setAccessible(false);
                    }
                }
                catch(final SQLException  | IllegalAccessException cause)
                {
                    throw new ResultSetMappingToEntityException(cause);
                }
            });
            return resultProxyEntity;
        }
    }

    private final class FieldsIntoPreparedStatementBinder
    {
        public final <TypeOfEntity> void bind(final Set<Field> fields, final PreparedStatement preparedStatement,
                                              final TypeOfEntity entity)
                throws FieldsIntoPreparedStatementBindingException
        {
            try
            {
                final Map<Field, Optional<Method>> mapOfFieldOfEntityToOptionalOfGetterMethod
                        = this.findMapOfFieldOfEntityToOptionalOfGetterMethod(fields);

                int runnerParameterIndex = 1;
                for(final Map.Entry<Field, Optional<Method>> fieldToGetterMethod
                        : mapOfFieldOfEntityToOptionalOfGetterMethod.entrySet())
                {
                    final Field field = fieldToGetterMethod.getKey();
                    final Optional<Method> optionalOfGetterMethod = fieldToGetterMethod.getValue();
                    final Object fieldValue;
                    if (optionalOfGetterMethod.isPresent())
                    {
                        final Method getterMethod = optionalOfGetterMethod.get();
                        fieldValue = getterMethod.invoke(entity);
                    }
                    else
                    {
                        field.setAccessible(true);
                        try
                        {
                            fieldValue = field.get(entity);
                        } finally
                        {
                            field.setAccessible(false);
                        }
                    }
                    preparedStatement.setObject(runnerParameterIndex, fieldValue);
                    runnerParameterIndex++;
                }
            }
            catch(final SQLException | IllegalAccessException | InvocationTargetException cause)
            {
                throw new FieldsIntoPreparedStatementBindingException(cause);
            }
        }

        private Map<Field, Optional<Method>> findMapOfFieldOfEntityToOptionalOfGetterMethod(final Set<Field> fields)
        {
            return fields.stream().collect(Collectors.toMap(Function.identity(), (final Field field) ->
            {
                try
                {
                    return Optional.of(Session.this.accessMethodExtractor.extractGetterMethod(field));
                }
                catch(final ExtractionAccessMethodException extractionGetterMethodException)
                {
                    final String loggerWarnMessage = FrameworkProperty.FRAMEWORK_NAME.getValue()
                            + FrameworkProperty.SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE.getValue()
                            + extractionGetterMethodException.getMessage();
                    Session.this.logger.warning(loggerWarnMessage);

                    return Optional.empty();        //In this case value of field is got directly from field, not from getter method
                }
            }));
        }
    }

    private static final class FounderGeneratedIdByDataBase
    {
        public static FounderGeneratedIdByDataBase createFounderGeneratedIdByDataBase()
        {
            if(FounderGeneratedIdByDataBase.founderGeneratedIdByDataBase == null)
            {
                synchronized(FounderGeneratedIdByDataBase.class)
                {
                    if(FounderGeneratedIdByDataBase.founderGeneratedIdByDataBase == null)
                    {
                        FounderGeneratedIdByDataBase.founderGeneratedIdByDataBase = new FounderGeneratedIdByDataBase();
                    }
                }
            }
            return FounderGeneratedIdByDataBase.founderGeneratedIdByDataBase;
        }

        private static FounderGeneratedIdByDataBase founderGeneratedIdByDataBase = null;

        private FounderGeneratedIdByDataBase()
        {
            super();
        }

        public final Object findGeneratedIdInLastInserting(final Statement insertingStatement,
                                                           final String nameOfColumnOfId)
                throws FindingGeneratedIdByDataBaseException
        {
            try(final ResultSet resultSetOfGeneratedKeys = insertingStatement.getGeneratedKeys())
            {
                resultSetOfGeneratedKeys.next();
                return resultSetOfGeneratedKeys.getObject(nameOfColumnOfId);
            }
            catch(final SQLException cause)
            {
                throw new FindingGeneratedIdByDataBaseException(cause);
            }
        }
    }
}
