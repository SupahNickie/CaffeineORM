package caffeine;

import java.sql.*;
import java.util.*;

public interface CaffeineObject {
	
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
	
	public default List<CaffeineObject> where(PreparedStatement ps) throws SQLException {
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

	public default List<CaffeineObject> where(String sql) throws SQLException {
		return where(setup().prepareStatement(sql));
	}
	
	public default <T> List<CaffeineObject> where(String sql, List<T> values) throws SQLException {
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (T value : values) {
			ps.setObject(counter, value);
			counter++;
		}
		return where(ps);
	}
	
	public default <T> List<CaffeineObject> where(HashMap<String, T> args) throws SQLException {
		String sql = "select * from " + this.getClass().getSimpleName().toLowerCase() + "s where ";
		List<String> keys = new ArrayList<>(args.keySet());
		for (int i = 0; i < keys.size(); i++) {
			sql = sql + keys.get(i) + " = ?";
			if ( (args.keySet().size() > 1) && (i != args.keySet().size() - 1) ) { sql = sql + " and "; }
		}
		PreparedStatement ps = setup().prepareStatement(sql);
		int counter = 1;
		for (String column : keys) {
			ps.setObject(counter, args.get(column));
			counter++;
		}
		return where(ps);
	}
	
	public CaffeineObject copy(CaffeineObject obj);
	public void setAttrs(ResultSet rs) throws SQLException;
	public void setAttr(String column, Object value);
	
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