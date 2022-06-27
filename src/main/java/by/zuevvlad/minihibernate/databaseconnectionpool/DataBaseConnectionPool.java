package by.zuevvlad.minihibernate.databaseconnectionpool;

import by.zuevvlad.minihibernate.databaseconnectionpool.exception.DataBaseConnectionPoolAccessConnectionException;
import by.zuevvlad.minihibernate.databaseconnectionpool.exception.DataBaseConnectionPoolCreatingException;
import by.zuevvlad.minihibernate.databaseconnectionpool.exception.DataBaseConnectionPoolFullingException;
import by.zuevvlad.minihibernate.driverloader.DriverLoader;
import by.zuevvlad.minihibernate.driverloader.exception.DriverLoadingException;
import by.zuevvlad.minihibernate.propertiesfilereader.PropertiesFileReader;
import by.zuevvlad.minihibernate.propertiesfilereader.exception.PropertiesFileReadingException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.*;

public final class DataBaseConnectionPool implements AutoCloseable
{
    private final Future<BlockingQueue<Connection>> holderOfAvailableConnections;

    private DataBaseConnectionPool(final Future<BlockingQueue<Connection>> holderOfAvailableConnections)
    {
        super();
        this.holderOfAvailableConnections = holderOfAvailableConnections;
    }

    public static DataBaseConnectionPool createDataBaseConnectionPool()
    {
        if(DataBaseConnectionPool.dataBaseConnectionPool == null)
        {
            synchronized(DataBaseConnectionPool.class)
            {
                if(DataBaseConnectionPool.dataBaseConnectionPool == null)
                {
                    try
                    {
                        final Properties dataBaseProperties = DataBaseConnectionPool.PROPERTIES_FILE_READER
                                .readProperties(PATH_OF_FILE_WITH_DATA_BASE_PROPERTIES);

                        final String nameOfDriverClass = dataBaseProperties.getProperty(
                                DataBaseConnectionPool.KEY_OF_PROPERTY_OF_NAME_OF_DRIVER_CLASS);
                        DataBaseConnectionPool.DRIVER_LOADER.loadDriver(nameOfDriverClass);

                        final String urlOfDataBase = dataBaseProperties.getProperty(
                                DataBaseConnectionPool.KEY_OF_PROPERTY_OF_URL_OF_DATA_BASE);
                        final String nameOfUser = dataBaseProperties.getProperty(
                                DataBaseConnectionPool.KEY_OF_PROPERTY_OF_NAME_OF_USER_OF_DATA_BASE);
                        final String password = dataBaseProperties.getProperty(
                                DataBaseConnectionPool.KEY_OF_PROPERTY_OF_PASSWORD_OF_DATA_BASE);

                        final String descriptionOfAmountOfConnectionsInPool = dataBaseProperties.getProperty(
                                DataBaseConnectionPool.KEY_OF_PROPERTY_OF_AMOUNT_OF_CONNECTIONS_IN_POOL);
                        final int amountOfConnectionsInPool = Integer.parseInt(descriptionOfAmountOfConnectionsInPool);

                        final ExecutorService executorService = Executors.newSingleThreadExecutor();
                        final PoolFuller poolFuller = new PoolFuller(urlOfDataBase, nameOfUser, password,
                                amountOfConnectionsInPool);
                        final Future<BlockingQueue<Connection>> holderOfAvailableConnections
                                = executorService.submit(poolFuller);
                        executorService.shutdown();
                        DataBaseConnectionPool.dataBaseConnectionPool
                                = new DataBaseConnectionPool(holderOfAvailableConnections);
                    }
                    catch(final PropertiesFileReadingException | DriverLoadingException cause)
                    {
                        throw new DataBaseConnectionPoolCreatingException(cause);
                    }
                }
            }
        }
        return DataBaseConnectionPool.dataBaseConnectionPool;
    }

    private static DataBaseConnectionPool dataBaseConnectionPool = null;
    private static final PropertiesFileReader PROPERTIES_FILE_READER
            = PropertiesFileReader.createPropertiesFileReader();
    private static final String PATH_OF_FILE_WITH_DATA_BASE_PROPERTIES = "src/main/resources/db.properties";
    private static final String KEY_OF_PROPERTY_OF_NAME_OF_DRIVER_CLASS = "db.nameOfDriverClass";
    private static final DriverLoader DRIVER_LOADER = DriverLoader.createDriverLoader();
    private static final String KEY_OF_PROPERTY_OF_URL_OF_DATA_BASE = "db.url";
    private static final String KEY_OF_PROPERTY_OF_NAME_OF_USER_OF_DATA_BASE = "db.userName";
    private static final String KEY_OF_PROPERTY_OF_PASSWORD_OF_DATA_BASE = "db.password";
    private static final String KEY_OF_PROPERTY_OF_AMOUNT_OF_CONNECTIONS_IN_POOL = "db.amountOfConnectionsInPool";

    private static final class PoolFuller implements Callable<BlockingQueue<Connection>>
    {
        private final String urlOfDataBase;
        private final String nameOfUserOfDataBase;
        private final String passwordOfDataBase;
        private final int amountOfConnections;

        public PoolFuller(final String urlOfDataBase, final String nameOfUserOfDataBase,
                          final String passwordOfDataBase, final int amountOfConnections)
        {
            super();
            this.urlOfDataBase = urlOfDataBase;
            this.nameOfUserOfDataBase = nameOfUserOfDataBase;
            this.passwordOfDataBase = passwordOfDataBase;
            this.amountOfConnections = amountOfConnections;
        }

        @Override
        public final BlockingQueue<Connection> call()
        {
            final BlockingQueue<Connection> fulledConnections = new ArrayBlockingQueue<Connection>(
                    this.amountOfConnections);
            try
            {
                Connection currentFulledConnection;
                for(int i = 0; i < this.amountOfConnections; i++)
                {
                    currentFulledConnection = DriverManager.getConnection(this.urlOfDataBase, this.nameOfUserOfDataBase,
                            this.passwordOfDataBase);
                    fulledConnections.add(currentFulledConnection);
                }
                return fulledConnections;
            }
            catch(final SQLException cause)
            {
                final DataBaseConnectionPoolFullingException mainException
                        = new DataBaseConnectionPoolFullingException();
                if(!fulledConnections.isEmpty())
                {
                    try
                    {
                        for(final Connection fulledConnection : fulledConnections)
                        {
                            fulledConnection.close();
                        }
                    }
                    catch(final SQLException exceptionOfClosingConnection)
                    {
                        mainException.addSuppressed(exceptionOfClosingConnection);
                    }
                }
                throw mainException;
            }
        }
    }

    public final int getAmountOfAvailableConnections()
    {
        try
        {
            final BlockingQueue<Connection> availableConnections = this.holderOfAvailableConnections.get();
            return availableConnections.size();
        }
        catch(final ExecutionException | InterruptedException cause)
        {
            throw new DataBaseConnectionPoolFullingException(cause);
        }
    }

    public final Connection findAvailableConnection()
            throws DataBaseConnectionPoolAccessConnectionException
    {
        try
        {
            final BlockingQueue<Connection> availableConnection = this.holderOfAvailableConnections.get();
            final Connection foundConnection = availableConnection.poll(
                    DataBaseConnectionPool.AMOUNT_OF_UNITS_OF_WAITING_OF_CONNECTION,
                    DataBaseConnectionPool.TIME_UNIT_OF_WAITING_OF_CONNECTION);
            if(foundConnection == null)
            {
                throw new DataBaseConnectionPoolAccessConnectionException("Trying of getting connection from connection"
                        + " pool is very long.");
            }
            return foundConnection;
        }
        catch(final ExecutionException cause)
        {
            throw new DataBaseConnectionPoolFullingException(cause);
        }
        catch(final InterruptedException cause)
        {
            throw new DataBaseConnectionPoolAccessConnectionException(cause);
        }
    }

    private static final long AMOUNT_OF_UNITS_OF_WAITING_OF_CONNECTION = 5;
    private static final TimeUnit TIME_UNIT_OF_WAITING_OF_CONNECTION = TimeUnit.SECONDS;

    public final void returnConnectionToPool(final Connection returnedConnection)
    {
        try
        {
            final BlockingQueue<Connection> availableConnections = this.holderOfAvailableConnections.get();
            availableConnections.add(returnedConnection);
        }
        catch(final InterruptedException | ExecutionException cause)
        {
            throw new DataBaseConnectionPoolFullingException(cause);
        }
    }

    @Override
    public final void close() throws IOException
    {
        try
        {
            final BlockingQueue<Connection> availableConnections = this.holderOfAvailableConnections.get();
            for(final Connection closedConnection : availableConnections)
            {
                closedConnection.close();
            }
        }
        catch(final ExecutionException | InterruptedException cause)
        {
            throw new DataBaseConnectionPoolFullingException(cause);
        }
        catch(final SQLException cause)
        {
            throw new IOException(cause);
        }
    }
}

