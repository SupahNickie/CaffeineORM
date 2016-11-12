package myApp;

import caffeine.*;

public class User implements CaffeineObject {

	/* Caffeine utilities */
	private String currentQuery;
	private boolean firstCondition;

	/* Normal model attributes; names must map to database columns
	 * (if your db columns are lowerCamelCase, use that style) */
	public int id;
	public String first_name;
	public String last_name;
	public String encrypted_password;
	public int sign_in_count;
	public String role;

	public User() {}

	/* Caffeine Getters */

	@SuppressWarnings("rawtypes")
	public Class getCurrentClass() { return User.class; }
	public String getTableName() { return "users"; }
	public String getCurrentQuery() { return this.currentQuery; }
	public boolean getFirstCondition() { return this.firstCondition; }

	/* Caffeine Setters */

	public void setCurrentQuery(String sql) { this.currentQuery = sql; }
	public void setFirstCondition(Boolean bool) { this.firstCondition = bool; }

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
