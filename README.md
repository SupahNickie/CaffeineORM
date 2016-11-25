# Caffeine ORM
### An ActiveRecord-esque database wrapper for Java

Caffeine ORM is a wrapper used to handle the lower level database connections and SQL translation. It hydrates a `CaffeineObject` abstract type, with each model that needs to talk to a database in your application sublassing from `CaffeineObject`.

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
List<CaffeineObject> userResults = userLookup.where("first_name ilike 'Nick'").where("last_name ilike ?", "Case").execute();
List<CaffeineObject> downloadResults = downloadLookup.where("org_id = 167").or("org_id = 4").execute();
List<CaffeineObject> moreComplexUserResults = userLookup.where("last_name ilike ?", "Perez").where("id > ?", 50).execute();
```

Chainable AR-like `join` methods; specify (or don't) what type of join you would like. Default join is "join", which behaves like an inner join.
```
// select downloads.* from downloads join users on users.id = downloads.user_id where users.email ilike 'example1@somethingnew.com'
List<CaffeineObject> moreResults = downloadLookup.join("downloads.user_id", "users.id").where("users.email ilike ?", "example1@somethingnew.com").execute();
// select users.* from users join accounts on accounts.creator_id = user_id left outer join partners on partners.id = accounts.partner_id where partners.name = 'Huge Corporation'
List<CaffeineObject> multipleJoins = userLookup.join("users.id", "accounts.creator_id").join("left outer", "accounts.partner_id", "partners.id").where("partners.name = 'Huge Corporation'").execute();
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

Associated models are set in a static block at the class level, then Caffeine knows how to query using a simple `getAssociated` method. Foreign keys are an optional second argument to the `getAssociated` function. See the provided example User and Download classes for more context.
```
CaffeineObject user = userLookup.find(37);
List<CaffeineObject> downloadsBelongingToThisUser = user.getAssociated(downloadLookup);
// select downloads.* from downloads where user_id = 37

CaffeineObject user = userLookup.find(37);
List<CaffeineObject> downloadsThisUserOrganizedInstead = user.getAssociated(downloadLookup, "org_id");
// select downloads.* from downloads where org_id = 37

CaffeineObject download = downloadLookup.find(1341);
List<CaffeineObject> userThatHasThisDownload = download.getAssociated(userLookup);
// select users.* from users where id = 3 (the user_id of download with id 1341)

CaffeineObject download = downloadLookup.find(1341);
List<CaffeineObject> userThatOrganizedThisDownloadInstead = download.getAssociated(userLookup, "org_id");
// select users.* from users where id = 741 (the org_id of download with id 1341)
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

Inserts, updates, and deletes can also be handled in a more AR-like format. The object `create` and `update` calls are performed on will have the inserted attributes on itself after execution.
```
Map<String, Object> createArgs = new HashMap<String, Object>();
createArgs.put("email", "superexample@example.com");
createArgs.put("first_name", "Stephen");
User newlyInstantiatedUser = new User();
newlyInstantiatedUser.create(createArgs);

// Oops, I misspelled his first name; let's update the user.
createArgs.put("first_name", "Steven");
newlyInstantiatedUser.update(createArgs);

// Never mind, let's delete him.
newlyInstantiatedUser.delete();
```

Validations are handled with a handle into the `validate` abstract method on the implementing models. Please examine the User or Download example models for implementation details.

#### Tests

Tests are located in the supahnickie.caffeineTester package. To run the tests for yourself, create a blank test database and assign it to the `CAFFEINE_DB_TEST_URL` environment variable. Other environment variables needed to run the tests are `CAFFEINE_DB_DRIVER`, `CAFFEINE_DB_USERNAME`, and `CAFFEINE_DB_PASSWORD`.
