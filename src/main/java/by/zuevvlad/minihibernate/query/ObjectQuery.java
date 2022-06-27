package by.zuevvlad.minihibernate.query;

import java.sql.Connection;
import java.util.Collection;
import java.util.Optional;

public final class ObjectQuery<TypeOfEntity> extends Query<TypeOfEntity>
{
    public ObjectQuery(final Connection connection, final String sqlQuery)
    {
        super(connection, sqlQuery);
    }

    @Override
    public final void executeUpdate()
    {

    }

    //FROM Person, Where 'Person' name in Entity annotation
    @Override
    public final Collection<TypeOfEntity> executeSelect()
    {
        return null;
    }

    @Override
    public final Optional<TypeOfEntity> executeSelectForSingle()
    {
        return Optional.empty();
    }
}
