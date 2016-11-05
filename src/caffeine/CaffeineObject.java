package caffeine;

import java.sql.*;
import java.util.*;

public interface CaffeineObject {

	/* Raw SQL execute, used for INSERT, UPDATE, DELETE */

	public default void executeUpdate(Connection c, PreparedStatement ps) throws SQLException {
		ps.executeUpdate();
		c.commit();
		c.close();
		ps.close();
		teardown();
	}

	public default void executeUpdate(String sql) throws SQLException {
		Connection c = setup();
		executeUpdate(c, c.prepareStatement(sql));
	}

	public default void executeUpdate(String sql, List<Object> values, Map<String, Object> options) throws SQLException {
		sql = appendOptions(sql, options);
		Connection c = setup();
		PreparedStatement ps = c.prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		executeUpdate(c, ps);
	}

	/* Raw SQL query, used for SELECT */

	public default List<CaffeineObject> executeQuery(PreparedStatement ps) throws SQLException {
		List<CaffeineObject> ret = new ArrayList<CaffeineObject>();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		Row.formTable(rs, table);
		for (HashMap<String, Object> row : table) {
			for (String column: row.keySet()) {
				this.setAttr(column, row.get(column));
		  }
			ret.add(copy(this));
		}
		teardown(rs, ps);
		return ret;
	}

	public default List<CaffeineObject> executeQuery(String sql) throws SQLException {
		return executeQuery(setup().prepareStatement(sql));
	}

	public default List<CaffeineObject> executeQuery(String sql, List<Object> values, Map<String, Object> options) throws SQLException {
		sql = appendOptions(sql, options);
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (Object value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		return executeQuery(ps);
	}

	public default List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws SQLException {
		String sql = "select * from " + this.getClass().getSimpleName().toLowerCase() + "s where ";
		List<String> keys = new ArrayList<>(args.keySet());
		for (int i = 0; i < keys.size(); i++) {
			sql = sql + keys.get(i) + " = ?";
			if ( (args.keySet().size() > 1) && (i != args.keySet().size() - 1) ) { sql = sql + " and "; }
		}
		sql = appendOptions(sql, options);
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (String column : keys) {
			ps.setObject(counter, args.get(column));
			counter++;
		}
		return executeQuery(ps);
	}

	/* AR-like querying methods */

	public default CaffeineObject find(int i) throws SQLException {
		Connection c = setup();
		PreparedStatement ps = c.prepareStatement("select * from " + this.getClass().getSimpleName() + "s where id = ?");
		ps.setInt(1, i);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			this.setAttrs(rs);
		}
		teardown(rs, ps);
		return this;
	}

	/* AR-like helper methods */

	public default String appendOptions(String sql, Map<String, Object> options) {
		if ((options != null) && (!options.isEmpty()) ) {
			sql = sql + " ";
			if (options.containsKey("groupBy")) { sql = sql + "group by " + options.get("groupBy") + " "; }
			if (options.containsKey("orderBy")) { sql = sql + "order by " + options.get("orderBy") + " "; }
			if (options.containsKey("limit")) { sql = sql + "limit " + options.get("limit") + " "; }
		}
		return sql;
	}

	public CaffeineObject copy(CaffeineObject obj);
	public void setAttrs(ResultSet rs) throws SQLException;
	public void setAttr(String column, Object value);

	/* Connection handling */

	public default Connection setup() {
		return Caffeine.caffeine.setup();
	}

	public default void teardown() {
		Caffeine.caffeine.teardown();
	}

	public default void teardown(ResultSet rs, PreparedStatement ps) throws SQLException {
		rs.close();
		ps.close();
		teardown();
	}

}
