package by.zuevvlad.minihibernate.session.exception;

public final class SessionAddingEntityException extends SessionException
{
    public SessionAddingEntityException()
    {
        super();
    }

    public SessionAddingEntityException(final String description)
    {
        super(description);
    }

    public SessionAddingEntityException(final Exception cause)
    {
        super(cause);
    }

    public SessionAddingEntityException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
