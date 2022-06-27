package by.zuevvlad.minihibernate.session.exception;

public final class ResultSetMappingToCollectionException extends SessionException
{
    public ResultSetMappingToCollectionException()
    {
        super();
    }

    public ResultSetMappingToCollectionException(final String description)
    {
        super(description);
    }

    public ResultSetMappingToCollectionException(final Exception cause)
    {
        super(cause);
    }

    public ResultSetMappingToCollectionException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
