package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CaffeineConnection {
	@SuppressWarnings("rawtypes")
	private static Class currentQueryClass;
	private static String dbDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static Connection connection;

	public static final void setConfiguration(String driver, String url, String username, String password) {
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
