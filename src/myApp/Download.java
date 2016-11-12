package myApp;

import caffeine.*;

public class Download implements CaffeineObject {

	/* Caffeine utilities */
	public static final String tableName = "downloads";
	public String currentQuery;
	public boolean firstCondition;

	/* Normal model attributes; names must map to database columns
	 * (if your db columns are lowerCamelCase, use that style) */
	public int id;
	public int org_id;
	public String file_file_name;

	public Download() {}

	/* Model Methods */

	public String toString() {
		return "fileName: " + file_file_name +
				", id: " + id +
				", orgId: " + org_id;
	}
}
