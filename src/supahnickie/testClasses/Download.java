package supahnickie.testClasses;

import supahnickie.caffeine.*;

import java.util.HashMap;
import java.util.Map;

public class Download extends CaffeineObject {

	/* Caffeine utilities */
	static final String tableName = "downloads";

	// Associations
	@SuppressWarnings("rawtypes")
	public static Map<Class, String> caffeineAssociations = new HashMap<Class, String>();

	static {
		caffeineAssociations.put(User.class, "belongsTo");
	}

	/* Normal model attributes; names must map to database columns
	 * (if your db columns are lowerCamelCase, use that style) */
	public int id;
	public int org_id;
	public String file_file_name;
	public int user_id;

	public Download() throws Exception {
		init();
	}

	/* Validations */

	public boolean validate(String validationType) {
		validationErrors = "";
		return true;
	}

	/* Model Methods */

	public String toString() {
		return "fileName: " + file_file_name +
				", id: " + id +
				", orgId: " + org_id +
				", userId: " + user_id;
	}
}
