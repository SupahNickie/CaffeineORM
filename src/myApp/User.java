package myApp;

import caffeine.*;

public class User implements CaffeineObject {

	/* Caffeine utilities */
	public static final String tableName = "users";
	public String currentQuery;
	public boolean firstCondition;

	/* Normal model attributes; names must map to database columns
	 * (if your db columns are lowerCamelCase, use that style) */
	public int id;
	public String first_name;
	public String last_name;
	public String encrypted_password;
	public int sign_in_count;
	public String role;

	public User() {}

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
