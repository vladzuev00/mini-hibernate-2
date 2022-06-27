package by.zuevvlad.minihibernate.query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

public abstract class Query<TypeOfEntity> implements AutoCloseable
{
    private final Connection connection;
    private final String query;        //can be object-oriented query or sql query
    private Optional<? extends Statement> optionalOfStatement; //can be Statement, PreparedStatement, CallableStatement

    public Query(final Connection connection, final String query)
    {
        super();
        this.connection = connection;
        this.query = query;
        this.optionalOfStatement = Optional.empty();
    }

    protected final Connection getConnection()
    {
        return this.connection;
    }

    protected final String getQuery()
    {
        return this.query;
    }

    protected final void setStatement(final Statement statement)
    {
        this.optionalOfStatement = Optional.ofNullable(statement);
    }

    protected final Optional<? extends Statement> getOptionalOfStatement()
    {
        return this.optionalOfStatement;
    }

    @Override
    public final void close() throws SQLException
    {
        if(this.optionalOfStatement.isPresent())
        {
            final Statement statement = optionalOfStatement.get();
            statement.close();
        }
    }

    public abstract void executeUpdate();
    public abstract Collection<TypeOfEntity> executeSelect();
    public abstract Optional<TypeOfEntity> executeSelectForSingle();

    public abstract void setBigDecimal(final int parameterPosition, final BigDecimal parameter);
    public abstract void setBigInteger(final int parameterPosition, final BigInteger parameter);
    public abstract void setBinary(final int parameterPosition, final byte[] parameter);
    public abstract void setBoolean(final int parameterPosition, final boolean parameter);
    //public abstract void setByte(final int parameterPosition, final byte parameter);
}
