package myApp;

import caffeine.*;

public class Download implements CaffeineObject {

	/* Caffeine utilities */
	private String currentQuery;
	private boolean firstCondition;

	/* Normal model attributes; names must map to database columns
	 * (if your db columns are lowerCamelCase, use that style) */
	public int id;
	public int org_id;
	public String file_file_name;

	public Download() {}

	/* Caffeine Getters */

	@SuppressWarnings("rawtypes")
	public Class getCurrentClass() { return Download.class; }
	public String getTableName() { return "downloads"; }
	public String getCurrentQuery() { return this.currentQuery; }
	public boolean getFirstCondition() { return this.firstCondition; }

	/* Caffeine Setters */

	public void setCurrentQuery(String sql) { this.currentQuery = sql; }
	public void setFirstCondition(Boolean bool) { this.firstCondition = bool; }

	/* Model Methods */

	public String toString() {
		return "fileName: " + file_file_name +
				", id: " + id +
				", orgId: " + org_id;
	}
}
