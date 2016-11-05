package caffeine;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

public abstract class CaffeineObject {
	static String currentQuery;

	/* Raw SQL execute, used for INSERT, UPDATE, DELETE */

	public static void executeUpdate(Connection c, PreparedStatement ps) throws SQLException {
		ps.executeUpdate();
		c.commit();
		c.close();
		ps.close();
		teardown();
	}

	public static void executeUpdate(String sql) throws SQLException {
		Connection c = setup();
		executeUpdate(c, c.prepareStatement(sql));
	}

	public static void executeUpdate(String sql, List<Object> values) throws SQLException {
		Connection c = setup();
		PreparedStatement ps = c.prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		executeUpdate(c, ps);
	}

	/* Raw SQL query, used for SELECT */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<CaffeineObject> executeQuery(PreparedStatement ps, Class klass) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<CaffeineObject> ret = new ArrayList<CaffeineObject>();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		Row.formTable(rs, table);
		for (HashMap<String, Object> row : table) {
			CaffeineObject newInstance = (CaffeineObject) klass.getConstructor().newInstance();
			for (String column: row.keySet()) {
				newInstance.setAttr(column, row.get(column));
		  }
			ret.add(newInstance);
		}
		teardown(rs, ps);
		return ret;
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<CaffeineObject> executeQuery(String sql, Class klass) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return executeQuery(setup().prepareStatement(sql), klass);
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<CaffeineObject> executeQuery(String sql, List<Object> values, Map<String, Object> options, Class klass) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		sql = appendOptions(sql, options);
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		return executeQuery(ps, klass);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options, Class klass) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		CaffeineObject exampleInstance = (CaffeineObject) klass.getConstructor().newInstance();
		String sql = "select * from " + exampleInstance.getTableName() + " where ";
		List<String> keys = new ArrayList<>(args.keySet());
		for (int i = 0; i < keys.size(); i++) {
			sql = sql + keys.get(i) + " = ?";
			if ( (args.keySet().size() > 1) && (i != args.keySet().size() - 1) ) { sql = sql + " and "; }
		}
		sql = appendOptions(sql, options);
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (String column : keys) {
			ps.setObject(counter, args.get(column));
			counter++;
		}
		return executeQuery(ps, klass);
	}

	/* AR-like querying methods */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static CaffeineObject find(int i, Class klass) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Connection c = setup();
		CaffeineObject newInstance = (CaffeineObject) klass.getConstructor().newInstance();
		PreparedStatement ps = c.prepareStatement("select * from " + newInstance.getTableName() + " where id = ?");
		ps.setInt(1, i);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			newInstance.setAttrs(rs);
		}
		teardown(rs, ps);
		return newInstance;
	}

	/* AR-like helper methods */

	public static String appendOptions(String sql, Map<String, Object> options) {
		if ((options != null) && (!options.isEmpty()) ) {
			sql = sql + " ";
			if (options.containsKey("groupBy")) { sql = sql + "group by " + options.get("groupBy") + " "; }
			if (options.containsKey("orderBy")) { sql = sql + "order by " + options.get("orderBy") + " "; }
			if (options.containsKey("limit")) { sql = sql + "limit " + options.get("limit") + " "; }
		}
		return sql;
	}

	public abstract String getTableName();
	public abstract void setAttrs(ResultSet rs) throws SQLException;
	public abstract void setAttr(String column, Object value);

	/* Connection handling */

	public static Connection setup() {
		return Caffeine.caffeine.setup();
	}

	public static void teardown() {
		Caffeine.caffeine.teardown();
	}

	public static void teardown(ResultSet rs, PreparedStatement ps) throws SQLException {
		rs.close();
		ps.close();
		teardown();
	}

}
