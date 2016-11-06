package myApp;

import caffeine.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User implements CaffeineObject {
	/* Caffeine utilities */
	private String currentQuery;
	private boolean firstCondition;

	/* Normal model attributes */
	public int id;
	public String firstName;
	public String lastName;
	public String encryptedPassword;
	public int signInCount;
	public String role;

	public User() {}

	/* Getters */

	@SuppressWarnings("rawtypes")
	public Class getCurrentClass() { return User.class; }
	public String getTableName() { return "users"; }
	public String getCurrentQuery() { return this.currentQuery; }
	public boolean getFirstCondition() { return this.firstCondition; }

	/* Setters */

	public void setCurrentQuery(String sql) { this.currentQuery = sql; }
	public void setFirstCondition(Boolean bool) { this.firstCondition = bool; }

	public void setAttrs(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.firstName = rs.getString("first_name");
		this.lastName = rs.getString("last_name");
		this.encryptedPassword = rs.getString("encrypted_password");
		this.signInCount = rs.getInt("sign_in_count");
		this.role = rs.getString("role");
	}

	public void setAttr(String column, Object value) {
		switch (column) {
			case "id": this.id = (Integer)value;
				break;
			case "first_name": this.firstName = (String)value;
				break;
			case "last_name": this.lastName = (String)value;
				break;
			case "encrypted_password": this.encryptedPassword = (String)value;
				break;
			case "sign_in_count": this.signInCount = (Integer)value;
				break;
			case "role": this.role = (String)value;
				break;
			default: break;
		}
	}

	/* Model methods */

	public String toString() {
		return "firstName: " + firstName +
				", lastName: " + lastName +
				", encryptedPassword: " + encryptedPassword +
				", id: " + id +
				", signInCount: " + signInCount +
				", role: " + role;
	}
}
