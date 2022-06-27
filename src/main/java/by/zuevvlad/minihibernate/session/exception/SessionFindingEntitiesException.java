package by.zuevvlad.minihibernate.session.exception;

public final class SessionFindingEntitiesException extends Exception
{
    public SessionFindingEntitiesException()
    {
        super();
    }

    public SessionFindingEntitiesException(final String description)
    {
        super(description);
    }

    public SessionFindingEntitiesException(final Exception cause)
    {
        super(cause);
    }

    public SessionFindingEntitiesException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
