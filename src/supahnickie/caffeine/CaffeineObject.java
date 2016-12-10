package supahnickie.caffeine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaffeineObject {
	static List<String> ignoredFields = new ArrayList<String>();
	protected String validationErrors = "";

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
		CaffeineConnection.setQueryClass(klass);
	}

	public final static CaffeineChainable chainable() {
		return new CaffeineChainable();
	}

	@SuppressWarnings("rawtypes")
	public final static CaffeineChainable chainable(Class klass) {
		CaffeineConnection.setQueryClass(klass);
		return new CaffeineChainable();
	}

	/* AR-like CRUD */

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final CaffeineObject find(Class klass, int i) throws Exception {
		Connection c = CaffeineConnection.setup();
		CaffeineObject newInstance = (CaffeineObject) CaffeineConnection.setQueryClass(klass).getConstructor().newInstance();
		PreparedStatement ps = c.prepareStatement(CaffeineObject.baseQuery() + " where id = ?");
		ps.setInt(1, i);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			newInstance.setAttrs(rs);
		}
		CaffeineConnection.teardown(rs, ps);
		return newInstance;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static CaffeineObject create(Class klass, Map<String, Object> args) {
		try {
			CaffeineConnection.setQueryClass(klass);
			CaffeineObject newInstance = (CaffeineObject) klass.newInstance();
			Method validate = klass.getMethod("validate", String.class);
			if ((boolean) validate.invoke(newInstance.assignMapArgsToInstance(args), "create")) {
				List<Object> argKeys = new ArrayList<Object>(args.keySet());
				String sql = insertInsertPlaceholders(args, argKeys);
				return CaffeineSQLRunner.executeUpdate(sql, args, argKeys, (CaffeineObject) klass.newInstance());
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
				CaffeineConnection.setQueryClass(this.getClass());
				Map<String, Object> args = buildArgsFromCurrentInstance();
				List<Object> argKeys = new ArrayList<Object>(args.keySet());
				String sql = insertInsertPlaceholders(args, argKeys);
				return CaffeineSQLRunner.executeUpdate(sql, args, argKeys, this);
			} else {
				System.out.println("Failed validation; please run the 'getValidationErrors()' method to see errors.");
				return null;
			}
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	public final CaffeineObject update(Map<String, Object> args) throws Exception {
		Map<String, Object> originalState = buildArgsFromCurrentInstance();
		try {
			this.assignMapArgsToInstance(args);
			if (validate("update")) {
				CaffeineConnection.setQueryClass(this.getClass());
				List<Object> argKeys = new ArrayList<Object>(args.keySet());
				String sql = insertUpdatePlaceholders(args, argKeys);
				return CaffeineSQLRunner.executeUpdate(sql, args, argKeys, this);
			} else {
				this.assignMapArgsToInstance(originalState);
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
			CaffeineConnection.setQueryClass(this.getClass());
			String tableName = (String) getFieldValue("tableName");
			int id = (int) getFieldValue("id", this);
			String sql = "delete from " + tableName + " where id = " + id;
			CaffeineSQLRunner.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}

	@SuppressWarnings("rawtypes")
	public final List<CaffeineObject> getAssociated(Class associated) throws Exception {
		return getAssociated(associated, null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final List<CaffeineObject> getAssociated(Class associated, String foreignKey) throws Exception {
		Map<Class, String> associations = (Map<Class, String>) CaffeineConnection.setQueryClass(this.getClass()).getDeclaredField("caffeineAssociations").get(null);
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
		String tableName = (String) getFieldValue("tableName");
		String associatedTableName = (String) getFieldValue("tableName", associated);
		int id = (int) getFieldValue("id", this);
		String foreignLookup = (foreignKey == null) ? tableName.substring(0, tableName.length() - 1) + "_id" : foreignKey;
		String sql = "select " + associatedTableName + ".* from " + associatedTableName + " where " + foreignLookup + " = " + id;
		CaffeineConnection.setQueryClass(associated);
		return CaffeineSQLRunner.executeQuery(sql);
	}

	@SuppressWarnings("rawtypes")
	private final List<CaffeineObject> getBelongsTo(Class associated, String foreignKey) throws Exception {
		String associatedTableName = (String) getFieldValue("tableName", associated);
		String foreignLookup = (foreignKey == null) ? associatedTableName.substring(0, associatedTableName.length() -1) + "_id" : foreignKey;
		int id = (int) getFieldValue(foreignLookup, this);
		String sql = "select " + associatedTableName + ".* from " + associatedTableName + " where id = " + id;
		CaffeineConnection.setQueryClass(associated);
		return CaffeineSQLRunner.executeQuery(sql);
	}

	/* Helper methods */

	private final Map<String, Object> buildArgsFromCurrentInstance() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		Field[] fields = CaffeineConnection.getQueryClass().getDeclaredFields();
		for (Field field : fields) {
			String[] nameSplit = field.toString().split("\\.");
			String simpleName = nameSplit[nameSplit.length - 1];
			if ( !(ignoredFields.contains(simpleName) || simpleName.startsWith("$") || simpleName.equals("id")) ) {
				if (Modifier.isPrivate(field.getModifiers())) { field.setAccessible(true); }
				args.put(simpleName, field.get(this));
			}
		}
		return args;
	}

	private final static String insertInsertPlaceholders(Map<String, Object> args, List<Object> argKeys) throws Exception {
		String tableName = (String) getFieldValue("tableName");
		String sql = "insert into " + tableName + " (";
		for (int i = 0; i < argKeys.size(); i++) {
			sql = sql + argKeys.get(i);
			if (i != argKeys.size()- 1) { sql = sql + ", "; }
		}
		sql = sql + ") values (";
		for (int j = 0; j < argKeys.size(); j++) {
			sql = sql + "?";
			if (j != argKeys.size() - 1) { sql = sql + ", "; }
		}
		sql = sql + ")";
		return sql;
	}

	private final String insertUpdatePlaceholders(Map<String, Object> args, List<Object> argKeys) throws Exception {
		String tableName = (String) getFieldValue("tableName");
		int id = (int) getFieldValue("id", this);
		String sql = "update " + tableName + " set ";
		for (int i = 0; i < argKeys.size(); i++) {
			sql = sql + argKeys.get(i) + " = ?";
			if (i != argKeys.size() - 1) { sql = sql + ", "; }
		}
		sql = sql + " where id = " + id;
		return sql;
	}

	/* validationType being either "update" or "create" */

	public boolean validate(String validationType) {
		return false;
	}

	static final String baseQuery() throws Exception {
		String tableName = (String) getFieldValue("tableName", CaffeineConnection.getQueryClass());
		String sql = "select " + tableName + ".* from " + tableName;
		return sql;
	}

	/* Getters */

	public final String getValidationErrors() throws Exception {
		return this.validationErrors;
	}

	private static Object getFieldValue(String fieldName) throws Exception {
		return getFieldValue(fieldName, null, null);
	}

	private static Object getFieldValue(String fieldName, CaffeineObject instance) throws Exception {
		return getFieldValue(fieldName, instance, null);
	}

	@SuppressWarnings("rawtypes")
	private static Object getFieldValue(String fieldName, Class klassToUse) throws Exception {
		return getFieldValue(fieldName, null, klassToUse);
	}

	@SuppressWarnings("rawtypes")
	private static Object getFieldValue(String fieldName, CaffeineObject instance, Class klassToUse) throws Exception {
		Class klass = (klassToUse == null) ? CaffeineConnection.getQueryClass() : klassToUse;
		Field field = klass.getDeclaredField(fieldName);
		field.setAccessible(true);
		Object result = field.get(instance);
		return result;
	}

	/* Setters */

	final CaffeineObject assignMapArgsToInstance(Map<String, Object> args) throws Exception {
		List<String> keySet = new ArrayList<String>(args.keySet());
		for (int i = 0; i < keySet.size(); i++) {
			this.setAttr(keySet.get(i), args.get(keySet.get(i)));
		}
		return this;
	}

	final void setAttrs(ResultSet rs) throws Exception {
		Field[] fields = CaffeineConnection.getQueryClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				String[] attrIdentifier = field.toString().split("\\.");
				field.setAccessible(true);
				field.set(this, rs.getObject(attrIdentifier[attrIdentifier.length - 1]));
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
			Field field = CaffeineConnection.getQueryClass().getDeclaredField(column);
			field.setAccessible(true);
			field.set(this, value);
		} catch (Exception e) {
			// Do nothing
		}
	}
}
