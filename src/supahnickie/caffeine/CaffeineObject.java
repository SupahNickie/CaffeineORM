package supahnickie.caffeine;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CaffeineObject {
	String currentQuery;
	boolean firstCondition;
	List<Object> placeholders = new ArrayList<Object>();
	public String validationErrors = "";

	/* Raw SQL execute, used for INSERT, UPDATE, DELETE */

	private final void executeUpdate(Connection c, PreparedStatement ps) throws Exception {
		ps.executeUpdate();
		c.commit();
		c.close();
		ps.close();
		teardown();
	}

	public final void executeUpdate(String sql) throws Exception {
		Connection c = setup();
		executeUpdate(c, c.prepareStatement(sql));
	}

	public final void executeUpdate(String sql, List<Object> values) throws Exception {
		Connection c = setup();
		PreparedStatement ps = c.prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		executeUpdate(c, ps);
	}

	private final CaffeineObject executeUpdate(Connection c, PreparedStatement ps, boolean returning) throws Exception {
		if (returning) {
			ps.executeUpdate();
			c.commit();
			setAttrsFromSqlReturn(ps.getGeneratedKeys());
			c.close();
			ps.close();
			teardown();
			return this;
		} else {
			executeUpdate(c, ps);
			return null;
		}
	}

	private final CaffeineObject executeUpdate(String sql, Map<String, Object> args, Object[] argKeys) throws Exception {
		Connection c = setup();
		PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int counter = 1;
		for (int i = 0; i < argKeys.length; i++) {
			ps.setObject(counter, args.get(argKeys[i]));
			counter++;
		}
		return executeUpdate(c, ps, true);
	}

	/* Raw SQL query, used for SELECT */

	private final List<CaffeineObject> executeQuery(PreparedStatement ps) throws Exception {
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

	public final List<CaffeineObject> executeQuery(String sql) throws Exception {
		return executeQuery(setup().prepareStatement(sql));
	}

	public final List<CaffeineObject> executeQuery(String sql, List<Object> values) throws Exception {
		return executeQuery(sql, values, null);
	}

	public final List<CaffeineObject> executeQuery(String sql, List<Object> values, Map<String, Object> options) throws Exception {
		if (!(options == null)) { sql = appendOptions(sql, options); }
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		return executeQuery(ps);
	}

	public final List<CaffeineObject> executeQuery(Map<String, Object> args) throws Exception {
		return executeQuery(args, null);
	}

	public final List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws Exception {
		String sql = baseQuery() + " where ";
		List<String> keys = new ArrayList<>(args.keySet());
		for (int i = 0; i < keys.size(); i++) {
			sql = sql + keys.get(i) + " = ?";
			if ( (args.keySet().size() > 1) && (i != args.keySet().size() - 1) ) { sql = sql + " and "; }
		}
		if (!(options == null)) { sql = appendOptions(sql, options); }
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (String column : keys) {
			ps.setObject(counter, args.get(column));
			counter++;
		}
		return executeQuery(ps);
	}

	/* AR-like querying methods */

	@SuppressWarnings("unchecked")
	public final List<CaffeineObject> execute() throws Exception {
		String sql = getCurrentQuery();
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (int i = 0; i < getPlaceholders().size(); i++) {
			if (getPlaceholders().get(i).getClass().equals(ArrayList.class)) {
				List<Object> arrayArgs = (List<Object>) getPlaceholders().get(i);
				for (int j = 0; j < arrayArgs.size(); j++) {
					ps.setObject(counter, arrayArgs.get(j));
					counter++;
				}
			} else {
				ps.setObject(counter, getPlaceholders().get(i));
				counter++;
			}
		}
		List<CaffeineObject> results = executeQuery(ps);
		resetQueryState();
		return results;
	}

	public final CaffeineObject find(int i) throws Exception {
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

	public final CaffeineObject join(String typeOfJoin, String fromJoin, String toJoin) throws Exception {
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

	public final CaffeineObject join(String fromJoin, String toJoin) throws Exception { 
		return join("", fromJoin, toJoin);
	}

	public final CaffeineObject where(String condition) throws Exception {
		return appendCondition("and", condition);
	}

	public final CaffeineObject where(String condition, Object placeholderValue) throws Exception {
		getPlaceholders().add(placeholderValue);
		return where(condition);
	}

	public final CaffeineObject where(String condition, List<Object> placeholderValues) throws Exception {
		getPlaceholders().add(placeholderValues);
		return where(condition);
	}

	public final CaffeineObject or(String condition) throws Exception {
		return appendCondition("or", condition);
	}

	public final CaffeineObject or(String condition, Object placeholderValue) throws Exception {
		getPlaceholders().add(placeholderValue);
		return or(condition);
	}

	public final CaffeineObject or(String condition, List<Object> placeholderValues) throws Exception {
		getPlaceholders().add(placeholderValues);
		return or(condition);
	}

	public final List<CaffeineObject> getAssociated(CaffeineObject associatedLookup) throws Exception {
		return getAssociated(associatedLookup, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final List<CaffeineObject> getAssociated(CaffeineObject associatedLookup, String foreignKey) throws Exception, SQLException, ClassNotFoundException {
		Map<Class, String> associations = (Map<Class, String>) getClass().getDeclaredField("caffeineAssociations").get(null);
		String type = associations.get(associatedLookup.getClass());
		switch (type) {
			case "hasMany":
				return getHasMany(associatedLookup, foreignKey);
			case "belongsTo":
				return getBelongsTo(associatedLookup, foreignKey);
			default:
				break;
		}
		return null;
	}

	private final List<CaffeineObject> getHasMany(CaffeineObject associatedLookup, String foreignKey) throws Exception {
		Field tableNameField = getClass().getDeclaredField("tableName");
		Field associatedTableNameField = associatedLookup.getClass().getDeclaredField("tableName");
		Field field = getClass().getDeclaredField("id");
		String tableName = (String) tableNameField.get(null);
		String associatedTableName = (String) associatedTableNameField.get(null);
		int id = field.getInt(this);
		String foreignLookup = (foreignKey == null) ? tableName.substring(0, tableName.length() - 1) + "_id" : foreignKey;
		String sql = "select " + associatedTableName + ".* from " + associatedTableName + " where " + foreignLookup + " = " + id;
		return associatedLookup.executeQuery(sql);
	}

	private final List<CaffeineObject> getBelongsTo(CaffeineObject associatedLookup, String foreignKey) throws Exception {
		Field associatedTableNameField = associatedLookup.getClass().getDeclaredField("tableName");
		String associatedTableName = (String) associatedTableNameField.get(null);
		Field field = (foreignKey == null) ? getClass().getDeclaredField(associatedTableName.substring(0, associatedTableName.length() -1) + "_id") : getClass().getDeclaredField(foreignKey);
		int id = field.getInt(this);
		String sql = "select " + associatedTableName + ".* from " + associatedTableName + " where id = " + id;
		return associatedLookup.executeQuery(sql);
	}

	/* Create, Update, Delete methods */

	public final CaffeineObject create(Map<String, Object> args) {
		try {
			if (validate("create")) {
				Object[] argKeys = args.keySet().toArray();
				String sql = insertInsertPlaceholders(args, argKeys);
				return executeUpdate(sql, args, argKeys);
			} else {
				System.out.println("Failed validation; please run the 'getValidationErrors()' method to see errors.");
				return null;
			}
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public final CaffeineObject update(Map<String, Object> args) {
		try {
			if (validate("update")) {
				Object[] argKeys = args.keySet().toArray();
				String sql = insertUpdatePlaceholders(args, argKeys);
				return executeUpdate(sql, args, argKeys);
			} else {
				System.out.println("Failed validation; please run the 'getValidationErrors()' method to see errors.");
				return null;
			}
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public final boolean delete() {
		try {
			Field tableNameField = getClass().getDeclaredField("tableName");
			Field field = getClass().getDeclaredField("id");
			String tableName = (String) tableNameField.get(null);
			int id = field.getInt(this);
			String sql = "delete from " + tableName + " where id = " + id;
			executeUpdate(sql);
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	/* Helper methods */

	private final String insertInsertPlaceholders(Map<String, Object> args, Object[] argKeys) throws Exception {
		Field tableNameField = getClass().getDeclaredField("tableName");
		String tableName = (String) tableNameField.get(null);
		String sql = "insert into " + tableName + " (";
		for (int i = 0; i < argKeys.length; i++) {
			sql = sql + argKeys[i];
			if (i != argKeys.length - 1) { sql = sql + ", "; }
		}
		sql = sql + ") values (";
		for (int j = 0; j < argKeys.length; j++) {
			sql = sql + "?";
			if (j != argKeys.length - 1) { sql = sql + ", "; }
		}
		sql = sql + ")";
		return sql;
	}

	private final String insertUpdatePlaceholders(Map<String, Object> args, Object[] argKeys) throws Exception {
		Field tableNameField = getClass().getDeclaredField("tableName");
		Field field = getClass().getDeclaredField("id");
		String tableName = (String) tableNameField.get(null);
		int id = field.getInt(this);
		String sql = "update " + tableName + " set ";
		for (int i = 0; i < argKeys.length; i++) {
			sql = sql + argKeys[i] + " = ?";
			if (i != argKeys.length - 1) { sql = sql + ", "; }
		}
		sql = sql + " where id = " + id;
		return sql;
	}

	private final CaffeineObject appendCondition(String type, String condition) throws Exception {
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

	private static String appendOptions(String sql, Map<String, Object> options) {
		if ((options != null) && (!options.isEmpty()) ) {
			sql = sql + " ";
			if (options.containsKey("groupBy")) { sql = sql + "group by " + options.get("groupBy") + " "; }
			if (options.containsKey("orderBy")) { sql = sql + "order by " + options.get("orderBy") + " "; }
			if (options.containsKey("limit")) { sql = sql + "limit " + options.get("limit") + " "; }
		}
		return sql;
	}

	/* validationType being either "update" or "create" */
	public abstract boolean validate(String validationType);

	public final String getValidationErrors() throws Exception {
		Field field = getClass().getDeclaredField("validationErrors");
		String errors = (String) field.get(this);
		return errors;
	}

	private final String baseQuery() throws Exception {
		Field field = getClass().getDeclaredField("tableName");
		String tableName = (String) field.get(null);
		String sql = "select " + tableName + ".* from " + tableName;
		return sql;
	}

	private final void resetQueryState() throws Exception {
		setPlaceholders(new ArrayList<Object>());
		setCurrentQuery(null);
		setFirstCondition(true);
	}

	/* Getters */

	public final List<Object> getPlaceholders() throws Exception {
		return this.placeholders;
	}

	public final String getCurrentQuery() throws Exception {
		return this.currentQuery;
	}

	public final boolean getFirstCondition() throws Exception {
		return this.firstCondition;
	}

	/* Setters */

	public final void setPlaceholders(List<Object> placeholders) throws Exception {
		this.placeholders = placeholders;
	}

	public final void setCurrentQuery(String sql) throws Exception {
		this.currentQuery = sql;
	}

	public final void setFirstCondition(Boolean bool) throws Exception {
		this.firstCondition = bool;
	}

	private final void setAttrs(ResultSet rs) throws Exception {
		Field[] fields = getClass().getDeclaredFields();
		for (Field f : fields) {
			try {
				String[] attrIdentifier = f.toString().split("\\.");
				f.set(this, rs.getObject(attrIdentifier[attrIdentifier.length - 1]));
			} catch (Exception e){
				// Do nothing
			}
		}
	}

	private final void setAttrsFromSqlReturn(ResultSet rs) throws Exception {
		if (rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numOfCol = rsmd.getColumnCount();
			for (int i = 1; i <= numOfCol; i++) {
				this.setAttr(rsmd.getColumnName(i), rs.getObject(i));
			}
		}
	}

	private final void setAttr(String column, Object value) throws Exception {
		try {
			Field field = getClass().getDeclaredField(column);
			field.set(this, value);
		} catch (Exception e) {
			// Do nothing
		}
	}

	/* Connection handling */

	private final static Connection setup() {
		return Caffeine.caffeine.setup();
	}

	private final static void teardown() {
		Caffeine.caffeine.teardown();
	}

	private final static void teardown(ResultSet rs, PreparedStatement ps) throws Exception {
		rs.close();
		ps.close();
		teardown();
	}
}
