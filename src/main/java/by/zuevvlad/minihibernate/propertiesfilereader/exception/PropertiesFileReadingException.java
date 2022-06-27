package by.zuevvlad.minihibernate.propertiesfilereader.exception;

public final class PropertiesFileReadingException extends Exception
{
    public PropertiesFileReadingException()
    {
        super();
    }

    public PropertiesFileReadingException(final String description)
    {
        super(description);
    }

    public PropertiesFileReadingException(final Exception cause)
    {
        super(cause);
    }

    public PropertiesFileReadingException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
