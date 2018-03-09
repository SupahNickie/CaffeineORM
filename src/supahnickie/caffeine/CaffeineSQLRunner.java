package supahnickie.caffeine;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CaffeineSQLRunner {

	private CaffeineSQLRunner() {}
	
	/* Insert, Update, Delete statements */

	static final void executeUpdate(CaffeinePooledConnection c, PreparedStatement ps) throws Exception {
		ps.executeUpdate();
		c.getConnection().commit();
		ps.close();
		CaffeineConnection.teardown(c);
	}

	static final CaffeineObject executeUpdate(CaffeinePooledConnection c, PreparedStatement ps, CaffeineObject instance) throws Exception {
		ps.executeUpdate();
		c.getConnection().commit();
		instance.setAttrsFromSqlReturn(ps.getGeneratedKeys());
		instance.setIsNewRecord(false);
		instance.captureCurrentStateOfAttrs();
		ps.close();
		CaffeineConnection.teardown(c);
		return instance;
	}

	static final void executeUpdate(String sql) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		executeUpdate(c, c.getConnection().prepareStatement(sql));
	}

	static final void executeUpdate(String sql, List<Object> values) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c.getConnection(), sql, values) : CaffeineParamReplacer.replaceJDBCParameters(c.getConnection(), sql, values);
		executeUpdate(c, ps);
	}

	static final void executeUpdate(String sql, Object... args) throws Exception {
		executeUpdate(sql, Arrays.asList(args));
	}

	static final void executeUpdate(String sql, Map<String, Object> args) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		PreparedStatement ps = CaffeineParamReplacer.replaceExactNamedParameters(c.getConnection(), sql, args);
		executeUpdate(c, ps);
	}

	static final CaffeineObject executeUpdate(String sql, Map<String, Object> args, List<Object> argKeys, CaffeineObject instance) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		PreparedStatement ps = c.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		insertValuesIntoQuery(ps, args, argKeys);
		return executeUpdate(c, ps, instance);
	}

	/* Select statements */

	static final List<HashMap<String, Object>> executeComplexQuery(CaffeinePooledConnection c, PreparedStatement ps) throws Exception {
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		CaffeineRow.formTable(rs, table);
		CaffeineConnection.teardown(c, rs, ps);
		return table;
	}

	static final List<HashMap<String, Object>> executeComplexQuery(String sql) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		return executeComplexQuery(c, c.getConnection().prepareStatement(sql));
	}

	static final List<HashMap<String, Object>> executeComplexQuery(String sql, List<Object> args) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c.getConnection(), sql, args) : CaffeineParamReplacer.replaceJDBCParameters(c.getConnection(), sql, args);
		return executeComplexQuery(c, ps);
	}

	static final List<HashMap<String, Object>> executeComplexQuery(String sql, Object... args) throws Exception {
		return executeComplexQuery(sql, Arrays.asList(args));
	}

	static final List<HashMap<String, Object>> executeComplexQuery(String sql, Map<String, Object> args) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		PreparedStatement ps = CaffeineParamReplacer.replaceExactNamedParameters(c.getConnection(), sql, args);
		return executeComplexQuery(c, ps);
	}

	static final List<CaffeineObject> executeQuery(CaffeinePooledConnection c, PreparedStatement ps) throws Exception {
		raiseExceptionIfNoQueryClass();
		List<HashMap<String, Object>> table = new ArrayList<HashMap<String, Object>>();
		ResultSet rs = ps.executeQuery();
		CaffeineRow.formTable(rs, table);
		List<CaffeineObject> ret = createListFromQueryReturn(table);
		CaffeineConnection.teardown(c, rs, ps);
		return ret;
	}

	static final List<CaffeineObject> executeQuery(String sql) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		return executeQuery(c, c.getConnection().prepareStatement(sql));
	}

	static final List<CaffeineObject> executeQuery(String sql, List<Object> args) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		PreparedStatement ps = (sql.contains("$")) ? CaffeineParamReplacer.replaceNamedParameters(c.getConnection(), sql, args) : CaffeineParamReplacer.replaceJDBCParameters(c.getConnection(), sql, args);
		return executeQuery(c, ps);
	}

	static final List<CaffeineObject> executeQuery(String sql, Object... args) throws Exception {
		return executeQuery(sql, Arrays.asList(args));
	}

	static final List<CaffeineObject> executeQuery(Map<String, Object> args) throws Exception {
		CaffeinePooledConnection c = CaffeineConnection.setup();
		String sql = CaffeineObject.baseQuery() + " where ";
		List<Object> argKeys = new ArrayList<Object>(args.keySet());
		sql = buildRawQueryFromMapArgs(sql, args, argKeys);
		PreparedStatement ps = c.getConnection().prepareStatement(sql);
		insertValuesIntoQuery(ps, args, argKeys);
		return executeQuery(c, ps);
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

	private static final void raiseExceptionIfNoQueryClass() throws Exception {
		if (CaffeineConnection.getQueryClass() == null) CaffeineConnection.raiseNoQuerySetException();
	}
}