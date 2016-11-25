package supahnickie.caffeineTester;

import static org.junit.Assert.*;

import org.junit.*;
import java.util.List;
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
		userLookup = new User();
		downloadLookup = new Download();
		insertTables();
		insertUsers();
	}

	@Test
	public void executeUpdate() throws Exception {
		downloadLookup.executeUpdate("insert into downloads (id, org_id, file_file_name) values (15, 13, 'download1'), (16, 13, 'download2')");
		List<CaffeineObject> downloads = downloadLookup.executeQuery("select * from downloads where id in (15, 16)");
		Download download15 = (Download) downloads.get(0);
		Download download16 = (Download) downloads.get(1);
		assertArrayEquals("returned downloads should match expected ids", new int[] {download15.id, download16.id}, new int[] {15, 16});
		assertArrayEquals("returned downloads should match expected file names", new String[] {download15.file_file_name, download16.file_file_name}, new String[] {"download1", "download2"});
		assertArrayEquals("returned downloads should match expected org_ids", new int[] {download15.org_id, download16.org_id}, new int[] {13, 13});
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

	public void insertTables() throws Exception {
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
	}

	private void insertUsers() throws Exception {
		databaseHandle.executeUpdate("insert into users (id, first_name, last_name) values (13, 'Nick', 'Case')");
		databaseHandle.executeUpdate("insert into users (id, first_name, last_name) values (5, 'Grawr', 'McPhee')");
	}
}
