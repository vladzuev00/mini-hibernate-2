package by.zuevvlad.minihibernate.session.exception;

public final class SessionDeletingEntityException extends SessionException
{
    public SessionDeletingEntityException()
    {
        super();
    }

    public SessionDeletingEntityException(final String description)
    {
        super(description);
    }

    public SessionDeletingEntityException(final Exception cause)
    {
        super(cause);
    }

    public SessionDeletingEntityException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
