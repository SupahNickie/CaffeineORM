package supahnickie.caffeine;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * CaffeineConnection is the connection handler and can also be used to run direct SQL queries/updates. All configuration
 * must be properly set before any queries can be run.
 * 
 * @author Nicholas Case (nicholascase@live.com)
 * @version 5.2.0
 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples, including initialization</a>
 */
public final class CaffeineConnection {
	@SuppressWarnings("rawtypes")
	private static Class currentQueryClass;
	private static String currentDb;
	private static Map<String, Integer> connectionIndices = new HashMap<String, Integer>();
	private static Map<String, CaffeinePooledConnection[]> connectionPool = new HashMap<String, CaffeinePooledConnection[]>();
	private static Map<String, Object[]> connectionCredentials = new HashMap<String, Object[]>();

	private CaffeineConnection() {}

	/**
	 * Since multiple databases can be configured by CaffeineORM, this function will remind you which ones are available for use.
	 * @return Set of database aliases that have been configured and are ready to use.
	 */
	public static final Set<String> listDatabases() {
		return connectionCredentials.keySet();
	}

	/**
	 * Invoked when a query is run without the application having set the query class yet. See {@link CaffeineObject#setQueryClass(Class)}
	 * @throws Exception
	 */
	public static final void raiseNoQuerySetException() throws Exception {
		throw new Exception("no query class has been set yet so no object queries can commence; use the 'CaffeineObject.setQueryClass' method");
	}

	/**
	 * Before any queries can be run, CaffeineORM must be told what database to use. At least one database
	 * alias and set of credentials must be added using the addDatabaseConnection() function before this function can be used.
	 * @param name User-defined alias for the database to use. Credentials are fetched and connections are then ready to be made.
	 * @throws Exception
	 */
	public static final void useDatabase(String name) throws Exception {
		if ( !(connectionCredentials.containsKey(name)) ) {
			throw new Exception("database " + name + " has not been added to the list of databases yet; use the 'addDatabaseConnection' method");
		}
		currentDb = name;
	}

	/**
	 * Before any queries can be run, CaffeineORM must be told what database to use and how to connect to it. At least one database
	 * alias and set of credentials must be added using this function, then you may use the useDatabase() function to
	 * tell CaffeineORM which one to use.
	 * @param name Alias for the database name you would like to use when switching between database connections
	 * @param driver The JDBC driver needed to talk to your database. Example - "org.postgresql.Driver"
	 * @param url The url to connect to the database 
	 * @param username Username string
	 * @param password Password string
	 * @param concurrency Number of open connections to maintain to database
	 */
	public static final void addDatabaseConnection(String name, String driver, String url, String username, String password, Integer concurrency) {
		Object[] creds = new Object[] { driver, url, username, password, concurrency };
		connectionCredentials.put(name, creds);
		connectionIndices.put(name, 0);
		setupConnectionPool(name, creds, concurrency);
	}

	private static final void setupConnectionPool(String name, Object[] creds, int concurrency) {
		CaffeinePooledConnection[] connections = new CaffeinePooledConnection[concurrency];
		for (int i = 0; i < concurrency; i++) {
			connections[i] = fetchNewConnection(creds);
		}
		connectionPool.put(name, connections);
	}

	static final CaffeinePooledConnection setup() throws Exception {
		int maxConcurrency = (int) connectionCredentials.get(currentDb)[4];
		int idx = connectionIndices.get(currentDb);
		CaffeinePooledConnection connection = connectionPool.get(currentDb)[idx];
		if (!connection.getConnection().isValid(2)) {
			CaffeinePooledConnection newConnection = new CaffeinePooledConnection(connectionCredentials.get(currentDb));
			connectionPool.get(currentDb)[idx] = null;
			connectionPool.get(currentDb)[idx] = newConnection;			
		}
		while (connection.isBusy()) {
			TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(3, 8) * 50);
			int newIdx = setIndex(idx, maxConcurrency);
			connectionIndices.put(currentDb, newIdx);
			connection = connectionPool.get(currentDb)[newIdx];
		}
		connection.setIsBusy(true);
		return connection;
	}

	static final int setIndex(int current, int max) {
		current++;
		if (current >= max) { current = 0; }
		return current;
	}

	static final CaffeinePooledConnection fetchNewConnection(Object[] creds) {
		return new CaffeinePooledConnection(creds);
	}

	static final void teardown(CaffeinePooledConnection connection) {
		connection.setIsBusy(false);
	}

	static final void teardown(CaffeinePooledConnection connection, ResultSet rs, PreparedStatement ps) throws Exception {
		rs.close();
		ps.close();
		teardown(connection);
	}

	/**
	 * Closes all connections for all databases.
	 * @throws Exception
	 */
	public static final void destroyConnectionPools() throws Exception {
		for (String name : connectionPool.keySet()) {
			destroyConnectionPool(name);
		}
	}

	/**
	 * Closes all connections for a specific database.
	 * @param dbName User-assigned alias for the database that should have the connection pool destroyed.
	 * @throws Exception
	 */
	public static final void destroyConnectionPool(String dbName) throws Exception {
		CaffeinePooledConnection[] pool = connectionPool.get(dbName);
		for (CaffeinePooledConnection c : pool) {
			try {
				c.getConnection().close();
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				c = null;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	static final Class getQueryClass() { return currentQueryClass; }

	@SuppressWarnings("rawtypes")
	static final Class setQueryClass(Class klass) {
		currentQueryClass = klass;
		return klass;
	}

	/* Delegated to internal SQL Runner class */

	/**
	 * Function to perform a raw SQL update to the database. The string will be interpreted literally.
	 * This function should only be used for INSERT, UPDATE, or DELETE actions.
	 * @param sql Raw SQL to execute
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final void rawUpdate(String sql) throws Exception { CaffeineSQLRunner.executeUpdate(sql); }

	/**
	 * Function to perform a raw SQL update to the database. This version of the function signature expects
	 * either JDBC style placeholders (?) or bind variables ($1, $2) to be used. This function should only be 
	 * used for INSERT, UPDATE, or DELETE actions. 
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args A list of Objects to be inserted into the placeholders
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final void rawUpdate(String sql, List<Object> args) throws Exception { CaffeineSQLRunner.executeUpdate(sql, args); }

	/**
	 * Function to perform a raw SQL update to the database. This version of the function signature expects
	 * either JDBC style placeholders (?) or bind variables ($1, $2) to be used. This function should only be 
	 * used for INSERT, UPDATE, or DELETE actions.
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args Varargs of type Object to be inserted into the placeholders
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final void rawUpdate(String sql, Object... args) throws Exception { CaffeineSQLRunner.executeUpdate(sql, args); }

	/**
	 * Function to perform a raw SQL query of the database. All the rawQuery functions are not required to have the query class set
	 * because they return a raw List of HashMaps containing the attributes in a String, Object pairing. This function is particularly
	 * useful when doing more complex SQL queries (such as returning attributes from two or more tables or aggregate functions).
	 * @param sql Raw SQL to execute
	 * @return List of records not tied to a specific model, with keys being columns and values being data fields returned from the query.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<HashMap<String, Object>> rawQuery(String sql) throws Exception { return CaffeineSQLRunner.executeComplexQuery(sql); }

	/**
	 * Function to perform a raw SQL query of the database. All the rawQuery functions are not required to have the query class set
	 * because they return a raw List of HashMaps containing the attributes in a String, Object pairing. This function is particularly
	 * useful when doing more complex SQL queries (such as returning attributes from two or more tables or aggregate functions).
	 * This version of the function signature expects either JDBC style placeholders (?) or bind variables ($1, $2) to be used.
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args A list of Objects to be inserted into the placeholders
	 * @return List of records not tied to a specific model, with keys being columns and values being data fields returned from the query.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<HashMap<String, Object>> rawQuery(String sql, List<Object> args) throws Exception { return CaffeineSQLRunner.executeComplexQuery(sql, args); }

	/**
	 * Function to perform a raw SQL query of the database. All the rawQuery functions are not required to have the query class set
	 * because they return a raw List of HashMaps containing the attributes in a String, Object pairing. This function is particularly
	 * useful when doing more complex SQL queries (such as returning attributes from two or more tables or aggregate functions).
	 * This version of the function signature expects either JDBC style placeholders (?) or bind variables ($1, $2) to be used.
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args Varargs of type Object to be inserted into the placeholders
	 * @return List of records not tied to a specific model, with keys being columns and values being data fields returned from the query.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<HashMap<String, Object>> rawQuery(String sql, Object... args) throws Exception { return CaffeineSQLRunner.executeComplexQuery(sql, args); }

	/**
	 * Function to perform a raw SQL query of the database that is coerced into a CaffeineObject abstract type. The query class
	 * must be set before any of the objectQuery methods are used so the runner knows how to assign the data returned by the SQL
	 * into the CaffeineObject. See {@link CaffeineObject#setQueryClass(Class)}
	 * @param sql Raw SQL to execute
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<CaffeineObject> objectQuery(String sql) throws Exception { return CaffeineSQLRunner.executeQuery(sql); }

	/**
	 * Function to perform a raw SQL query of the database that is coerced into a CaffeineObject abstract type. The query class
	 * must be set before any of the objectQuery methods are used so the runner knows how to assign the data returned by the SQL
	 * into the CaffeineObject. This version of the function signature expects either JDBC style placeholders (?) 
	 * or bind variables ($1, $2) to be used. See {@link CaffeineObject#setQueryClass(Class)}
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args A list of Objects to be inserted into the placeholders
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<CaffeineObject> objectQuery(String sql, List<Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args); }

	/**
	 * Function to perform a raw SQL query of the database that is coerced into a CaffeineObject abstract type. The query class
	 * must be set before any of the objectQuery methods are used so the runner knows how to assign the data returned by the SQL
	 * into the CaffeineObject. This version of the function signature expects either JDBC style placeholders (?) 
	 * or bind variables ($1, $2) to be used. See {@link CaffeineObject#setQueryClass(Class)}
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args Varargs of type Object to be inserted into the placeholders
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<CaffeineObject> objectQuery(String sql, Object... args) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args); }

	/**
	 * Function to perform an ActiveRecord-like query of the database, using the keys in the args hash as columns and
	 * the values as the where conditions for which to look up records by. The query class must be set before any of 
	 * the objectQuery methods are used so the runner knows how to assign the data returned by the SQL into the CaffeineObject.
	 * See {@link CaffeineObject#setQueryClass(Class)}
	 * @param args Map of arguments with the keys being column names and the values being limiting conditions (i.e. where {KEY} = {VALUE})
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<CaffeineObject> query(Map<String, Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(args); }
}
