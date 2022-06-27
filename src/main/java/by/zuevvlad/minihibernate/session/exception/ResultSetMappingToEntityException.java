package by.zuevvlad.minihibernate.session.exception;

public final class ResultSetMappingToEntityException extends RuntimeException
{
    public ResultSetMappingToEntityException()
    {
        super();
    }

    public ResultSetMappingToEntityException(final String description)
    {
        super(description);
    }

    public ResultSetMappingToEntityException(final Exception cause)
    {
        super(cause);
    }

    public ResultSetMappingToEntityException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
