package supahnickie.caffeine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	@SuppressWarnings("unchecked")
	static final PreparedStatement replaceNamedParameters(Connection c, String sql, List<Object> values) throws Exception {
		int counter = 1;
		PreparedStatement ps = null;
		Pattern p = Pattern.compile("\\$\\d*");
		Matcher m = p.matcher(sql);
		List<String> allMatches = new ArrayList<String>();
		Queue<Object> replacementVals = new LinkedList<Object>();
		while (m.find()) { allMatches.add(m.group()); }
		for (String placeholder : allMatches) {
			int indexToGrab = new Integer(placeholder.split("\\$")[1]) - 1;
			Object val = values.get(indexToGrab);
			if (val.getClass().equals(ArrayList.class) || val.getClass().equals(LinkedList.class)) {
				List<Object> vals = (List<Object>) val;
				String arrayPlaceholder = "";
				for (int i = 0; i < vals.size() - 1; i++) { arrayPlaceholder = arrayPlaceholder.concat("?, "); }
				arrayPlaceholder = arrayPlaceholder.concat("?");
				sql = sql.replaceAll("\\$" + (indexToGrab + 1), arrayPlaceholder);
				for (int j = 0; j < vals.size(); j++) { replacementVals.add(vals.get(j)); }
			} else {
				replacementVals.add(val);
			}
		}
		sql = sql.replaceAll("\\$\\d*", "?");
		ps = c.prepareStatement(sql);
		while (!replacementVals.isEmpty()) {
			ps.setObject(counter, replacementVals.poll());
			counter++;
		}
		return ps;
	}
	
	@SuppressWarnings("unchecked")
	static final PreparedStatement replaceJDBCParameters(Connection c, String sql, List<Object> values) throws Exception {
		int counter = 1;
		sql = injectAdditionalPlaceholders(sql, values);
		PreparedStatement ps = c.prepareStatement(sql);
		for (Object value : values) {
			if (value.getClass().equals(ArrayList.class) || value.getClass().equals(LinkedList.class)) {
				List<Object> vals = (List<Object>) value;
				for (int j = 0; j < vals.size(); j++) {
					ps.setObject(counter, vals.get(j));
					counter++;
				}
			} else {
				ps.setObject(counter, value);
				counter++;
			}
		}
		return ps;
	}

	@SuppressWarnings("unchecked")
	private static final String injectAdditionalPlaceholders(String sql, List<Object> values) {
		Pattern p = Pattern.compile("\\?");
		Matcher m = p.matcher(sql);
		int index = 0;
		while (m.find()) {
			Object newVal = values.get(index);
			if (newVal.getClass().equals(ArrayList.class) || newVal.getClass().equals(LinkedList.class)) {
				List<Object> vals = (List<Object>) newVal;
				String arrayPlaceholder = "";
				for (int i = 0; i < vals.size() - 1; i++) {
					arrayPlaceholder = arrayPlaceholder.concat("?, ");
				}
				arrayPlaceholder = arrayPlaceholder.concat("?");
				sql = sql.replaceFirst("\\(\\?\\)", "( " + arrayPlaceholder + " )");
				index++;
			} else {
				index++;
			}
		}
		return sql;
	}

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
		PreparedStatement ps = (sql.contains("$")) ? replaceNamedParameters(c, sql, values) : replaceJDBCParameters(c, sql, values);
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
		Connection c = setup();
		if (!(options == null)) { sql = CaffeineObject.appendOptions(sql, options); }
		PreparedStatement ps = (sql.contains("$")) ? replaceNamedParameters(c, sql, values) : replaceJDBCParameters(c, sql, values);
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
