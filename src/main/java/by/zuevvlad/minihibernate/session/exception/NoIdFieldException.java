package by.zuevvlad.minihibernate.session.exception;

public final class NoIdFieldException extends RuntimeException
{
    public NoIdFieldException()
    {
        super();
    }

    public NoIdFieldException(final String description)
    {
        super(description);
    }

    public NoIdFieldException(final Exception cause)
    {
        super(cause);
    }

    public NoIdFieldException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
