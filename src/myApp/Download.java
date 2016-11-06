package myApp;

import caffeine.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Download implements CaffeineObject {
	/* Caffeine utilities */
	private String currentQuery;
	private boolean firstCondition;

	/* Normal model attributes */
	public int id;
	public int orgId;
	public String fileName;

	public Download() {}

	/* Getters */

	@SuppressWarnings("rawtypes")
	public Class getCurrentClass() { return Download.class; }
	public String getTableName() { return "downloads"; }
	public String getCurrentQuery() { return this.currentQuery; }
	public boolean getFirstCondition() { return this.firstCondition; }

	/* Setters */

	public void setCurrentQuery(String sql) { this.currentQuery = sql; }
	public void setFirstCondition(Boolean bool) { this.firstCondition = bool; }

	public void setAttrs(ResultSet rs) throws SQLException {
		this.id = rs.getInt("id");
		this.orgId = rs.getInt("org_id");
		this.fileName = rs.getString("file_file_name");
	}

	public void setAttr(String column, Object value) {
		switch (column) {
			case "id": this.id = (Integer)value;
				break;
			case "org_id": this.orgId = (Integer)value;
				break;
			case "file_file_name": this.fileName = (String)value;
				break;
			default: break;
		}
	}

	/* Model Methods */

	public String toString() {
		return "fileName: " + fileName +
				", id: " + id +
				", orgId: " + orgId;
	}
}
