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
```

##### Todos:
- More generic iteration of the `setAttrs`, `setAttr`, and copycat constructor methods so there's less to do in the application's model classes
- Chainable where clauses that do not execute immediately
- Options for limit, order, joins
- Allow array arguments to support queries like `where first_name in ('Bob', 'Gerald')`
- Create, update, delete, find_or_create functions
