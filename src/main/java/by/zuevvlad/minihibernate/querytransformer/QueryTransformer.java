package by.zuevvlad.minihibernate.querytransformer;

@FunctionalInterface
public interface QueryTransformer
{
    public abstract String transform(final String transformedQuery);
}
