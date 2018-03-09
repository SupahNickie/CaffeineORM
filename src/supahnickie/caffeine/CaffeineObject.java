package supahnickie.caffeine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CaffeineObject is the parent class of your application's model classes. It will also be the primary class
 * with which you work while using the CaffeineORM.
 * Any return object of type CaffeineObject can be cast to the appropriate concrete model class if needed.
 * 
 * @author Nicholas Case (nicholascase@live.com)
 * @version 1.0.0
 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples, including initialization</a>
 */
public class CaffeineObject {
	static List<String> ignoredFields = new ArrayList<String>();
	protected String validationErrors = "";
	Map<String, Object> attrsOnInit = new HashMap<String, Object>();
	boolean isNewRecord = false;

	static {
		ignoredFields.add("tableName");
		ignoredFields.add("validationErrors");
		ignoredFields.add("caffeineAssociations");
		ignoredFields.add("attrsOnInit");
		ignoredFields.add("isNewRecord");
	}

	protected void init() throws Exception {
		this.isNewRecord = true;
		this.captureCurrentStateOfAttrs();
	}

	/* Internal meta-utilities */

	/**
	 * Adds a model attribute to the collection of other model attributes that should be ignored
	 * when returning CaffeineObjects from a query. Things that are not database attributes are good examples.
	 * @param field	Model attribute to be added to the list of ignored attributes.
	 * @return {@code List<String>} containing the current list of ignored fields after adding.
	 */
	public static List<String> addIgnoredField(String field) {
		ignoredFields.add(field);
		return ignoredFields;
	}

	/**
	 * Prepares the CaffeineConnection class to assign attributes to instances of the correct class
	 * after a query. Calling any function that returns {@code List<CaffeineObject>} or {@code CaffeineObject} 
	 * must first have the query class set properly.
	 * @param klass The actual class object that queries expect to use.
	 */
	@SuppressWarnings("rawtypes")
	public static final void setQueryClass(Class klass) {
		CaffeineConnection.setQueryClass(klass);
	}

	/**
	 * Invokes the CaffeineChainable class which can then be used to make ActiveRecord-like queries. This function signature
	 * assumes that the query class has already been set. If you need to change the active query class or if it has not
	 * been set already, pass the Class of the objects you would like to return in as an argument.
	 * @return An instance of the CaffeineChainable class from which you can join additional queries using the 
	 * {@code join()}, {@code where()}, and {@code or()} methods, actually running the query with {@code execute()}
	 * @throws Exception 
	 * @see CaffeineObject#setQueryClass(Class) setQueryClass(Class)
	 */
	public final static CaffeineChainable chainable() throws Exception {
		return new CaffeineChainable();
	}

	/**
	 * Invokes the CaffeineChainable class which can then be used to make ActiveRecord-like queries. If you need to change 
	 * the active query class or if it has not been set already, pass the Class of the 
	 * objects you would like to return in as an argument.
	 * @param klass	The actual class object that queries expect to use.
	 * @return An instance of the CaffeineChainable class from which you can join additional queries using the 
	 * {@code join()}, {@code where()}, and {@code or()} methods, actually running the query with {@code execute()}
	 * @throws Exception 
	 * @see CaffeineObject#setQueryClass(Class) setQueryClass(Class)
	 */
	@SuppressWarnings("rawtypes")
	public final static CaffeineChainable chainable(Class klass) throws Exception {
		CaffeineConnection.setQueryClass(klass);
		return new CaffeineChainable();
	}

	/* AR-like CRUD */

	/**
	 * Executes an immediate find using id passed in as the lookup against the "id" column in the database.
	 * @param klass The actual class object that Caffeine should use.
	 * @param i The integer (or long) representing the id to look up in the current query table.
	 * @return A CaffeineObject that can be cast to the class representing the object. An ID of 0 and other
	 * values set to null would mean the record was not found in the database.
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	public static final CaffeineObject find(Class klass, long i) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		CaffeineObject newInstance = (CaffeineObject) CaffeineConnection.setQueryClass(klass).newInstance();
		PreparedStatement ps = c.getConnection().prepareStatement(CaffeineObject.baseQuery() + " where id = ?");
		ps.setLong(1, i);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			newInstance.setAttrs(rs);
		}
		newInstance.captureCurrentStateOfAttrs();
		newInstance.setIsNewRecord(false);
		CaffeineConnection.teardown(c, rs, ps);
		return newInstance;
	}

	/**
	 * Executes an immediate find using id passed in as the lookup against the "id" column in the database.
	 * This function signature expects the current query class to already be set.
	 * @param i The integer (or long) representing the id to look up in the current query table.
	 * @return A CaffeineObject that can be cast to the class representing the object. An ID of 0 and other
	 * values set to null would mean the record was not found in the database.
	 * @throws Exception
	 */
	public static final CaffeineObject find(long i) throws Exception {
		return find(CaffeineConnection.getQueryClass(), i);
	}

	/**
	 * Executes either a create action on a new record or an update action on an existing, but changed record.
	 * The save() function calls either create() or update() as appropriate without needing the user to specify
	 * which action it should take.
	 * @return Boolean representing whether or not the record has been saved successfully into the database.
	 * @throws Exception
	 */
	public final boolean save() throws Exception {
		boolean saveSuccessful = false;
		if (this.isNewRecord() && this.isDirty()) {
			try {
				this.create();
				saveSuccessful = true;
			} catch (Exception e) {
				saveSuccessful = false;
			}
		} else if (!this.isNewRecord && this.isDirty()) {
			try {
				this.update();
				saveSuccessful = true;
			} catch (Exception e) {
				saveSuccessful = false;
			}
		}
		return saveSuccessful;
	}

	/**
	 * Static version of the create function. Used when you do not have an instance in memory yet but 
	 * would just like to create a new record.
	 * @param klass The actual class object that Caffeine should use.
	 * @param args A map with the keys being the database columns and the values being the attribute values to write
	 * to the database and assign to the returned object.
	 * @return A CaffeineObject that can be cast to the class representing the object.
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static CaffeineObject create(Class klass, Map<String, Object> args) throws Exception {
		try {
			CaffeineConnection.setQueryClass(klass);
			CaffeineObject newInstance = (CaffeineObject) klass.newInstance();
			Method validate = klass.getMethod("validate", String.class);
			if ((boolean) validate.invoke(newInstance.assignMapArgsToInstance(args), "create")) {
				List<Object> argKeys = new ArrayList<Object>(args.keySet());
				String sql = insertInsertPlaceholders(args, argKeys);
				return CaffeineSQLRunner.executeUpdate(sql, args, argKeys, (CaffeineObject) klass.newInstance());
			} else {
				throw new Exception("Failed validation; please run the 'getValidationErrors()' method to see errors.");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Instance version of the create function. Used when you have an instance in memory already and
	 * would like to persist it in the database.
	 * @return A CaffeineObject that can be cast to the class representing the object.
	 * @throws Exception
	 */
	public final CaffeineObject create() throws Exception {
		try {
			if (validate("create")) {
				CaffeineConnection.setQueryClass(this.getClass());
				Map<String, Object> args = buildArgsFromCurrentInstance();
				List<Object> argKeys = new ArrayList<Object>(args.keySet());
				String sql = insertInsertPlaceholders(args, argKeys);
				return CaffeineSQLRunner.executeUpdate(sql, args, argKeys, this);
			} else {
				throw new Exception("Failed validation; please run the 'getValidationErrors()' method to see errors.");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Updates the row in the database this object represents with whatever new values it is passed in from the args.
	 * @param args A map with the keys being the database columns and the values being the attribute values to write
	 * to the database and assign to the returned object.
	 * @return A CaffeineObject that can be cast to the class representing the object. If the update fails, the calling object
	 * rolls back to its original state.
	 * @throws Exception
	 */
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
				throw new Exception("Failed validation; please run the 'getValidationErrors()' method to see errors.");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Updates the row in the database this object represents with whatever new values it currently has assigned to it.
	 * @return A CaffeineObject that can be cast to the class representing the object. If the update fails, the calling object
	 * rolls back to its original state.
	 * @throws Exception
	 */
	public final CaffeineObject update() throws Exception {
		try {
			if (validate("update")) {
				Map<String, Object> args = buildArgsFromCurrentInstance();
				CaffeineConnection.setQueryClass(this.getClass());
				List<Object> argKeys = new ArrayList<Object>(args.keySet());
				String sql = insertUpdatePlaceholders(args, argKeys);
				return CaffeineSQLRunner.executeUpdate(sql, args, argKeys, this);
			} else {
				throw new Exception("Failed validation; please run the 'getValidationErrors()' method to see errors.");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Deletes an instance from the database using ID as its lookup.
	 * @return Boolean representing whether or not the record was successfully deleted.
	 */
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

	/**
	 * Fetches associated records from a given instance using relationships defined in the extending model classes.
	 * For example, if a User has many Downloads, calling {@code user.getAssociated(Download.class)} would fetch the downloads
	 * that belong to them using the table name of the class as the foreign key lookup (in this case, "user_id" on the Downloads table).
	 * @param associated The class type of the records you would like to fetch.
	 * @return A list of the requested associated records. This is always a list, even when there's only one to return.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/src/supahnickie/testClasses/User.java">Relationships defined in sample User class.</a>
	 */
	@SuppressWarnings("rawtypes")
	public final List<CaffeineObject> getAssociated(Class associated) throws Exception {
		return getAssociated(associated, null);
	}

	/**
	 * Fetches associated records from a given instance using relationships defined in the extending model classes.
	 * For example, if a User has many Downloads, calling {@code user.getAssociated(Download.class, "organizer_id")} would fetch the downloads
	 * where the user's id value is that of the download's organizer_id.
	 * @param associated The class type of the records you would like to fetch.
	 * @param foreignKey The foreign key to use during the query.
	 * @return A list of the requested associated records. This is always a list, even when there's only one to return.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/src/supahnickie/testClasses/User.java">Relationships defined in sample User class.</a>
	 */
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

	/**
	 * Returns true if the record has been newly instantiated and is not in the database yet.
	 * @return Boolean
	 */
	public boolean isNewRecord() { return this.isNewRecord; }

	/**
	 * Returns true if the object has attributes that are different from when the object was instantiated.
	 * For example, retrieving a record from the database and calling isDirty() on it will return false, but if you
	 * change the object's firstName attribute, this will flip to true (and back to false if you change it back to what
	 * it started as).
	 * @return Boolean
	 * @throws Exception
	 */
	public boolean isDirty() throws Exception {
		if (this.attrsOnInit.equals(this.buildArgsFromCurrentInstance())) { return false; }
		return true;
	}

	private final Map<String, Object> buildArgsFromCurrentInstance() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		setQueryClass(this.getClass());
		Field[] fields = this.getClass().getDeclaredFields();
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

	void captureCurrentStateOfAttrs() throws Exception {
		this.attrsOnInit = this.buildArgsFromCurrentInstance();
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

	/**
	 * Function to be overwritten by the extending classes. Validate() is called before persisting records when
	 * the create() or update() methods are called.
	 * @param validationType Either "create" or "update" representing the action taken on the object.
	 * @return Boolean depending on whether or not the record passes validation in the model class.
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/src/supahnickie/testClasses/User.java">validate() defined in the User class.</a>
	 */
	public boolean validate(String validationType) {
		return true;
	}

	static final String baseQuery() throws Exception {
		String tableName = (String) getFieldValue("tableName", CaffeineConnection.getQueryClass());
		String sql = "select " + tableName + ".* from " + tableName;
		return sql;
	}

	/**
	 * Fetches pertinent validation errors as reported in the validate() function.
	 * @return String of validation errors that are defined in the model classes.
	 * @throws Exception
	 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/src/supahnickie/testClasses/User.java">validate() and validationErrors used in sample User class.</a>
	 */
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

	void setIsNewRecord(boolean b) { this.isNewRecord = b; }
}
