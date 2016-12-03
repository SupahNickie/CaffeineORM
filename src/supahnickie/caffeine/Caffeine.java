package supahnickie.caffeine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
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

	/* Insert, update, delete SQL methods */

	static final void executeUpdate(Connection c, PreparedStatement ps) throws Exception {
		ps.executeUpdate();
		c.commit();
		c.close();
		ps.close();
		teardown();
	}

	public static final void executeUpdate(String sql) throws Exception {
		Connection c = setup();
		executeUpdate(c, c.prepareStatement(sql));
	}

	public static final void executeUpdate(String sql, List<Object> values) throws Exception {
		Connection c = setup();
		PreparedStatement ps = c.prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		executeUpdate(c, ps);
	}

	static final CaffeineObject executeUpdate(Connection c, PreparedStatement ps, CaffeineObject instance) throws Exception {
		ps.executeUpdate();
		c.commit();
		instance.setAttrsFromSqlReturn(ps.getGeneratedKeys());
		c.close();
		ps.close();
		teardown();
		return instance;
	}

	static final CaffeineObject executeUpdate(String sql, Map<String, Object> args, Object[] argKeys, CaffeineObject instance) throws Exception {
		Connection c = setup();
		PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int counter = 1;
		for (int i = 0; i < argKeys.length; i++) {
			ps.setObject(counter, args.get(argKeys[i]));
			counter++;
		}
		return executeUpdate(c, ps, instance);
	}

	/* Select statements */

	static final List<CaffeineObject> executeQuery(PreparedStatement ps) throws Exception {
		List<CaffeineObject> ret = new ArrayList<CaffeineObject>();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		Row.formTable(rs, table);
		for (HashMap<String, Object> row : table) {
			CaffeineObject newInstance = (CaffeineObject) getQueryClass().newInstance();
			for (String column: row.keySet()) {
				newInstance.setAttr(column, row.get(column));
		  }
			ret.add(newInstance);
		}
		teardown(rs, ps);
		return ret;
	}

	public static final List<CaffeineObject> executeQuery(String sql) throws Exception {
		return executeQuery(setup().prepareStatement(sql));
	}

	public static final List<CaffeineObject> executeQuery(String sql, List<Object> values) throws Exception {
		return executeQuery(sql, values, null);
	}

	public static final List<CaffeineObject> executeQuery(String sql, List<Object> values, Map<String, Object> options) throws Exception {
		if (!(options == null)) { sql = CaffeineObject.appendOptions(sql, options); }
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		return executeQuery(ps);
	}

	public static final List<CaffeineObject> executeQuery(Map<String, Object> args) throws Exception {
		return executeQuery(args, null);
	}

	public static final List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws Exception {
		String sql = CaffeineObject.baseQuery() + " where ";
		List<String> keys = new ArrayList<>(args.keySet());
		for (int i = 0; i < keys.size(); i++) {
			sql = sql + keys.get(i) + " = ?";
			if ( (args.keySet().size() > 1) && (i != args.keySet().size() - 1) ) { sql = sql + " and "; }
		}
		if (!(options == null)) { sql = CaffeineObject.appendOptions(sql, options); }
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (String column : keys) {
			ps.setObject(counter, args.get(column));
			counter++;
		}
		return executeQuery(ps);
	}
}
