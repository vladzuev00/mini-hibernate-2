package by.zuevvlad.minihibernate.session.exception;

public final class FieldsIntoPreparedStatementBindingException extends Exception
{
    public FieldsIntoPreparedStatementBindingException()
    {
        super();
    }

    public FieldsIntoPreparedStatementBindingException(final String description)
    {
        super(description);
    }

    public FieldsIntoPreparedStatementBindingException(final Exception cause)
    {
        super(cause);
    }

    public FieldsIntoPreparedStatementBindingException(final String description, final Exception cause)
    {
        super(description, cause);
    }
}
