package supahnickie.caffeineTester;

import static org.junit.Assert.*;

import org.junit.*;
import supahnickie.caffeine.*;
import supahnickie.testClasses.*;

public class CaffeineTest {
	CaffeineObject databaseHandle;
	CaffeineObject userLookup;
	CaffeineObject downloadLookup;

	@Before
	public void setUp() throws Exception {
		// The database must already exist, but should be blank otherwise.
		new Caffeine(System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_TEST_URL"), System.getenv("CAFFEINE_DB_USERNAME"), System.getenv("CAFFEINE_DB_PASSWORD"));
		databaseHandle = new User();
		databaseHandle.executeUpdate("drop table if exists users");
		databaseHandle.executeUpdate("drop table if exists downloads");
		databaseHandle.executeUpdate("create table if not exists users (" +
			"id integer not null, " +
			"first_name varchar(255), " +
			"last_name varchar(255), " +
			"encrypted_password varchar(255), " +
			"sign_in_count integer, " +
			"role varchar(255))"
		);
		databaseHandle.executeUpdate("create table if not exists downloads (" +
			"id integer not null, " +
			"file_file_name varchar(255), " +
			"org_id integer, " +
			"user_id integer)"
		);
		insertUsers();
		userLookup = new User();
		downloadLookup = new Download();
	}

	@Test
	public void find() throws Exception {
		User user = (User) userLookup.find(13);
		assertEquals("ids should match", user.id, 13);
		assertEquals("first name should match", user.first_name, "Nick");
		assertEquals("last name should match", user.last_name, "Case");
		assertNotEquals("ids should not match others", user.id, 5);
		assertNotEquals("first name should not match others", user.first_name, "Grawr");
		assertNotEquals("last name should not match others", user.last_name, "McPhee");
	}

	@After
	public void tearDown() throws Exception {
		databaseHandle.executeUpdate("drop table if exists users");
		databaseHandle.executeUpdate("drop table if exists downloads");
	}

	// Test helper methods

	private void insertUsers() throws Exception {
		databaseHandle.executeUpdate("insert into users (id, first_name, last_name) values (13, 'Nick', 'Case')");
		databaseHandle.executeUpdate("insert into users (id, first_name, last_name) values (5, 'Grawr', 'McPhee')");
	}
}
