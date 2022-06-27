package by.zuevvlad.minihibernate.driverloader.exception;

public final class DriverLoadingException extends Exception
{
    public DriverLoadingException()
    {
        super();
    }

    public DriverLoadingException(final String description)
    {
        super(description);
    }

    public DriverLoadingException(final Exception cause)
    {
        super(cause);
    }

    public DriverLoadingException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
