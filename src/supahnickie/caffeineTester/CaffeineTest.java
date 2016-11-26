package supahnickie.caffeineTester;

import static org.junit.Assert.*;

import org.junit.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import supahnickie.caffeine.*;
import supahnickie.testClasses.*;

public class CaffeineTest {
	CaffeineObject databaseHandle;
	CaffeineObject userLookup;
	CaffeineObject downloadLookup;

	// Raw SQL tests

	@Test
	public void executeUpdate() throws Exception {
		downloadLookup.executeUpdate("insert into downloads (id, org_id, file_file_name) values (15, 1, 'download1'), (16, 1, 'download2')");
		List<CaffeineObject> downloads = downloadLookup.executeQuery("select * from downloads where id in (15, 16)");
		Download download15 = (Download) downloads.get(0);
		Download download16 = (Download) downloads.get(1);
		assertArrayEquals("returned downloads should match expected ids", new int[] {download15.id, download16.id}, new int[] {15, 16});
		assertArrayEquals("returned downloads should match expected file names", new String[] {download15.file_file_name, download16.file_file_name}, new String[] {"download1", "download2"});
		assertArrayEquals("returned downloads should match expected org_ids", new int[] {download15.org_id, download16.org_id}, new int[] {1, 1});
	}

	@Test
	public void executeUpdateWithListArgs() throws Exception {
		Download download = (Download) downloadLookup.find(1);
		assertEquals("file_file_name should match seed before transformation", download.file_file_name, "FileTest num 1");
		assertEquals("org_id should match seed before transformation", download.org_id, 2);
		List<Object> args = new ArrayList<Object>();
		args.add("RenamedFile");
		args.add(3);
		args.add(1);
		downloadLookup.executeUpdate("update downloads set (file_file_name, org_id) = (?, ?) where id = ?", args);
		download = (Download) downloadLookup.find(1);
		assertEquals("file_file_name should be changed to reflect the update", download.file_file_name, "RenamedFile");
		assertEquals("org_id should should be changed to reflect the update", download.org_id, 3);
	}

	@Test
	public void executeQuery() throws Exception {
		List<CaffeineObject> downloads = downloadLookup.executeQuery("select * from downloads");
		assertEquals("size of return array should match seeds", downloads.size(), 4);
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertEquals("download1 file name should match seed", download1.file_file_name, "FileTest num 1");
		assertEquals("download1 org_id should match seed", download1.org_id, 2);
		assertEquals("download2 file name should match seed", download2.file_file_name, "FileTest num 2");
		assertEquals("download2 org_id should match seed", download2.org_id, 1);
	}

	@Test
	public void executeQueryWithListArgs() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add("FileTest num 2");
		args.add(2);
		List<CaffeineObject> downloads = downloadLookup.executeQuery("select * from downloads where file_file_name = ? or org_id = ? order by id asc", args);
		assertEquals("size of return array should match expected return", downloads.size(), 3);
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertEquals("download1 file name should match seed", download1.file_file_name, "FileTest num 1");
		assertEquals("download1 org_id should match seed", download1.org_id, 2);
		assertEquals("download2 file name should match seed", download2.file_file_name, "FileTest num 2");
		assertEquals("download2 org_id should match seed", download2.org_id, 1);
	}

	@Test
	public void executeQueryWithListArgsAndOptions() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add("FileTest num 3");
		args.add(6);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("limit", 1);
		options.put("orderBy", "id asc");
		List<CaffeineObject> downloads = downloadLookup.executeQuery("select * from downloads where file_file_name = ? or org_id = ?", args, options);
		assertEquals("size of return array should match expected return", downloads.size(), 1);
		Download download1 = (Download) downloads.get(0);
		assertEquals("download1 file name should match seed", download1.file_file_name, "FileTest num 3");
		assertEquals("download1 org_id should match seed", download1.org_id, 2);
	}

	@Test
	public void executeARlikeQuery() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("file_file_name", "FileTest num 4");
		args.put("org_id", 3);
		List<CaffeineObject> downloads = downloadLookup.executeQuery(args);
		assertEquals("size of return array should match expected return", downloads.size(), 1);
		Download download1 = (Download) downloads.get(0);
		assertEquals("download1 file name should match seed", download1.file_file_name, "FileTest num 4");
		assertEquals("download1 org_id should match seed", download1.org_id, 3);
	}

	@Test
	public void executeARlikeQueryWithOptions() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("file_file_name", "FileTest num 3");
		args.put("org_id", 2);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("limit", 1);
		options.put("orderBy", "id asc");
		List<CaffeineObject> downloads = downloadLookup.executeQuery(args, options);
		assertEquals("size of return array should match expected return", downloads.size(), 1);
		Download download1 = (Download) downloads.get(0);
		assertEquals("download1 file name should match seed", download1.file_file_name, "FileTest num 3");
		assertEquals("download1 org_id should match seed", download1.org_id, 2);
	}

	@Test
	public void queryWhereNoConditionsMatch() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("file_file_name", "FileTest num 6");
		args.put("org_id", 2);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("limit", 1);
		options.put("orderBy", "id asc");
		List<CaffeineObject> downloads = downloadLookup.executeQuery(args, options);
		assertEquals("size of return array should match expected return", downloads.size(), 0);
	}

	// AR-like methods tests

	@Test
	public void find() throws Exception {
		User user = (User) userLookup.find(2);
		assertEquals("ids should match", user.id, 2);
		assertEquals("first name should match", user.first_name, "Nick");
		assertEquals("last name should match", user.last_name, "Case");
		assertNotEquals("ids should not match others", user.id, 1);
		assertNotEquals("first name should not match others", user.first_name, "Grawr");
		assertNotEquals("last name should not match others", user.last_name, "McPhee");
	}

	// Test helper methods

	@Before
	public void setUp() throws Exception {
		// The database must already exist, but should be blank otherwise.
		new Caffeine(System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_TEST_URL"), System.getenv("CAFFEINE_DB_USERNAME"), System.getenv("CAFFEINE_DB_PASSWORD"));
		databaseHandle = new User();
		userLookup = new User();
		downloadLookup = new Download();
		insertTables();
		insertUsers();
		insertDownloads();
	}

	@After
	public void tearDown() throws Exception {
		databaseHandle.executeUpdate("drop table if exists users");
		databaseHandle.executeUpdate("drop table if exists downloads");
	}

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
		databaseHandle.executeUpdate("insert into users (id, first_name, last_name, encrypted_password, sign_in_count, role) values " +
			"(1, 'Grawr', 'McPhee', 'qwerqwer', 13, 'admin')," +
			"(2, 'Nick', 'Case', 'asdfasdf', 0, 'super')," +
			"(3, 'Test', 'User', 'zxcvzxcv', 3, 'moderator')"
		);
	}

	private void insertDownloads() throws Exception {
		databaseHandle.executeUpdate("insert into downloads (id, file_file_name, org_id, user_id) values " +
				"(1, 'FileTest num 1', 2, 2)," +
				"(2, 'FileTest num 2', 1, 3)," +
				"(3, 'FileTest num 3', 2, 1)," +
				"(4, 'FileTest num 4', 3, 2)"
			);
	}
}
