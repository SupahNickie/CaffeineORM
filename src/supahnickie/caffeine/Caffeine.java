package supahnickie.caffeine;

import java.sql.*;
import java.util.List;
import java.util.Map;

public final class Caffeine {
	@SuppressWarnings("rawtypes")
	private static Class currentQueryClass;
	private static String dbDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static Connection connection;

	private Caffeine() {}
	
	public static void setConfiguration(String driver, String url, String username, String password) {
		dbDriver = driver;
		dbUrl = url;
		dbUsername = username;
		dbPassword = password;
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static final void teardown(ResultSet rs, PreparedStatement ps) throws Exception {
		rs.close();
		ps.close();
		teardown();
	}

	@SuppressWarnings("rawtypes")
	static final Class setQueryClass(Class klass) {
		currentQueryClass = klass;
		return klass;
	}

	@SuppressWarnings("rawtypes")
	static final Class getQueryClass() { return currentQueryClass; }
	
	/* Delegated to internal SQL Runner class */

	public static final void executeUpdate(String sql) throws Exception { CaffeineSQLRunner.executeUpdate(sql); }
	public static final void executeUpdate(String sql, List<Object> args) throws Exception { CaffeineSQLRunner.executeUpdate(sql, args); }
	public static final CaffeineObject executeUpdate(String sql, Map<String, Object> args, Object[] argKeys, CaffeineObject instance) throws Exception { return CaffeineSQLRunner.executeUpdate(sql, args, argKeys, instance); }
	public static final List<CaffeineObject> executeQuery(PreparedStatement ps) throws Exception { return CaffeineSQLRunner.executeQuery(ps); }
	public static final List<CaffeineObject> executeQuery(String sql) throws Exception { return CaffeineSQLRunner.executeQuery(sql); }
	public static final List<CaffeineObject> executeQuery(String sql, List<Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args, null); }
	public static final List<CaffeineObject> executeQuery(String sql, List<Object> args, Map<String, Object> options) throws Exception { return CaffeineSQLRunner.executeQuery(sql, args, options); }
	public static final List<CaffeineObject> executeQuery(Map<String, Object> args) throws Exception { return CaffeineSQLRunner.executeQuery(args, null); }
	public static final List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws Exception { return CaffeineSQLRunner.executeQuery(args, options); }
}
