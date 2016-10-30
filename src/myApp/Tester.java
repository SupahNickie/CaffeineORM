package myApp;

import caffeine.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Tester {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		new Caffeine(System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_URL"), System.getenv("CAFFEINE_DB_USERNAME"), System.getenv("CAFFEINE_DB_PASSWORD"));

		/* Single model find */
		CaffeineObject singleUser = new User().find(2294);
		System.out.println(singleUser);

		/* AR-like find_by_sql */
		List<CaffeineObject> admins = new User().where("select * from users where role = 'super' limit 3");
		for(CaffeineObject admin : admins) {
			System.out.println(admin);
		}

		/* SQL fragment with placeholder values and List of arguments */
		List<Object> list = new ArrayList<Object>();
		list.add("Smith");
		list.add(5);
		List<CaffeineObject> smithUsers = new User().where("select * from users where last_name ilike ? limit ?", list);
		for(CaffeineObject smithUser : smithUsers) {
			System.out.println(smithUser);
		}

		/* AR-like where with HashMap args */
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("role", "super");
		map.put("first_name", "Elizabeth");
		map.put("sign_in_count", 1447);
		List<CaffeineObject> superUser = new User().where(map);
		for(CaffeineObject superU : superUser) {
			System.out.println(superU);
		}

	}
}
