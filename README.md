# Caffeine ORM
### An ActiveRecord-esque database wrapper for Java

Caffeine ORM is a wrapper used to handle the lower level database connections and SQL translation. It hydrates a `CaffeineObject` abstract type, with each model that needs to talk to a database in your application subclassing from `CaffeineObject`.

#### Initialization
```
Caffeine.setConfiguration(driver (example: "org.postgresql.Driver"), url, username, password));
```

#### Usage

Queries are done through the CaffeineObject abstract type; a lookup class must be declared in order to perform queries. This field is static on the Caffeine parent class and must be set manually whenever there is a change in the class you would like to lookup.
```
CaffeineObject user = CaffeineObject.find(User.class, id);

// Or, if doing multiple lookups on a given class

CaffeineObject.setQueryClass(Download.class);
CaffeineObject download = CaffeineObject.find(2);
CaffeineObject otherDownload = CaffeineObject.find(3);
```


Single model immediate find by id.
```
CaffeineObject singleUser = CaffeineObject.find(User.class, 2294);
```

CaffeineObject abstract types can be cast to the subclass concrete type.
```
User singleUser = (User) CaffeineObject.find(User.class, 2294);
```

Chainable AR-like `where` and `or`; no query execution until `execute()` is called at the end. `chainable()` must be called at the start and optionally contains the class to look up. If the optional param is not there, the current query class assigned to the Caffeine parent class will be used.
```
List<CaffeineObject> userResults = CaffeineObject.chainable(User.class).where("first_name ilike 'Nick'").where("last_name ilike ?", "Case").execute();

// Or, if doing multiple queries of the same class

CaffeineObject.setQueryClass(Download.class);
List<CaffeineObject> downloadResults = CaffeineObject.chainable().where("org_id = 167").or("org_id = 4").execute();
List<CaffeineObject> moreDownloads = CaffeineObject.chainable().where("file_file_name ilike ?", "hello there").execute();
List<CaffeineObject> moreComplexUserResults = CaffeineObject.chainable(User.class).where("last_name ilike ?", "Perez").where("id > ?", 50).execute();
```

Chainable AR-like `join` methods; specify (or don't) what type of join you would like. Default join is "join", which behaves like an inner join.
```
// select downloads.* from downloads join users on users.id = downloads.user_id where users.email ilike 'example1@somethingnew.com'
List<CaffeineObject> moreResults = CaffeineObject.chainable(Download.class).join("downloads.user_id", "users.id").where("users.email ilike ?", "example1@somethingnew.com").execute();
// select users.* from users join accounts on accounts.creator_id = user_id left outer join partners on partners.id = accounts.partner_id where partners.name = 'Huge Corporation'
List<CaffeineObject> multipleJoins = CaffeineObject.chainable(User.class).join("users.id", "accounts.creator_id").join("left outer", "accounts.partner_id", "partners.id").where("partners.name = 'Huge Corporation'").execute();
```

Raw SQL with immediate execution. The Caffeine type can be invoked statically but must be told what class objects to return.
```
CaffeineObject.setQueryClass(User.class);
List<CaffeineObject> admins = Caffeine.rawQuery("select * from users where role = 'super' limit 3");
```

SQL fragment with placeholder values, List of arguments, and additional options. As with any lookup in Caffeine, it must be told what class of objects to return. `IN` syntax is also supported.
```
List<Object> list = new ArrayList<Object>();
list.add("Smith");
list.add("Bruce");
List<Integer> idArg = new LinkedList<Integer>();
idArg.add(2);
idArg.add(3);
idArg.add(6);
list.add(idArg);
Map<String, Object> optionsMap = new HashMap<String, Object>();
optionsMap.put("limit", 5);
optionsMap.put("orderBy", "created_at desc");
CaffeineObject.setQueryClass(User.class);
List<CaffeineObject> smithUsers = Caffeine.rawQuery("select * from users where last_name ilike ? or first_name ilike ? or id in (?)", list, optionsMap);
// select * from users where last_name ilike 'Smith' or first_name ilike 'Bruce' or id in ( 2, 3, 6 ) order by created_at desc limit 5
```

PostgreSQL style named parameters can be used as well with a list or n number of arguments for either DB updates or selects. `IN` syntax is also supported.
```
List<Object> list = new ArrayList<Object>();
list.add(5);
list.add(8);
List<String> names = new ArrayList<String>();
names.add("Paul");
names.add("Bunyan");
names.add("The Third");
list.add(names);
Caffeine.setQueryClass(User.class);
List<CaffeineObject> users = Caffeine.rawQuery("select * from users where id = $1 or id = $2 or first_name in ($3) and sign_in_count > $2", list);
// select * from users where id = 5 or id = 8 or first_name in ('Paul', 'Bunyan', 'The Third') and sign_in_count > 8

List<String> names = new ArrayList<String>();
names.add("Paul");
names.add("Bunyan");
names.add("The Third");
list.add(names);
Caffeine.setQueryClass(User.class);
List<CaffeineObject> users = Caffeine.rawQuery("select * from users where id = $1 or id = $2 or first_name in ($3) and sign_in_count > $2", 5, 8, names);
// select * from users where id = 5 or id = 8 or first_name in ('Paul', 'Bunyan', 'The Third') and sign_in_count > 8

List<Object> list = new ArrayList<Object>();
list.add("Grawr");
list.add(3);
Caffeine.rawUpdate("insert into users set (favorite_number, sign_in_count, first_name, a_number_between_2_and_4) values ($2, $2, $1, $2)", list);

// This is equivalent to the above update
Caffeine.rawUpdate("insert into users set (favorite_number, sign_in_count, first_name, a_number_between_2_and_4) values ($2, $2, $1, $2)", "Grawr", 3);
```

AR-like where with HashMap args and additional options.
```
Map<String, Object> map = new HashMap<String, Object>();
Map<String, Object> otherOptionsMap = new HashMap<String, Object>();
map.put("role", "super");
map.put("first_name", "Elizabeth");
map.put("sign_in_count", 1447);
otherOptionsMap.put("limit", 2);
CaffeineObject.setQueryClass(User.class);
List<CaffeineObject> superUser = Caffeine.query(map, otherOptionsMap);
```

Associated models are set in a static block at the class level, then Caffeine knows how to query using a simple `getAssociated` method. Foreign keys are an optional second argument to the `getAssociated` function. See the provided example User and Download classes for more context.
```
CaffeineObject user = userLookup.find(37);
List<CaffeineObject> downloadsBelongingToThisUser = user.getAssociated(Download.class);
// select downloads.* from downloads where user_id = 37

CaffeineObject user = userLookup.find(37);
List<CaffeineObject> downloadsThisUserOrganizedInstead = user.getAssociated(Download.class, "org_id");
// select downloads.* from downloads where org_id = 37

CaffeineObject download = downloadLookup.find(1341);
List<CaffeineObject> userThatHasThisDownload = download.getAssociated(User.class);
// select users.* from users where id = 3 (the user_id of download with id 1341)

CaffeineObject download = downloadLookup.find(1341);
List<CaffeineObject> userThatOrganizedThisDownloadInstead = download.getAssociated(User.class, "org_id");
// select users.* from users where id = 741 (the org_id of download with id 1341)
```

Inserts, updates, and deletes can be handled with execution of raw SQL updates. Since the raw SQL update methods do not return anything, no query class is necessary for Caffeine to know about.
```
List<Object> insertArgs = new ArrayList<Object>();
insertArgs.add("Grawr");
insertArgs.add("McPhee");
insertArgs.add("something_else@example.com");
Caffeine.rawUpdate("insert into users (first_name, last_name, email) values (?, ?, ?)", insertArgs);

CaffeineObject.setQueryClass(User.class);
List<CaffeineObject> users = Caffeine.rawQuery("select * from users order by id desc limit 5");
```

Inserts, updates, and deletes can also be handled in a more AR-like format. The object `create` and `update` calls are performed on will have the inserted attributes on itself after execution.
```
Map<String, Object> createArgs = new HashMap<String, Object>();
createArgs.put("email", "superexample@example.com");
createArgs.put("first_name", "Stephen");
User newlyInstantiatedUser = (User) CaffeineObject.create(User.class, createArgs);

// Objects that have attributes in memory but are not persisted in the database yet also have an instance method to create them.

User anotherNewUser = new User();
anotherNewUser.first_name = "Hop";
anotherNewUser.last_name = "Skip, and a Jump";
anotherNewUser.create();

// Oops, I misspelled his first name; let's update the user.
createArgs.put("first_name", "Steven");
newlyInstantiatedUser.update(createArgs);

// Never mind, let's delete him.
newlyInstantiatedUser.delete();
```

Validations are handled with a handle into the `validate` abstract method on the implementing models. Please examine the User or Download example models for implementation details.

#### Tests

Tests are located in the supahnickie.caffeineTester package. To run the tests for yourself, create a blank test database and assign it to the `CAFFEINE_DB_TEST_URL` environment variable. Other environment variables needed to run the tests are `CAFFEINE_DB_DRIVER`, `CAFFEINE_DB_USERNAME`, and `CAFFEINE_DB_PASSWORD`.
