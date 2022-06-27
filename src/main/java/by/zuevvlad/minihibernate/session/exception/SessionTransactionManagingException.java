package by.zuevvlad.minihibernate.session.exception;

public final class SessionTransactionManagingException extends SessionException
{
    public SessionTransactionManagingException()
    {
        super();
    }

    public SessionTransactionManagingException(final String description)
    {
        super(description);
    }

    public SessionTransactionManagingException(final Exception cause)
    {
        super(cause);
    }

    public SessionTransactionManagingException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
