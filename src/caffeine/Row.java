package caffeine;

import java.sql.*;
import java.util.*;

public class Row {
	public HashMap<String, Object> row;

	public Row() {
		this.row = new HashMap<String, Object>();
	}

	public HashMap<String, Object> getRow() {
		return this.row;
	}

	public void add (String column, Object value) {
		getRow().put(column, value);
	}

	public static void formTable (ResultSet rs, List<HashMap<String, Object>> table) throws SQLException {
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
