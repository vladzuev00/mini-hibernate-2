package by.zuevvlad.minihibernate.session.exception;

public final class SessionClosingException extends SessionException
{
    public SessionClosingException()
    {
        super();
    }

    public SessionClosingException(final String description)
    {
        super(description);
    }

    public SessionClosingException(final Exception cause)
    {
        super(cause);
    }

    public SessionClosingException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
