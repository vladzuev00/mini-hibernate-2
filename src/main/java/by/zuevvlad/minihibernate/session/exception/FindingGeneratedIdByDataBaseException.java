package by.zuevvlad.minihibernate.session.exception;

public final class FindingGeneratedIdByDataBaseException extends Exception
{
    public FindingGeneratedIdByDataBaseException()
    {
        super();
    }

    public FindingGeneratedIdByDataBaseException(final String description)
    {
        super(description);
    }

    public FindingGeneratedIdByDataBaseException(final Exception cause)
    {
        super(cause);
    }

    public FindingGeneratedIdByDataBaseException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
