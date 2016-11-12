package caffeine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public default List<CaffeineObject> executeQuery(PreparedStatement ps) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		List<CaffeineObject> ret = new ArrayList<CaffeineObject>();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		Row.formTable(rs, table);
		for (HashMap<String, Object> row : table) {
			CaffeineObject newInstance = (CaffeineObject) getClass().getConstructor().newInstance();
			for (String column: row.keySet()) {
				newInstance.setAttr(column, row.get(column));
		  }
			ret.add(newInstance);
		}
		teardown(rs, ps);
		return ret;
	}

	public default List<CaffeineObject> executeQuery(String sql) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		return executeQuery(setup().prepareStatement(sql));
	}

	public default List<CaffeineObject> executeQuery(String sql, List<Object> values, Map<String, Object> options) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		sql = appendOptions(sql, options);
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		return executeQuery(ps);
	}

	public default List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
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

	public default List<CaffeineObject> execute() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, SQLException, NoSuchFieldException {
		List<CaffeineObject> results = executeQuery(getCurrentQuery());
		resetQueryState();
		return results;
	}

	public default CaffeineObject find(int i) throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		Connection c = setup();
		CaffeineObject newInstance = (CaffeineObject) getClass().getConstructor().newInstance();
		PreparedStatement ps = c.prepareStatement(baseQuery() + " where id = ?");
		ps.setInt(1, i);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			newInstance.setAttrs(rs);
		}
		teardown(rs, ps);
		return newInstance;
	}

	public default CaffeineObject join(String typeOfJoin, String fromJoin, String toJoin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
		String[] fromJoins = fromJoin.split("\\.");
		String[] toJoins = toJoin.split("\\.");
		String sql;
		if (getCurrentQuery() == null) {
			sql = baseQuery();
		} else {
			sql = getCurrentQuery();
		}
		if (typeOfJoin.equals("")) {
			typeOfJoin = "join ";
		} else {
			typeOfJoin = typeOfJoin + " join ";
		}
		sql = sql + " " + typeOfJoin + toJoins[0] + " on " + toJoins[0] + "." + toJoins[1] + " = " + fromJoins[0] + "." + fromJoins[1];
		setCurrentQuery(sql);
		return this;
	}

	public default CaffeineObject join(String fromJoin, String toJoin) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException { 
		return join("", fromJoin, toJoin);
	}

	public default CaffeineObject where(String condition) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		return appendCondition("and", condition);
	}

	public default CaffeineObject where(String condition, Object placeholderValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		String replacedString = sanitizeParameter(condition, placeholderValue);
		return where(replacedString);
	}

	public default CaffeineObject or(String condition) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		return appendCondition("or", condition);
	}

	public default CaffeineObject or(String condition, Object placeholderValue) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		String replacedString = sanitizeParameter(condition, placeholderValue);
		return or(replacedString);
	}

	/* Helper methods */

	public default CaffeineObject appendCondition(String type, String condition) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		if (getCurrentQuery() == null) {
			setCurrentQuery(baseQuery());
		}
		Pattern p = Pattern.compile("where");
		Matcher m = p.matcher(getCurrentQuery());
		if ( !m.find() ) setFirstCondition(true);
		if ( getFirstCondition() ) { 
			setCurrentQuery(getCurrentQuery() + " where ");
		} else {
			setCurrentQuery(getCurrentQuery() + type + " ");
		}
		setFirstCondition(false);
		setCurrentQuery(getCurrentQuery() + condition + " ");
		return this;
	}

	// TODO: Change replace method to guard against SQL injection
	public default String sanitizeParameter(String condition, Object placeholderValue) {
		return condition.replaceAll("[?]", placeholderValue.toString());
	}

	public static String appendOptions(String sql, Map<String, Object> options) {
		if ((options != null) && (!options.isEmpty()) ) {
			sql = sql + " ";
			if (options.containsKey("groupBy")) { sql = sql + "group by " + options.get("groupBy") + " "; }
			if (options.containsKey("orderBy")) { sql = sql + "order by " + options.get("orderBy") + " "; }
			if (options.containsKey("limit")) { sql = sql + "limit " + options.get("limit") + " "; }
		}
		return sql;
	}

	public default String baseQuery() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		Field field = getClass().getDeclaredField("tableName");
		String tableName = (String) field.get(null);
		String sql = "select " + tableName + ".* from " + tableName;
		return sql;
	}

	public default void resetQueryState() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		setCurrentQuery(null);
		setFirstCondition(true);
	}

	/* Getters */

	public default String getCurrentQuery() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = getClass().getDeclaredField("currentQuery");
		return (String) field.get(this);
	}

	public default  boolean getFirstCondition() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = getClass().getDeclaredField("firstCondition");
		return (Boolean) field.get(this);
	}

	/* Setters */

	public default void setCurrentQuery(String sql) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = getClass().getDeclaredField("currentQuery");
		field.set(this, sql);
	}

	public default void setFirstCondition(Boolean bool) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = getClass().getDeclaredField("firstCondition");
		field.set(this, bool);
	}

	public default void setAttrs(ResultSet rs) throws SQLException {
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			try {
				String[] attrIdentifier = f.toString().split("\\.");
				f.set(this, rs.getObject(attrIdentifier[attrIdentifier.length - 1]));
			} catch (Exception e){
				System.out.println(e);
			}
		}
	}

	public default void setAttr(String column, Object value) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		try {
			Field field = getClass().getDeclaredField(column);
			field.set(this, value);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

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
