package by.zuevvlad.minihibernate.driverloader;

import by.zuevvlad.minihibernate.driverloader.exception.DriverLoadingException;

public final class DriverLoader
{
    public static DriverLoader createDriverLoader()
    {
        if(DriverLoader.driverLoader == null)
        {
            synchronized(DriverLoader.class)
            {
                if(DriverLoader.driverLoader == null)
                {
                    DriverLoader.driverLoader = new DriverLoader();
                }
            }
        }
        return DriverLoader.driverLoader;
    }

    private static DriverLoader driverLoader = null;

    private DriverLoader()
    {
        super();
    }

    public final void loadDriver(final String nameOfClassOfDriver)
            throws DriverLoadingException
    {
        try
        {
            Class.forName(nameOfClassOfDriver);
        }
        catch(final ClassNotFoundException cause)
        {
            throw new DriverLoadingException(cause);
        }
    }
}