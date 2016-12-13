package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CaffeineSQLRunner {
	/* Insert, Update, Delete statements */

	static final void executeUpdate(Connection c, PreparedStatement ps) throws Exception {
		ps.executeUpdate();
		c.commit();
		c.close();
		ps.close();
		CaffeineConnection.teardown();
	}

	static final CaffeineObject executeUpdate(Connection c, PreparedStatement ps, CaffeineObject instance) throws Exception {
		ps.executeUpdate();
		c.commit();
		instance.setAttrsFromSqlReturn(ps.getGeneratedKeys());
		instance.setIsNewRecord(false);
		instance.captureCurrentStateOfAttrs();
		c.close();
		ps.close();
		CaffeineConnection.teardown();
		return instance;
	}

	static final void executeUpdate(String sql) throws Exception {
		Connection c = CaffeineConnection.setup();
		executeUpdate(c, c.prepareStatement(sql));
	}

	static final void executeUpdate(String sql, List<Object> values) throws Exception {
		Connection c = CaffeineConnection.setup();
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c, sql, values) : CaffeineParamReplacer.replaceJDBCParameters(c, sql, values);
		executeUpdate(c, ps);
	}

	static final void executeUpdate(String sql, Object... args) throws Exception {
		executeUpdate(sql, Arrays.asList(args));
	}

	static final CaffeineObject executeUpdate(String sql, Map<String, Object> args, List<Object> argKeys, CaffeineObject instance) throws Exception {
		Connection c = CaffeineConnection.setup();
		PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		insertValuesIntoQuery(ps, args, argKeys);
		return executeUpdate(c, ps, instance);
	}

	/* Select statements */

	static final List<HashMap<String, Object>> executeComplexQuery(PreparedStatement ps) throws Exception {
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		CaffeineRow.formTable(rs, table);
		CaffeineConnection.teardown(rs, ps);
		return table;
	}

	static final List<HashMap<String, Object>> executeComplexQuery(String sql) throws Exception {
		return executeComplexQuery(CaffeineConnection.setup().prepareStatement(sql));
	}

	static final List<HashMap<String, Object>> executeComplexQuery(String sql, List<Object> args) throws Exception {
		Connection c = CaffeineConnection.setup();
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c, sql, args) : CaffeineParamReplacer.replaceJDBCParameters(c, sql, args);
		return executeComplexQuery(ps);
	}

	static final List<HashMap<String, Object>> executeComplexQuery(String sql, Object... args) throws Exception {
		return executeComplexQuery(sql, Arrays.asList(args));
	}

	static final List<CaffeineObject> executeQuery(PreparedStatement ps) throws Exception {
		raiseExceptionIfNoQueryClass();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		CaffeineRow.formTable(rs, table);
		List<CaffeineObject> ret = createListFromQueryReturn(table);
		CaffeineConnection.teardown(rs, ps);
		return ret;
	}

	static final List<CaffeineObject> executeQuery(String sql) throws Exception {
		return executeQuery(CaffeineConnection.setup().prepareStatement(sql));
	}

	static final List<CaffeineObject> executeQuery(String sql, List<Object> args, Map<String, Object> options) throws Exception {
		Connection c = CaffeineConnection.setup();
		if (!(options == null)) { sql = appendOptions(sql, options); }
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c, sql, args) : CaffeineParamReplacer.replaceJDBCParameters(c, sql, args);
		return executeQuery(ps);
	}

	static final List<CaffeineObject> executeQuery(String sql, Object... args) throws Exception {
		return executeQuery(sql, Arrays.asList(args), null);
	}

	static final List<CaffeineObject> executeQuery(Map<String, Object> args, Map<String, Object> options) throws Exception {
		String sql = CaffeineObject.baseQuery() + " where ";
		List<Object> argKeys = new ArrayList<Object>(args.keySet());
		sql = buildRawQueryFromMapArgs(sql, args, argKeys);
		if (!(options == null)) { sql = appendOptions(sql, options); }
		PreparedStatement ps = CaffeineConnection.setup().prepareStatement(sql);
		insertValuesIntoQuery(ps, args, argKeys);
		return executeQuery(ps);
	}

	/* Helper methods */

	private static final List<CaffeineObject> createListFromQueryReturn(List<HashMap<String, Object>> table) throws Exception {
		List<CaffeineObject> ret = new ArrayList<CaffeineObject>();
		for (HashMap<String, Object> row : table) {
			CaffeineObject newInstance = (CaffeineObject) CaffeineConnection.getQueryClass().newInstance();
			newInstance.setIsNewRecord(false);
			for (String column: row.keySet()) {
				newInstance.setAttr(column, row.get(column));
			}
			newInstance.captureCurrentStateOfAttrs();
			ret.add(newInstance);
		}
		return ret;
	}

	private static final String buildRawQueryFromMapArgs(String sql, Map<String, Object> args, List<Object> argKeys) throws Exception {
		for (int i = 0; i < argKeys.size(); i++) {
			sql = sql + argKeys.get(i) + " = ?";
			if ( (args.keySet().size() > 1) && (i != args.keySet().size() - 1) ) { sql = sql + " and "; }
		}
		return sql;
	}

	private static final void insertValuesIntoQuery(PreparedStatement ps, Map<String, Object> args, List<Object> argKeys) throws Exception {
		int counter = 1;
		for (int i = 0; i < argKeys.size(); i++) {
			ps.setObject(counter, args.get(argKeys.get(i)));
			counter++;
		}
	}

	private static final String appendOptions(String sql, Map<String, Object> options) {
		if ((options != null) && (!options.isEmpty()) ) {
			sql = sql + " ";
			if (options.containsKey("groupBy")) { sql = sql + "group by " + options.get("groupBy") + " "; }
			if (options.containsKey("orderBy")) { sql = sql + "order by " + options.get("orderBy") + " "; }
			if (options.containsKey("limit")) { sql = sql + "limit " + options.get("limit") + " "; }
		}
		return sql;
	}

	private static final void raiseExceptionIfNoQueryClass() throws Exception {
		if (CaffeineConnection.getQueryClass() == null) CaffeineConnection.raiseNoQuerySetException();
	}
}