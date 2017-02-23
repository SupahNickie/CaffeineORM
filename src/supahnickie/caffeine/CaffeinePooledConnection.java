package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * CaffeinePooledConnection is a small utility class used mostly internally to generate a connection pool. However, some of
 * the methods are public to allow a user to generate reusable PreparedStatements.
 *
 * @author Nicholas Case (nicholascase@live.com)
 * @version 5.3.0
 * @see <a href="https://github.com/SupahNickie/CaffeineORM/blob/master/README.md">README containing examples, including initialization</a>
 */
public final class CaffeinePooledConnection {
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

	/**
	 * Method to allow users to generate a PreparedStatement on the PooledConnection object so they don't have to manage resources themselves.
	 * @param sql Raw sql to be executed, as with any normal PreparedStatement.
	 * @return PreparedStatement object that can be reused.
	 * @throws Exception
	 */
	public PreparedStatement prepareStatement(String sql) throws Exception {
		return this.getConnection().prepareStatement(sql);
	}

	/**
	 * Grabs the connection instance for a particular Pooled Connection.
	 * @return Raw DB connection that will stay open and out of the connection pool until the user manually releases it.
	 */
	public Connection getConnection() { return this.connection; }

	boolean isBusy() { return this.isBusy; }

	void setIsBusy(boolean state) { this.isBusy = state; }
}