package by.zuevvlad.minihibernate.session.exception;

public class SessionException extends Exception
{
    public SessionException()
    {
        super();
    }

    public SessionException(final String description)
    {
        super(description);
    }

    public SessionException(final Exception cause)
    {
        super(cause);
    }

    public SessionException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
