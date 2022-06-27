package by.zuevvlad.minihibernate.accessmethodextractor.exception;

public final class ExtractionAccessMethodException extends Exception
{
    public ExtractionAccessMethodException()
    {
        super();
    }

    public ExtractionAccessMethodException(final String description)
    {
        super(description);
    }

    public ExtractionAccessMethodException(final Exception cause)
    {
        super(cause);
    }

    public ExtractionAccessMethodException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
