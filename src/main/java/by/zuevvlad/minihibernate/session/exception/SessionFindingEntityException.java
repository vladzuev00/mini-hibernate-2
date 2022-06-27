package by.zuevvlad.minihibernate.session.exception;

public final class SessionFindingEntityException extends SessionException
{
    public SessionFindingEntityException()
    {
        super();
    }

    public SessionFindingEntityException(final String description)
    {
        super(description);
    }

    public SessionFindingEntityException(final Exception cause)
    {
        super(cause);
    }

    public SessionFindingEntityException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
