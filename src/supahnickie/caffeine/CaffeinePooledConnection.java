package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.DriverManager;

class CaffeinePooledConnection {
	private Connection connection;
	private boolean isBusy;

	CaffeinePooledConnection(Object[] creds) {
		String dbDriver = (String) creds[0];
		String dbUrl = (String) creds[1];
		String dbUsername = (String) creds[2];
		String dbPassword = (String) creds[3];
		Connection connection = null;
		try {
			Class.forName(dbDriver);
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
			connection.setAutoCommit(false);
			this.connection = connection;
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.isBusy = false;
	}

	Connection getConnection() { return this.connection; }

	boolean isBusy() { return this.isBusy; }

	void setIsBusy(boolean state) { this.isBusy = state; }
}