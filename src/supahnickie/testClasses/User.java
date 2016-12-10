package supahnickie.testClasses;

import supahnickie.caffeine.*;

import java.util.HashMap;
import java.util.Map;

public class User extends CaffeineObject {

	/* Caffeine utilities */
	static final String tableName = "users";

	// Associations
	@SuppressWarnings("rawtypes")
	public static Map<Class, String> caffeineAssociations = new HashMap<Class, String>();

	static {
		caffeineAssociations.put(Download.class, "hasMany");
	}

	/* Normal model attributes; names must map to database columns
	 * (if your db columns are lowerCamelCase, use that style) */
	private int id;
	private String first_name;
	private String last_name;
	private String encrypted_password;
	private int sign_in_count;
	private String role;

	public User() {}

	/* Validations */

	public boolean validate(String validationType) {
		validationErrors = "";
		if (validationType.equals("create")) {
			return createValidations();
		} else {
			return updateValidations();
		}
	}

	public boolean createValidations() {
		boolean valid = true;
		if (this.getFirstName().equals("illegal first name")) {
			validationErrors = validationErrors + "* first_name of " + this.getFirstName() + " is not allowed for a User *";
			valid = false;
		}
		return valid;
	}

	public boolean updateValidations() {
		boolean valid = true;
		if (this.getLastName().equals("another illegal name")) {
			validationErrors = validationErrors + "* last_name of " + this.getLastName() + " is not allowed for a User *";
			valid = false;
		}
		return valid;
	}

	/* Normal Getters */

	public int getId() { return this.id; }
	public String getFirstName() { return this.first_name; }
	public String getLastName() { return this.last_name; }
	public String getEncryptedPassword() { return this.encrypted_password; }
	public int getSignInCount() { return this.sign_in_count; }
	public String getRole() { return this.role; }

	/* Normal Setters */

	public void setId(int id) { this.id = id; }
	public void setFirstName(String firstName) { this.first_name = firstName; }
	public void setLastName(String lastName) { this.last_name = lastName; }
	public void setEncryptedPassword(String encryptedPassword) { this.encrypted_password = encryptedPassword; }
	public void setSignInCount(int signInCount) { this.sign_in_count = signInCount; }
	public void setRole(String role) { this.role = role; }

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
