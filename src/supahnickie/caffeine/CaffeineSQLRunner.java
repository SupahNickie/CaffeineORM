package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CaffeineSQLRunner {
	static final void executeUpdate(Connection c, PreparedStatement ps) throws Exception {
		ps.executeUpdate();
		c.commit();
		c.close();
		ps.close();
		Caffeine.teardown();
	}

	public static final void executeUpdate(String sql) throws Exception {
		Connection c = Caffeine.setup();
		executeUpdate(c, c.prepareStatement(sql));
	}

	public static final void executeUpdate(String sql, List<Object> values) throws Exception {
		Connection c = Caffeine.setup();
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c, sql, values) : CaffeineParamReplacer.replaceJDBCParameters(c, sql, values);
		executeUpdate(c, ps);
	}

	static final CaffeineObject executeUpdate(Connection c, PreparedStatement ps, CaffeineObject instance) throws Exception {
		ps.executeUpdate();
		c.commit();
		instance.setAttrsFromSqlReturn(ps.getGeneratedKeys());
		c.close();
		ps.close();
		Caffeine.teardown();
		return instance;
	}

	static final CaffeineObject executeUpdate(String sql, Map<String, Object> args, Object[] argKeys, CaffeineObject instance) throws Exception {
		Connection c = Caffeine.setup();
		PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int counter = 1;
		for (int i = 0; i < argKeys.length; i++) {
			ps.setObject(counter, args.get(argKeys[i]));
			counter++;
		}
		return executeUpdate(c, ps, instance);
	}

	/* Select statements */

	static final List<CaffeineObject> executeQuery(PreparedStatement ps) throws Exception {
		List<CaffeineObject> ret = new ArrayList<CaffeineObject>();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		Row.formTable(rs, table);
		for (HashMap<String, Object> row : table) {
			CaffeineObject newInstance = (CaffeineObject) Caffeine.getQueryClass().newInstance();
			for (String column: row.keySet()) {
				newInstance.setAttr(column, row.get(column));
			}
			ret.add(newInstance);
		}
		Caffeine.teardown(rs, ps);
		return ret;
	}

	public static final List<CaffeineObject> executeQuery(String sql) throws Exception {
		return executeQuery(Caffeine.setup().prepareStatement(sql));
	}

	public static final List<CaffeineObject> executeQuery(String sql, List<Object> values, Map<String, Object> options) throws Exception {
		Connection c = Caffeine.setup();
		if (!(options == null)) { sql = CaffeineObject.appendOptions(sql, options); }
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c, sql, values) : CaffeineParamReplacer.replaceJDBCParameters(c, sql, values);
		return executeQuery(ps);
	}

	public static final List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws Exception {
		String sql = CaffeineObject.baseQuery() + " where ";
		List<String> keys = new ArrayList<>(args.keySet());
		for (int i = 0; i < keys.size(); i++) {
			sql = sql + keys.get(i) + " = ?";
			if ( (args.keySet().size() > 1) && (i != args.keySet().size() - 1) ) { sql = sql + " and "; }
		}
		if (!(options == null)) { sql = CaffeineObject.appendOptions(sql, options); }
		PreparedStatement ps = Caffeine.setup().prepareStatement(sql);
		int counter = 1;
		for (String column : keys) {
			ps.setObject(counter, args.get(column));
			counter++;
		}
		return executeQuery(ps);
	}
}