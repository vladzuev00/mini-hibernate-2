package by.zuevvlad.minihibernate.fieldextractorbyaccessmethod.exception;

public class FieldExtractionException extends Exception
{
    public FieldExtractionException()
    {
        super();
    }

    public FieldExtractionException(final String description)
    {
        super(description);
    }

    public FieldExtractionException(final Exception cause)
    {
        super(cause);
    }

    public FieldExtractionException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
