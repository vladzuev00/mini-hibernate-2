package by.zuevvlad.minihibernate.frameworkproperty;

public enum FrameworkProperty
{
    FRAMEWORK_NAME("MINI_HIBERNATE"), SEPARATOR_OF_FRAMEWORK_NAME_AND_LOG_MESSAGE(" : ");

    private final String value;

    private FrameworkProperty(final String value)
    {
        this.value = value;
    }

    public final String getValue()
    {
        return this.value;
    }
}
