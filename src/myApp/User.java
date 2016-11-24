package myApp;

import supahnickie.caffeine.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class User implements CaffeineObject {

	/* Caffeine utilities */
	public static final String tableName = "users";
	public String currentQuery;
	public boolean firstCondition;
	public String validationErrors = "";
	public List<Object> placeholders = new ArrayList<Object>();

	// Associations
	@SuppressWarnings("rawtypes")
	public static Map<Class, String> caffeineAssociations = new HashMap<Class, String>();

	static {
		caffeineAssociations.put(Download.class, "hasMany");
	}

	/* Normal model attributes; names must map to database columns
	 * (if your db columns are lowerCamelCase, use that style) */
	public int id;
	public String first_name;
	public String last_name;
	public String encrypted_password;
	public int sign_in_count;
	public String role;

	public User() {}

	/* Validations */

	public boolean validate(String validationType) {
		validationErrors = "";
		if (validationType == "create") {
			return createValidations();
		} else {
			return updateValidations();
		}
	}

	public boolean createValidations() {
		return true;
	}

	public boolean updateValidations() {
		return true;
	}

	/* Model methods */

	public String toString() {
		return "firstName: " + first_name +
				", lastName: " + last_name +
				", encryptedPassword: " + encrypted_password +
				", id: " + id +
				", signInCount: " + sign_in_count +
				", role: " + role;
	}
}
