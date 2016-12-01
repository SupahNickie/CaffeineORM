package supahnickie.caffeine;

import java.sql.*;

public final class Caffeine {
	private static String dbDriver;
	private static String dbUrl;
	private static String dbUsername;
	private static String dbPassword;
	private static Connection connection;

	private Caffeine() {}
	
	public static void setConfiguration(String driver, String url, String username, String password) {
		dbDriver = driver;
		dbUrl = url;
		dbUsername = username;
		dbPassword = password;
	}

	final static Connection setup() {
		connection = null;
		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			connection.setAutoCommit(false);
			return connection;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	final static void teardown() {
		try {
			connection.close();
			connection = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
