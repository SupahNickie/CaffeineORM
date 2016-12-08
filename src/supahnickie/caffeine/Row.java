package supahnickie.caffeine;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;

final class Row {
	HashMap<String, Object> row;

	Row() {
		this.row = new HashMap<String, Object>();
	}

	final HashMap<String, Object> getRow() {
		return this.row;
	}

	final void add (String column, Object value) {
		this.getRow().put(column, value);
	}

	final static void formTable (ResultSet rs, List<HashMap<String, Object>> table) throws Exception {
		if (rs == null) return;

		ResultSetMetaData rsmd = rs.getMetaData();
		int numOfCol = rsmd.getColumnCount();
		while (rs.next()) {
			Row row = new Row();
			for (int i = 1; i <= numOfCol; i++) {
				row.add(rsmd.getColumnName(i), rs.getObject(i));
			}
			table.add(row.getRow());
		}
	}
}
