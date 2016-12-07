package supahnickie.caffeine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaffeineObject {
	static List<String> ignoredFields = new ArrayList<String>();
	String currentQuery;
	boolean firstCondition;
	List<Object> placeholders = new ArrayList<Object>();
	public String validationErrors = "";

	static {
		ignoredFields.add("tableName");
		ignoredFields.add("validationErrors");
		ignoredFields.add("caffeineAssociations");
	}

	/* Internal meta-utilities */

	public static List<String> addIgnoredField(String field) {
		ignoredFields.add(field);
		return ignoredFields;
	}

	@SuppressWarnings("rawtypes")
	public static final void setQueryClass(Class klass) {
		Caffeine.setQueryClass(klass);
	}

	public final static CaffeineObject chainable() {
		return new CaffeineObject();
	}

	@SuppressWarnings("rawtypes")
	public final static CaffeineObject chainable(Class klass) {
		Caffeine.setQueryClass(klass);
		return new CaffeineObject();
	}

	/* AR-like CRUD */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final CaffeineObject find(Class klass, int i) throws Exception {
		Connection c = Caffeine.setup();
		CaffeineObject newInstance = (CaffeineObject) Caffeine.setQueryClass(klass).getConstructor().newInstance();
		PreparedStatement ps = c.prepareStatement(CaffeineObject.baseQuery() + " where id = ?");
		ps.setInt(1, i);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			newInstance.setAttrs(rs);
		}
		Caffeine.teardown(rs, ps);
		return newInstance;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static CaffeineObject create(Class klass, Map<String, Object> args) {
		try {
			Method valid = klass.getMethod("validate", new Class[] { String.class });
			if ((boolean) valid.invoke(klass.newInstance(), new Object[] { "create" })) {
				Caffeine.setQueryClass(klass);
				Object[] argKeys = args.keySet().toArray();
				String sql = insertInsertPlaceholders(args, argKeys);
				return Caffeine.executeUpdate(sql, args, argKeys, (CaffeineObject) klass.newInstance());
			} else {
				System.out.println("Failed validation; please run the 'getValidationErrors()' method to see errors.");
				return null;
			}
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	/* Already have an instance in memory, just wanting to persist it to the DB */

	public final CaffeineObject create() {
		try {
			if (validate("create")) {
				Caffeine.setQueryClass(this.getClass());
				Map<String, Object> args = new HashMap<String, Object>();
				Field[] fields = Caffeine.getQueryClass().getDeclaredFields();
				for (Field f : fields) {
					String[] nameSplit = f.toString().split("\\.");
					String simpleName = nameSplit[nameSplit.length - 1];
					if ( !(ignoredFields.contains(simpleName) || simpleName.startsWith("$") || simpleName.equals("id")) ) {
						if (Modifier.isPrivate(f.getModifiers())) { f.setAccessible(true); }
						args.put(simpleName, f.get(this));
					}
				}
				Object[] argKeys = args.keySet().toArray();
				String sql = insertInsertPlaceholders(args, argKeys);
				return Caffeine.executeUpdate(sql, args, argKeys, this);
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
				Caffeine.setQueryClass(this.getClass());
				Object[] argKeys = args.keySet().toArray();
				String sql = insertUpdatePlaceholders(args, argKeys);
				return Caffeine.executeUpdate(sql, args, argKeys, this);
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
			Caffeine.setQueryClass(this.getClass());
			Field tableNameField = Caffeine.getQueryClass().getDeclaredField("tableName");
			Field field = Caffeine.getQueryClass().getDeclaredField("id");
			String tableName = (String) tableNameField.get(null);
			field.setAccessible(true);
			int id = field.getInt(this);
			String sql = "delete from " + tableName + " where id = " + id;
			Caffeine.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	/* AR-like querying methods */

	@SuppressWarnings("unchecked")
	public final List<CaffeineObject> execute() throws Exception {
		String sql = getCurrentQuery();
		PreparedStatement ps = Caffeine.setup().prepareStatement(sql);
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
		List<CaffeineObject> results = Caffeine.executeQuery(ps);
		resetQueryState();
		return results;
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

	@SuppressWarnings("rawtypes")
	public final List<CaffeineObject> getAssociated(Class associated) throws Exception {
		return getAssociated(associated, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final List<CaffeineObject> getAssociated(Class associated, String foreignKey) throws Exception, SQLException, ClassNotFoundException {
		Map<Class, String> associations = (Map<Class, String>) Caffeine.setQueryClass(this.getClass()).getDeclaredField("caffeineAssociations").get(null);
		String type = associations.get(associated);
		switch (type) {
			case "hasMany":
				return getHasMany(associated, foreignKey);
			case "belongsTo":
				return getBelongsTo(associated, foreignKey);
			default:
				break;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private final List<CaffeineObject> getHasMany(Class associated, String foreignKey) throws Exception {
		Field tableNameField = Caffeine.getQueryClass().getDeclaredField("tableName");
		Field associatedTableNameField = associated.getDeclaredField("tableName");
		Field field = Caffeine.getQueryClass().getDeclaredField("id");
		String tableName = (String) tableNameField.get(null);
		String associatedTableName = (String) associatedTableNameField.get(null);
		field.setAccessible(true);
		int id = field.getInt(this);
		String foreignLookup = (foreignKey == null) ? tableName.substring(0, tableName.length() - 1) + "_id" : foreignKey;
		String sql = "select " + associatedTableName + ".* from " + associatedTableName + " where " + foreignLookup + " = " + id;
		Caffeine.setQueryClass(associated);
		return Caffeine.executeQuery(sql);
	}

	@SuppressWarnings("rawtypes")
	private final List<CaffeineObject> getBelongsTo(Class associated, String foreignKey) throws Exception {
		Field associatedTableNameField = associated.getDeclaredField("tableName");
		String associatedTableName = (String) associatedTableNameField.get(null);
		Field field = (foreignKey == null) ? Caffeine.getQueryClass().getDeclaredField(associatedTableName.substring(0, associatedTableName.length() -1) + "_id") : Caffeine.getQueryClass().getDeclaredField(foreignKey);
		field.setAccessible(true);
		int id = field.getInt(this);
		String sql = "select " + associatedTableName + ".* from " + associatedTableName + " where id = " + id;
		Caffeine.setQueryClass(associated);
		return Caffeine.executeQuery(sql);
	}

	/* Helper methods */

	private final static String insertInsertPlaceholders(Map<String, Object> args, Object[] argKeys) throws Exception {
		Field tableNameField = Caffeine.getQueryClass().getDeclaredField("tableName");
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
		Field tableNameField = Caffeine.getQueryClass().getDeclaredField("tableName");
		Field field = Caffeine.getQueryClass().getDeclaredField("id");
		String tableName = (String) tableNameField.get(null);
		field.setAccessible(true);
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

	static String appendOptions(String sql, Map<String, Object> options) {
		if ((options != null) && (!options.isEmpty()) ) {
			sql = sql + " ";
			if (options.containsKey("groupBy")) { sql = sql + "group by " + options.get("groupBy") + " "; }
			if (options.containsKey("orderBy")) { sql = sql + "order by " + options.get("orderBy") + " "; }
			if (options.containsKey("limit")) { sql = sql + "limit " + options.get("limit") + " "; }
		}
		return sql;
	}

	/* validationType being either "update" or "create" */
	public boolean validate(String validationType) {
		return true;
	}

	public final String getValidationErrors() throws Exception {
		Field field = Caffeine.getQueryClass().getDeclaredField("validationErrors");
		String errors = (String) field.get(this);
		return errors;
	}

	static final String baseQuery() throws Exception {
		Field field = Caffeine.getQueryClass().getDeclaredField("tableName");
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

	final List<Object> getPlaceholders() throws Exception {
		return this.placeholders;
	}

	final String getCurrentQuery() throws Exception {
		return this.currentQuery;
	}

	final boolean getFirstCondition() throws Exception {
		return this.firstCondition;
	}

	/* Setters */

	final void setPlaceholders(List<Object> placeholders) throws Exception {
		this.placeholders = placeholders;
	}

	final void setCurrentQuery(String sql) throws Exception {
		this.currentQuery = sql;
	}

	final void setFirstCondition(Boolean bool) throws Exception {
		this.firstCondition = bool;
	}

	final void setAttrs(ResultSet rs) throws Exception {
		Field[] fields = Caffeine.getQueryClass().getDeclaredFields();
		for (Field f : fields) {
			try {
				String[] attrIdentifier = f.toString().split("\\.");
				f.setAccessible(true);
				f.set(this, rs.getObject(attrIdentifier[attrIdentifier.length - 1]));
			} catch (Exception e){
				// Do nothing
			}
		}
	}

	final void setAttrsFromSqlReturn(ResultSet rs) throws Exception {
		if (rs.next()) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int numOfCol = rsmd.getColumnCount();
			for (int i = 1; i <= numOfCol; i++) {
				this.setAttr(rsmd.getColumnName(i), rs.getObject(i));
			}
		}
	}

	final void setAttr(String column, Object value) throws Exception {
		try {
			Field field = Caffeine.getQueryClass().getDeclaredField(column);
			field.setAccessible(true);
			field.set(this, value);
		} catch (Exception e) {
			// Do nothing
		}
	}
}
