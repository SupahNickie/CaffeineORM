package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CaffeineConnection {
	@SuppressWarnings("rawtypes")
	private static Class currentQueryClass;
	private static String dbDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static Connection connection;
	private static Map<String, String[]> connectionCredentials = new HashMap<String, String[]>();

	public static final Set<String> listDatabases() {
		return connectionCredentials.keySet();
	}

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

	public static final void rawUpdate(String sql) throws Exception { CaffeineSQLRunner.executeUpdate(sql); }
	public static final void rawUpdate(String sql, List<Object> args) throws Exception { CaffeineSQLRunner.executeUpdate(sql, args); }
	public static final void rawUpdate(String sql, Object... args) throws Exception { CaffeineSQLRunner.executeUpdate(sql, args); }
	public static final List<HashMap<String, Object>> rawQuery(String sql) throws Exception { return CaffeineSQLRunner.executeComplexQuery(sql); }
	public static final List<HashMap<String, Object>> rawQuery(String sql, List<Object> args) throws Exception { return CaffeineSQLRunner.executeComplexQuery(sql, args); }
	public static final List<HashMap<String, Object>> rawQuery(String sql, Object... args) throws Exception { return CaffeineSQLRunner.executeComplexQuery(sql, args); }
	public static final List<CaffeineObject> objectQuery(String sql) throws Exception { return CaffeineSQLRunner.executeQuery(sql); }
	public static final List<CaffeineObject> objectQuery(String sql, List<Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args, null); }
	public static final List<CaffeineObject> objectQuery(String sql, Object... args) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args); }
	public static final List<CaffeineObject> objectQuery(String sql, List<Object> args, Map<String, Object> options) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args, options); }
	public static final List<CaffeineObject> query(Map<String, Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(args, null); }
	public static final List<CaffeineObject> query(Map<String, Object> args, Map<String, Object> options) throws Exception { return CaffeineSQLRunner.executeQuery(args, options); }
}
