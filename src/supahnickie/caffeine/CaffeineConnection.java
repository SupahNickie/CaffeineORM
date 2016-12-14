package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CaffeineConnection is the connection handler and can also be used to run direct SQL queries/updates. All configuration
 * must be properly set before any queries can be run.
 * 
 * @author Nicholas Case (nicholascase@live.com)
 * @version 5.0.0
 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples, including initialization</a>
 */
public final class CaffeineConnection {
	@SuppressWarnings("rawtypes")
	private static Class currentQueryClass;
	private static String dbDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static Connection connection;
	private static Map<String, String[]> connectionCredentials = new HashMap<String, String[]>();

	private CaffeineConnection() {}

	/**
	 * Since multiple databases can be configured by CaffeineORM, this function will remind you which ones are available for use.
	 * @return Set of database aliases that have been configured and are ready to use.
	 */
	public static final Set<String> listDatabases() {
		return connectionCredentials.keySet();
	}

	/**
	 * Invoked when a query is run without the application having set the query class yet.
	 * @throws Exception
	 * @see {@link CaffeineObject#setQueryClass(Class)} CaffeineObject#setQueryClass(Class)
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
		String[] creds = connectionCredentials.get(name);
		dbDriver = creds[0];
		dbUrl = creds[1];
		dbUsername = creds[2];
		dbPassword = creds[3];
	}

	/**
	 * Before any queries can be run, CaffeineORM must be told what database to use and how to connect to it. At least one database
	 * alias and set of credentials must be added using this function, then you may use the useDatabase() function to
	 * tell CaffeineORM which one to use.
	 * @param name Alias for the database name you would like to use when switching between database connections
	 * @param driver The JDBC driver needed to talk to your database. Example - "org.postgresql.Driver"
	 * @param url The url to connect to the database 
	 * @param username
	 * @param password
	 */
	public static final void addDatabaseConnection(String name, String driver, String url, String username, String password) {
		String[] creds = new String[] { driver, url, username, password };
		connectionCredentials.put(name, creds);
	}

	static final Connection setup() {
		connection = null;
		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			connection.setAutoCommit(false);
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static final void teardown() {
		try {
			connection.close();
			connection = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static final void teardown(ResultSet rs, PreparedStatement ps) throws Exception {
		rs.close();
		ps.close();
		teardown();
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
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 */
	public static final List<HashMap<String, Object>> rawQuery(String sql, Object... args) throws Exception { return CaffeineSQLRunner.executeComplexQuery(sql, args); }

	/**
	 * Function to perform a raw SQL query of the database that is coerced into a CaffeineObject abstract type. The query class
	 * must be set before any of the objectQuery methods are used so the runner knows how to assign the data returned by the SQL
	 * into the CaffeineObject.
	 * @param sql Raw SQL to execute
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 * @see {@link CaffeineObject#setQueryClass(Class)} CaffeineObject#setQueryClass(Class)
	 */
	public static final List<CaffeineObject> objectQuery(String sql) throws Exception { return CaffeineSQLRunner.executeQuery(sql); }

	/**
	 * Function to perform a raw SQL query of the database that is coerced into a CaffeineObject abstract type. The query class
	 * must be set before any of the objectQuery methods are used so the runner knows how to assign the data returned by the SQL
	 * into the CaffeineObject. This version of the function signature expects either JDBC style placeholders (?) 
	 * or bind variables ($1, $2) to be used.
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args A list of Objects to be inserted into the placeholders
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 * @see {@link CaffeineObject#setQueryClass(Class)} CaffeineObject#setQueryClass(Class)
	 */
	public static final List<CaffeineObject> objectQuery(String sql, List<Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args); }

	/**
	 * Function to perform a raw SQL query of the database that is coerced into a CaffeineObject abstract type. The query class
	 * must be set before any of the objectQuery methods are used so the runner knows how to assign the data returned by the SQL
	 * into the CaffeineObject. This version of the function signature expects either JDBC style placeholders (?) 
	 * or bind variables ($1, $2) to be used.
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args Varargs of type Object to be inserted into the placeholders
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 * @see {@link CaffeineObject#setQueryClass(Class)} CaffeineObject#setQueryClass(Class)
	 */
	public static final List<CaffeineObject> objectQuery(String sql, Object... args) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args); }

	/**
	 * Function to perform an ActiveRecord-like query of the database, using the keys in the args hash as columns and
	 * the values as the where conditions for which to look up records by. The query class must be set before any of 
	 * the objectQuery methods are used so the runner knows how to assign the data returned by the SQL into the CaffeineObject.
	 * @param sql Raw SQL to execute (with placeholders)
	 * @param args Map of arguments with the keys being column names and the values being limiting conditions (i.e. where {KEY} = {VALUE})
	 * @return List of CaffeineObjects populated with data from the database. The return is always a list, even when there is only one record.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples</a>
	 * @see {@link CaffeineObject#setQueryClass(Class)} CaffeineObject#setQueryClass(Class)
	 */
	public static final List<CaffeineObject> query(Map<String, Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(args); }
}
