package supahnickie.caffeine;

import java.sql.*;

public final class Caffeine {
	public static Caffeine caffeine;
	DBConnection connector;
	Connection connection;

	public Caffeine(String driver, String url, String username, String password) {
		caffeine = this;
		connector = new DBConnection(driver, url, username, password);
	}

	public Connection setup() {
		if (connection == null) { setConnection(); }
		return this.connection;
	}

	public void setConnection() {
		connection = connector.openConnection();
	}

	public void teardown() {
		connection = null;
		connector.closeConnection();
	}

	private final class DBConnection {
		private String driver;
		private String url;
		private String username;
		private String password;
		private Connection c;

		DBConnection(String driver, String url, String username, String password) {
			this.driver = driver;
			this.url = url;
			this.username = username;
			this.password = password;
		}

		public Connection openConnection() {
			c = null;
			try {
				Class.forName(this.driver);
				c = DriverManager.getConnection(this.url, this.username, this.password);
				c.setAutoCommit(false);
				return c;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public void closeConnection() {
			try {
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
