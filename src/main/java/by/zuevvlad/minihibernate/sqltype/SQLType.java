package by.zuevvlad.minihibernate.sqltype;

public enum SQLType
{
    INTEGER("INTEGER"), BIG_INTEGER("BIGINT"), VARCHAR("VARCHAR(256)");

    private final String value;

    private SQLType(final String value)
    {
        this.value = value;
    }

    public final String getValue()
    {
        return this.value;
    }
}
