# Caffeine ORM
### An ActiveRecord-esque database wrapper for Java

Caffeine ORM is a wrapper used to handle the lower level database connections and SQL translation. It hydrates a `CaffeineObject` abstract type, with each model in
your application implementing the `CaffeineObject` interface.

#### Initialization
```
new Caffeine(driver (example: "org.postgresql.Driver"), url, username, password);
```

#### Usage

Init lookup query object through the Caffeine Interface abstract type; use this to run queries with for a particular class.
```
CaffeineObject userLookup = new User();
CaffeineObject downloadLookup = new Download();
```


Single model immediate find by id.
```
CaffeineObject singleUser = userLookup.find(2294);
```

CaffeineObject abstract types can be cast to the subclass concrete type.
```
User singleUser = (User) userLookup.find(2294);
```

Chainable AR-like `where` and `or`; no query execution until `execute()` is called at the end.
```
List<CaffeineObject> userResults = userLookup.where("first_name ilike 'Nick'").where("last_name ilike '?'", "Case").execute();
List<CaffeineObject> downloadResults = downloadLookup.where("org_id = 167").or("org_id = 4").execute();
List<CaffeineObject> moreComplexUserResults = userLookup.where("last_name ilike '?'", "Perez").where("id > ?", 50).execute();
```

Raw SQL with immediate execution.
```
List<CaffeineObject> admins = userLookup.executeQuery("select * from users where role = 'super' limit 3");
```

SQL fragment with placeholder values, List of arguments, and additional options.
```
List<Object> list = new ArrayList<Object>();
list.add("Smith");
list.add("Bruce");
Map<String, Object> optionsMap = new HashMap<String, Object>();
optionsMap.put("limit", 5);
optionsMap.put("orderBy", "created_at desc");
List<CaffeineObject> smithUsers = userLookup.executeQuery("select * from users where last_name ilike ? or first_name ilike ?", list, optionsMap);
```

AR-like where with HashMap args and additional options.
```
Map<String, Object> map = new HashMap<String, Object>();
Map<String, Object> otherOptionsMap = new HashMap<String, Object>();
map.put("role", "super");
map.put("first_name", "Elizabeth");
map.put("sign_in_count", 1447);
otherOptionsMap.put("limit", 2);
List<CaffeineObject> superUser = userLookup.executeQuery(map, otherOptionsMap);
```

Inserts, updates, and deletes are handled with execution of raw SQL updates.
```
List<Object> insertArgs = new ArrayList<Object>();
insertArgs.add("Grawr");
insertArgs.add("McPhee");
insertArgs.add("something_else@example.com");
userLookup.executeUpdate("insert into users (first_name, last_name, email) values (?, ?, ?)", insertArgs);
List<CaffeineObject> users = userLookup.executeQuery("select * from users order by id desc limit 5");
```

##### Todos:
- Joins
- Easy associations between models
- `create`, `update`, and `delete` methods
