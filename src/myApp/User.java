package myApp;

import caffeine.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User implements CaffeineObject {
	public int id;
	public String firstName;
	public String lastName;
	public String encryptedPassword;
	public int signInCount;
	public String role;
	
	public User() {}
	
	public User(User copy) {
		id = copy.id;		
		firstName = copy.firstName;
		lastName = copy.lastName;
		encryptedPassword = copy.encryptedPassword;
		signInCount = copy.signInCount;		
		role = copy.role;
	}
	
	public CaffeineObject copy(CaffeineObject obj) {
		return new User(this);
	}
	
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
	
	public String toString() {
		return "firstName: " + firstName + 
				", lastName: " + lastName + 
				", encryptedPassword: " + encryptedPassword + 
				", id: " + id + 
				", signInCount: " + signInCount + 
				", role: " + role;
	}
}