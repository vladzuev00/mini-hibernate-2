package by.zuevvlad.minihibernate.propertiesfilereader;

import by.zuevvlad.minihibernate.propertiesfilereader.exception.PropertiesFileReadingException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class PropertiesFileReader
{
    public static PropertiesFileReader createPropertiesFileReader()
    {
        if(PropertiesFileReader.propertiesFileReader == null)
        {
            synchronized(PropertiesFileReader.class)
            {
                if(PropertiesFileReader.propertiesFileReader == null)
                {
                    PropertiesFileReader.propertiesFileReader = new PropertiesFileReader();
                }
            }
        }
        return PropertiesFileReader.propertiesFileReader;
    }

    private static PropertiesFileReader propertiesFileReader = null;

    private PropertiesFileReader()
    {
        super();
    }

    public final Properties readProperties(final String pathOfReadFile)
            throws PropertiesFileReadingException
    {
        try(final FileInputStream fileInputStream = new FileInputStream(pathOfReadFile);
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream))
        {
            final Properties readProperties = new Properties();
            readProperties.load(bufferedInputStream);
            return readProperties;
        }
        catch(final IOException cause)
        {
            throw new PropertiesFileReadingException(cause);
        }
    }
}
