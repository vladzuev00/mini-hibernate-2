package by.zuevvlad.minihibernate.annotation;

import by.zuevvlad.minihibernate.sqltype.SQLType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Column
{
    public String name();
    public SQLType type();
    public boolean isNullable() default Column.DEFAULT_VALUE_OF_NULLABLE;

    public static final boolean DEFAULT_VALUE_OF_NULLABLE = true;
}
