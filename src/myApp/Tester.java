package myApp;

import caffeine.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Tester {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		new Caffeine(System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_URL"), System.getenv("CAFFEINE_DB_USERNAME"), System.getenv("CAFFEINE_DB_PASSWORD"));

		/* Single model find */
		CaffeineObject singleUser = new User().find(2294);
		System.out.println(singleUser);

		/* AR-like find_by_sql */
		List<CaffeineObject> admins = new User().executeQuery("select * from users where role = 'super' limit 3");
		for(CaffeineObject admin : admins) {
			System.out.println(admin);
		}

		/* SQL fragment with placeholder values, List of arguments, and additional options */
		List<Object> list = new ArrayList<Object>();
		list.add("Smith");
		list.add("Bruce");
		Map<String, Object> optionsMap = new HashMap<String, Object>();
		optionsMap.put("limit", 5);
		optionsMap.put("orderBy", "created_at desc");
		List<CaffeineObject> smithUsers = new User().executeQuery("select * from users where last_name ilike ? or first_name ilike ?", list, optionsMap);
		for(CaffeineObject smithUser : smithUsers) {
			System.out.println(smithUser);
		}

		/* AR-like where with HashMap args and additional options */
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> otherOptionsMap = new HashMap<String, Object>();
		map.put("role", "super");
		map.put("first_name", "Elizabeth");
		map.put("sign_in_count", 1447);
		otherOptionsMap.put("limit", 2);
		List<CaffeineObject> superUser = new User().executeQuery(map, otherOptionsMap);
		for(CaffeineObject superU : superUser) {
			System.out.println(superU);
		}

		/* Execution of raw SQL updates */
		List<Object> insertArgs = new ArrayList<Object>();
		insertArgs.add("Grawr");
		insertArgs.add("McPhee");
		insertArgs.add("doesntmatter@something.com");
		new User().executeUpdate("insert into users (first_name, last_name, email) values (?, ?, ?)", insertArgs);
		List<CaffeineObject> users = new User().executeQuery("select * from users order by created_at desc limit 3");
		for(CaffeineObject user : users) {
			System.out.println(user);
		}

	}
}