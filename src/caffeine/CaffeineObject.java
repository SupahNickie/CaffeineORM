package caffeine;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

public interface CaffeineObject {

	/* Raw SQL execute, used for INSERT, UPDATE, DELETE */

	public default void executeUpdate(Connection c, PreparedStatement ps) throws SQLException {
		ps.executeUpdate();
		c.commit();
		c.close();
		ps.close();
		teardown();
	}

	public default void executeUpdate(String sql) throws SQLException {
		Connection c = setup();
		executeUpdate(c, c.prepareStatement(sql));
	}

	public default void executeUpdate(String sql, List<Object> values) throws SQLException {
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

	@SuppressWarnings({ "unchecked" })
	public default List<CaffeineObject> executeQuery(PreparedStatement ps) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<CaffeineObject> ret = new ArrayList<CaffeineObject>();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		Row.formTable(rs, table);
		for (HashMap<String, Object> row : table) {
			CaffeineObject newInstance = (CaffeineObject) getCurrentClass().getConstructor().newInstance();
			for (String column: row.keySet()) {
				newInstance.setAttr(column, row.get(column));
		  }
			ret.add(newInstance);
		}
		teardown(rs, ps);
		return ret;
	}

	public default List<CaffeineObject> executeQuery(String sql) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return executeQuery(setup().prepareStatement(sql));
	}

	public default List<CaffeineObject> executeQuery(String sql, List<Object> values, Map<String, Object> options) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		sql = appendOptions(sql, options);
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		return executeQuery(ps);
	}

	public default List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String sql = baseQuery();
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
		return executeQuery(ps);
	}

	/* AR-like querying methods */

	public default List<CaffeineObject> execute() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
		List<CaffeineObject> results = executeQuery(getCurrentQuery());
		resetQueryState();
		return results;
	}

	@SuppressWarnings({ "unchecked" })
	public default CaffeineObject find(int i) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Connection c = setup();
		CaffeineObject newInstance = (CaffeineObject) getCurrentClass().getConstructor().newInstance();
		PreparedStatement ps = c.prepareStatement(baseQuery() + "id = ?");
		ps.setInt(1, i);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			newInstance.setAttrs(rs);
		}
		teardown(rs, ps);
		return newInstance;
	}

	public default CaffeineObject where(String condition) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (getCurrentQuery() == null) {
			setCurrentQuery(baseQuery());
			setFirstCondition(true);
		}
		if (!getFirstCondition()) { setCurrentQuery(getCurrentQuery() + "and "); }
		setFirstCondition(false);
		setCurrentQuery(getCurrentQuery() + condition + " ");
		return this;
	}

	// This is just asking to get SQL-injected at the moment
	public default CaffeineObject where(String condition, Object placeholderValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String replacedString = condition.replaceAll("[?]", placeholderValue.toString());
		return where(replacedString);
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

	public default String baseQuery() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String sql = "select * from ";
		sql = sql + getTableName() + " where ";
		return sql;
	}

	public default void resetQueryState() {
		setCurrentQuery(null);
		setFirstCondition(true);
	}

	/* Getters */

	@SuppressWarnings("rawtypes")
	public abstract Class getCurrentClass();
	public abstract String getTableName();
	public abstract String getCurrentQuery();
	public abstract boolean getFirstCondition();

	/* Setters */

	public abstract void setCurrentQuery(String sql);
	public abstract void setFirstCondition(Boolean bool);
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
