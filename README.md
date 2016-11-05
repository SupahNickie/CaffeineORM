# Caffeine ORM
### An ActiveRecord-esque database wrapper for Java

##### Please note, this ORM is a POC/WIP, not meant to be used in an actual project as of yet.

Caffeine ORM is a wrapper used to handle the lower level database connections and SQL translation. It hydrates a `CaffeineObject` abstract type, with each model in
your application implementing the `CaffeineObject` interface.

##### Initialization
```
new Caffeine(System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_URL"), System.getenv("CAFFEINE_DB_USERNAME"), System.getenv("CAFFEINE_DB_PASSWORD"));
```

Feel free to examine the `Tester.java` file in the example `myApp` package to see it in context.

##### Usage
```
		/* Single model find */
		User singleUser = (User) User.find(2294, User.class);
		System.out.println(singleUser);

		/* AR-like find_by_sql */
		List<CaffeineObject> admins = User.executeQuery("select * from users where role = 'super' limit 3", User.class);
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
		List<CaffeineObject> smithUsers = User.executeQuery("select * from users where last_name ilike ? or first_name ilike ?", list, optionsMap, User.class);
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
		List<CaffeineObject> superUser = User.executeQuery(map, otherOptionsMap, User.class);
		for(CaffeineObject superU : superUser) {
			System.out.println(superU);
		}

		/* Execution of raw SQL updates */
		List<Object> insertArgs = new ArrayList<Object>();
		insertArgs.add("Grawr");
		insertArgs.add("McPhee");
		insertArgs.add("something_else@example.com");
		User.executeUpdate("insert into users (first_name, last_name, email) values (?, ?, ?)", insertArgs);
		List<CaffeineObject> users = User.executeQuery("select * from users order by created_at desc limit 5", User.class);
		for(CaffeineObject user : users) {
			System.out.println(user);
		}
```

##### Todos:
- More generic iteration of the `setAttrs`, `setAttr`, and copycat constructor methods so there's less to do in the application's model classes
- Chainable where clauses that do not execute immediately
- Options for limit, order, joins
