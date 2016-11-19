package myApp;

import caffeine.*;
import java.util.ArrayList;
import java.util.List;

public class User implements CaffeineObject {

	/* Caffeine utilities */
	public static final String tableName = "users";
	public String currentQuery;
	public boolean firstCondition;
	public String validationErrors = "";
	public List<Object> placeholders = new ArrayList<Object>();

	// Associations
	@SuppressWarnings("rawtypes")
	public static List<Class> hasMany = new ArrayList<Class>();

	static {
		hasMany.add(Download.class);
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
