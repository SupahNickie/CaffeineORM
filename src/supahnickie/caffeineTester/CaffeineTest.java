package supahnickie.caffeineTester;

import static org.junit.Assert.*;

import org.junit.*;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import supahnickie.caffeine.*;
import supahnickie.testClasses.*;

public class CaffeineTest {

	@Test
	public void multipleDatabaseConnections() throws Exception {
		CaffeineConnection.useDatabase("primary");
		CaffeineObject.setQueryClass(User.class);
		List<CaffeineObject> primaryDbUsers = CaffeineConnection.objectQuery("select * from users");
		User user1 = (User) primaryDbUsers.get(0);
		User user2 = (User) primaryDbUsers.get(1);
		User user3 = (User) primaryDbUsers.get(2);
		CaffeineConnection.useDatabase("secondary");
		List<CaffeineObject> secondaryDbUsers = CaffeineConnection.objectQuery("select * from users");
		User user4 = (User) secondaryDbUsers.get(0);
		User user5 = (User) secondaryDbUsers.get(1);
		User user6 = (User) secondaryDbUsers.get(2);
		CaffeineConnection.useDatabase("tertiary");
		List<CaffeineObject> tertiaryDbUsers = CaffeineConnection.objectQuery("select * from users");
		User user7 = (User) tertiaryDbUsers.get(0);
		User user8 = (User) tertiaryDbUsers.get(1);
		User user9 = (User) tertiaryDbUsers.get(2);
		assertArrayEquals("first names from first database should match expected", new String[] { "Grawr", "Nick", "Test" }, new String[] { user1.getFirstName(), user2.getFirstName(), user3.getFirstName() });
		assertArrayEquals("first names from second database should match expected", new String[] { "Saint", "Easter", "Freddy" }, new String[] { user4.getFirstName(), user5.getFirstName(), user6.getFirstName() });
		assertArrayEquals("first names from third database should match expected", new String[] { "Oliver", "Thomas", "Tiger" }, new String[] { user7.getFirstName(), user8.getFirstName(), user9.getFirstName() });
	}

	@Test
	public void rawQueryRawData() throws Exception {
		List<HashMap<String, Object>> rawReturn = CaffeineConnection.rawQuery("select downloads.*, users.* from downloads join users on downloads.user_id = users.id where downloads.id in (1, 2, 3) order by downloads.id asc");
		assertEquals("size of return should match expected", 3, rawReturn.size());
		Map<String, Object> returnAttrs1 = rawReturn.get(0);
		Map<String, Object> returnAttrs2 = rawReturn.get(1);
		Map<String, Object> returnAttrs3 = rawReturn.get(2);
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 1", "Nick"}, new Object[] {returnAttrs1.get("file_file_name"), returnAttrs1.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 2", "Test"}, new Object[] {returnAttrs2.get("file_file_name"), returnAttrs2.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 3", "Grawr"}, new Object[] {returnAttrs3.get("file_file_name"), returnAttrs3.get("first_name")});
	}

	@Test
	public void rawQueryRawDataNamedParameters() throws Exception {
		// List as argument
		List<Integer> idList = new ArrayList<Integer>();
		idList.add(1);
		idList.add(2);
		idList.add(3);
		List<HashMap<String, Object>> rawReturn = CaffeineConnection.rawQuery("select downloads.*, users.* from downloads join users on downloads.user_id = users.id where downloads.id in ($1) order by downloads.id asc", idList);
		assertEquals("size of return should match expected", 3, rawReturn.size());
		Map<String, Object> returnAttrs1 = rawReturn.get(0);
		Map<String, Object> returnAttrs2 = rawReturn.get(1);
		Map<String, Object> returnAttrs3 = rawReturn.get(2);
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 1", "Nick"}, new Object[] {returnAttrs1.get("file_file_name"), returnAttrs1.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 2", "Test"}, new Object[] {returnAttrs2.get("file_file_name"), returnAttrs2.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 3", "Grawr"}, new Object[] {returnAttrs3.get("file_file_name"), returnAttrs3.get("first_name")});

		// Object... as argument
		rawReturn = CaffeineConnection.rawQuery("select downloads.*, users.* from downloads join users on downloads.user_id = users.id where downloads.id in ($1, $2, $3) order by downloads.id asc", 1, 2, 3);
		assertEquals("size of return should match expected", 3, rawReturn.size());
		returnAttrs1 = rawReturn.get(0);
		returnAttrs2 = rawReturn.get(1);
		returnAttrs3 = rawReturn.get(2);
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 1", "Nick"}, new Object[] {returnAttrs1.get("file_file_name"), returnAttrs1.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 2", "Test"}, new Object[] {returnAttrs2.get("file_file_name"), returnAttrs2.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 3", "Grawr"}, new Object[] {returnAttrs3.get("file_file_name"), returnAttrs3.get("first_name")});
	}

	@Test
	public void rawQueryRawDataJDBCParameters() throws Exception {
		// List as argument
		List<Integer> idList = new ArrayList<Integer>();
		idList.add(1);
		idList.add(2);
		idList.add(3);
		List<HashMap<String, Object>> rawReturn = CaffeineConnection.rawQuery("select downloads.*, users.* from downloads join users on downloads.user_id = users.id where downloads.id in (?) order by downloads.id asc", idList);
		assertEquals("size of return should match expected", 3, rawReturn.size());
		Map<String, Object> returnAttrs1 = rawReturn.get(0);
		Map<String, Object> returnAttrs2 = rawReturn.get(1);
		Map<String, Object> returnAttrs3 = rawReturn.get(2);
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 1", "Nick"}, new Object[] {returnAttrs1.get("file_file_name"), returnAttrs1.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 2", "Test"}, new Object[] {returnAttrs2.get("file_file_name"), returnAttrs2.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 3", "Grawr"}, new Object[] {returnAttrs3.get("file_file_name"), returnAttrs3.get("first_name")});

		// Object... as argument
		rawReturn = CaffeineConnection.rawQuery("select downloads.*, users.* from downloads join users on downloads.user_id = users.id where downloads.id in (?, ?, ?) order by downloads.id asc", 1, 2, 3);
		assertEquals("size of return should match expected", 3, rawReturn.size());
		returnAttrs1 = rawReturn.get(0);
		returnAttrs2 = rawReturn.get(1);
		returnAttrs3 = rawReturn.get(2);
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 1", "Nick"}, new Object[] {returnAttrs1.get("file_file_name"), returnAttrs1.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 2", "Test"}, new Object[] {returnAttrs2.get("file_file_name"), returnAttrs2.get("first_name")});
		assertArrayEquals("expected download and user attributes should appear together", new Object[] {"FileTest num 3", "Grawr"}, new Object[] {returnAttrs3.get("file_file_name"), returnAttrs3.get("first_name")});
	}

	@Test
	public void rawUpdateWithNamedParameters() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add(6);
		args.add("McPhee");
		CaffeineConnection.rawUpdate("insert into users (first_name, id, sign_in_count) values ($2, $1, $1)", args);
		User user = (User) CaffeineObject.find(User.class, 6);
		assertEquals("id should match $1 arg", 6, user.getId());
		assertEquals("sign_in_count should match $1 arg", 6, user.getSignInCount());
		assertEquals("first_name should match $2 arg", "McPhee", user.getFirstName());
	}

	@Test
	public void rawUpdateWithUnstructuredNamedParameters() throws Exception {
		CaffeineConnection.rawUpdate("insert into users (first_name, id, sign_in_count) values ($2, $1, $1)", 6, "McPhee");
		User user = (User) CaffeineObject.find(User.class, 6);
		assertEquals("id should match $1 arg", 6, user.getId());
		assertEquals("sign_in_count should match $1 arg", 6, user.getSignInCount());
		assertEquals("first_name should match $2 arg", "McPhee", user.getFirstName());
	}

	@Test
	public void rawUpdate() throws Exception {
		CaffeineConnection.rawUpdate("insert into downloads (id, org_id, file_file_name) values (15, 1, 'download1'), (16, 1, 'download2')");
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where id in (15, 16)");
		Download download15 = (Download) downloads.get(0);
		Download download16 = (Download) downloads.get(1);
		assertArrayEquals("returned downloads should match expected ids", new int[] {15, 16}, new int[] {download15.id, download16.id});
		assertArrayEquals("returned downloads should match expected file names", new String[] {"download1", "download2"}, new String[] {download15.file_file_name, download16.file_file_name});
		assertArrayEquals("returned downloads should match expected org_ids", new int[] {1, 1}, new int[] {download15.org_id, download16.org_id});
	}

	@Test
	public void rawUpdateWithListArgs() throws Exception {
		Download download = (Download) CaffeineObject.find(Download.class, 1);
		assertEquals("file_file_name should match seed before transformation", "FileTest num 1", download.file_file_name);
		assertEquals("org_id should match seed before transformation", 2, download.org_id);
		List<Object> args = new ArrayList<Object>();
		args.add("RenamedFile");
		args.add(3);
		args.add(1);
		CaffeineConnection.rawUpdate("update downloads set (file_file_name, org_id) = (?, ?) where id = ?", args);
		download = (Download) CaffeineObject.find(Download.class, 1);
		assertEquals("file_file_name should be changed to reflect the update", "RenamedFile", download.file_file_name);
		assertEquals("org_id should should be changed to reflect the update", 3, download.org_id);
	}

	@Test(expected = Exception.class)
	public void objectQueryWithoutSettingClass() throws Exception {
		CaffeineObject.setQueryClass(null);
		CaffeineConnection.objectQuery("select * from users");
	}

	@Test
	public void objectQuery() throws Exception {
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads");
		assertEquals("size of return array should match seeds", 4, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertEquals("download1 file name should match seed", "FileTest num 1", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
		assertEquals("download2 file name should match seed", "FileTest num 2", download2.file_file_name);
		assertEquals("download2 org_id should match seed", 1, download2.org_id);
	}

	@Test
	public void rawQueryWithNamedParamsAndListArgs() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add("FileTest num 2");
		args.add(2);
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where file_file_name = $1 or org_id = $2 order by id asc", args);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertEquals("download1 file name should match seed", "FileTest num 1", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
		assertEquals("download2 file name should match seed", "FileTest num 2", download2.file_file_name);
		assertEquals("download2 org_id should match seed", 1, download2.org_id);
	}

	@Test
	public void rawQueryWithNamedParamsAndUnstructuredArgs() throws Exception {
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where file_file_name = $1 or org_id = $2 order by id asc", "FileTest num 2", 2);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertEquals("download1 file name should match seed", "FileTest num 1", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
		assertEquals("download2 file name should match seed", "FileTest num 2", download2.file_file_name);
		assertEquals("download2 org_id should match seed", 1, download2.org_id);
	}

	@Test
	public void rawQueryWithNamedParamsAndInStatement() throws Exception {
		List<Object> args = new ArrayList<Object>();
		List<Integer> orgIdArg = new ArrayList<Integer>();
		List<String> filenameArg = new ArrayList<String>();
		orgIdArg.add(1);
		orgIdArg.add(3);
		filenameArg.add("FileTest num 4");
		filenameArg.add("FileTest num 1");
		filenameArg.add("FileTest num 2");
		args.add(orgIdArg);
		args.add(filenameArg);
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where org_id in ($1) or file_file_name in ($2) order by id asc", args);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		Download download3 = (Download) downloads.get(2);
		assertEquals("download1 file name should match seed", "FileTest num 1", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
		assertEquals("download2 file name should match seed", "FileTest num 2", download2.file_file_name);
		assertEquals("download2 org_id should match seed", 1, download2.org_id);
		assertEquals("download3 file name should match seed", "FileTest num 4", download3.file_file_name);
		assertEquals("download3 org_id should match seed", 3, download3.org_id);
	}

	@Test
	public void rawQueryWithUnstructuredNamedParamsAndInStatement() throws Exception {
		List<Integer> orgIdArg = new ArrayList<Integer>();
		List<String> filenameArg = new ArrayList<String>();
		orgIdArg.add(1);
		orgIdArg.add(3);
		filenameArg.add("FileTest num 4");
		filenameArg.add("FileTest num 1");
		filenameArg.add("FileTest num 2");
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where org_id in ($1) or file_file_name in ($2) or id = $3 order by id asc", orgIdArg, filenameArg, 2);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		Download download3 = (Download) downloads.get(2);
		assertEquals("download1 file name should match seed", "FileTest num 1", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
		assertEquals("download2 file name should match seed", "FileTest num 2", download2.file_file_name);
		assertEquals("download2 org_id should match seed", 1, download2.org_id);
		assertEquals("download3 file name should match seed", "FileTest num 4", download3.file_file_name);
		assertEquals("download3 org_id should match seed", 3, download3.org_id);
	}

	@Test
	public void rawQueryWithListArgs() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add("FileTest num 2");
		args.add(2);
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where file_file_name = ? or org_id = ? order by id asc", args);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertEquals("download1 file name should match seed", "FileTest num 1", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
		assertEquals("download2 file name should match seed", "FileTest num 2", download2.file_file_name);
		assertEquals("download2 org_id should match seed", 1, download2.org_id);
	}

	@Test
	public void rawQueryWithUnstructuredListArgs() throws Exception {
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where file_file_name = ? or org_id = ? order by id asc", "FileTest num 2", 2);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertEquals("download1 file name should match seed", "FileTest num 1", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
		assertEquals("download2 file name should match seed", "FileTest num 2", download2.file_file_name);
		assertEquals("download2 org_id should match seed", 1, download2.org_id);
	}

	@Test
	public void rawQueryWithListArgsAndJDBCInPlaceholder() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add("FileTest num 2");
		List<Integer> arrayArg = new LinkedList<Integer>();
		arrayArg.add(2);
		arrayArg.add(3);
		arrayArg.add(4);
		arrayArg.add(5);
		args.add(arrayArg);
		List<Integer> otherArrayArg = new LinkedList<Integer>();
		otherArrayArg.add(1);
		otherArrayArg.add(6);
		args.add(otherArrayArg);
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where file_file_name = ? or id in (?) or org_id in (?) order by id asc", args);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		Download download3 = (Download) downloads.get(2);
		assertEquals("download1 file name should match seed", "FileTest num 2", download1.file_file_name);
		assertEquals("download1 id should match seed", 2, download1.id);
		assertEquals("download2 file name should match seed", "FileTest num 3", download2.file_file_name);
		assertEquals("download2 id should match seed", 3, download2.id);
		assertEquals("download3 file name should match seed", "FileTest num 4", download3.file_file_name);
		assertEquals("download3 id should match seed", 4, download3.id);
	}

	@Test
	public void rawQueryWithUnstructuredListArgsAndJDBCInPlaceholder() throws Exception {
		List<Integer> arrayArg = new LinkedList<Integer>();
		arrayArg.add(2);
		arrayArg.add(3);
		arrayArg.add(4);
		arrayArg.add(5);
		List<Integer> otherArrayArg = new LinkedList<Integer>();
		otherArrayArg.add(1);
		otherArrayArg.add(6);
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where file_file_name = ? or id in (?) or org_id in (?) order by id asc", "FileTest num 2", arrayArg, otherArrayArg);
		assertEquals("size of return array should match expected return", 3, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		Download download3 = (Download) downloads.get(2);
		assertEquals("download1 file name should match seed", "FileTest num 2", download1.file_file_name);
		assertEquals("download1 id should match seed", 2, download1.id);
		assertEquals("download2 file name should match seed", "FileTest num 3", download2.file_file_name);
		assertEquals("download2 id should match seed", 3, download2.id);
		assertEquals("download3 file name should match seed", "FileTest num 4", download3.file_file_name);
		assertEquals("download3 id should match seed", 4, download3.id);
	}

	@Test
	public void rawQueryWithListArgsAndOptions() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add("FileTest num 3");
		args.add(6);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("limit", 1);
		options.put("orderBy", "id asc");
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.objectQuery("select * from downloads where file_file_name = ? or org_id = ?", args, options);
		assertEquals("size of return array should match expected return", 1, downloads.size());
		Download download1 = (Download) downloads.get(0);
		assertEquals("download1 file name should match seed", "FileTest num 3", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
	}

	@Test
	public void executeARlikeQuery() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("file_file_name", "FileTest num 4");
		args.put("org_id", 3);
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.query(args);
		assertEquals("size of return array should match expected return", 1, downloads.size());
		Download download1 = (Download) downloads.get(0);
		assertEquals("download1 file name should match seed", "FileTest num 4", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 3, download1.org_id);
	}

	@Test
	public void executeARlikeQueryWithOptions() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("file_file_name", "FileTest num 3");
		args.put("org_id", 2);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("limit", 1);
		options.put("orderBy", "id asc");
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.query(args, options);
		assertEquals("size of return array should match expected return", 1, downloads.size());
		Download download1 = (Download) downloads.get(0);
		assertEquals("download1 file name should match seed", "FileTest num 3", download1.file_file_name);
		assertEquals("download1 org_id should match seed", 2, download1.org_id);
	}

	@Test
	public void queryWhereNoConditionsMatch() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("file_file_name", "FileTest num 6");
		args.put("org_id", 2);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("limit", 1);
		options.put("orderBy", "id asc");
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineConnection.query(args, options);
		assertEquals("size of return array should match expected return", 0, downloads.size());
	}

	// AR-like methods tests

	@Test
	public void find() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 2);
		assertEquals("ids should match", 2, user.getId());
		assertEquals("first name should match", "Nick", user.getFirstName());
		assertEquals("last name should match", "Case", user.getLastName());
		assertNotEquals("ids should not match others", 1, user.getId());
		assertNotEquals("first name should not match others", "Grawr", user.getFirstName());
		assertNotEquals("last name should not match others", "McPhee", user.getLastName());
	}

	@Test
	public void findAfterQueryClassSet() throws Exception {
		CaffeineObject.setQueryClass(User.class);
		User user1 = (User) CaffeineObject.find(1);
		assertEquals("ids should match expected", 1, user1.getId());
		assertEquals("first name should match expected", "Grawr", user1.getFirstName());
		assertEquals("last name should match expected", "McPhee", user1.getLastName());
	}

	@Test
	public void findWithNonexistentUser() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 8);
		assertEquals("returned object id should be 0", 0, user.getId());
		assertEquals("returned object first_name should be null", null, user.getFirstName());
		assertEquals("returned object last_name should be null", null, user.getLastName());
	}

	@Test
	public void create() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("first_name", "Superman");
		args.put("last_name", "is not as cool as a flawed hero");
		User newUser = (User) CaffeineObject.create(User.class, args);
		assertEquals("id should match what is next in the DB sequence", 4, newUser.getId());
		assertEquals("first name should match what was put in the args", "Superman", newUser.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", newUser.getLastName());
		User dbUser = (User) CaffeineObject.find(User.class, 4);
		assertEquals("id should match what is next in the DB sequence", 4, dbUser.getId());
		assertEquals("first name should match what was put in the args", "Superman", dbUser.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", dbUser.getLastName());
	}

	@Test
	public void createNotPassingValidations() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("first_name", "illegal first name");
		args.put("last_name", "is not as cool as a flawed hero");
		User newUser = (User) CaffeineObject.create(User.class, args);
		assertEquals("return is null because there was an error", null, newUser);
		User dbUser = (User) CaffeineObject.find(User.class, 4);
		assertEquals("returned object id should be 0", 0, dbUser.getId());
		assertEquals("returned object first_name should be null", null, dbUser.getFirstName());
		assertEquals("returned object last_name should be null", null, dbUser.getLastName());
	}

	@Test
	public void createFromInstance() throws Exception {
		User newUser = new User();
		newUser.setFirstName("Superman");
		newUser.setLastName("is not as cool as a flawed hero");
		newUser.create();
		assertEquals("id should match what is next in the DB sequence", 4, newUser.getId());
		assertEquals("first name should match what was put in the args", "Superman", newUser.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", newUser.getLastName());
		User dbUser = (User) CaffeineObject.find(User.class, 4);
		assertEquals("id should match what is next in the DB sequence", 4, dbUser.getId());
		assertEquals("first name should match what was put in the args", "Superman", dbUser.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", dbUser.getLastName());
	}

	@Test
	public void createFromInstanceNotPassingValidations() throws Exception {
		User newUser = new User();
		newUser.setFirstName("illegal first name");
		newUser.setLastName("is not as cool as a flawed hero");
		newUser.create();
		assertEquals("id should be zero valued because the update didn't pass validations", 0, newUser.getId());
		assertEquals("first name should match what was already assigned to the object", "illegal first name", newUser.getFirstName());
		assertEquals("last name should match what was already assigned to the object", "is not as cool as a flawed hero", newUser.getLastName());
		User dbUser = (User) CaffeineObject.find(User.class, 4);
		assertEquals("returned object id should be 0", 0, dbUser.getId());
		assertEquals("returned object first_name should be null", null, dbUser.getFirstName());
		assertEquals("returned object last_name should be null", null, dbUser.getLastName());
	}

	@Test
	public void update() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("first_name", "Superman");
		args.put("last_name", "is not as cool as a flawed hero");
		User user = (User) CaffeineObject.find(User.class, 2);
		user.update(args);
		assertEquals("id should not have been updated", 2, user.getId());
		assertEquals("first name should match what was put in the args", "Superman", user.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", user.getLastName());
		User dbUser = (User) CaffeineObject.find(User.class, 2);
		assertEquals("id should not have been updated", 2, dbUser.getId());
		assertEquals("first name should match what was put in the args", "Superman", dbUser.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", dbUser.getLastName());
	}

	@Test
	public void updateChangesWithNoArgs() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 2);
		assertEquals("id should be what's in the db", 2, user.getId());
		assertEquals("first name should be what's in the db", "Nick", user.getFirstName());
		assertEquals("last name should be what's in the db", "Case", user.getLastName());
		user.setFirstName("Superman");
		user.setLastName("is not as cool as a flawed hero");
		user.update();
		assertEquals("id should not have been updated", 2, user.getId());
		assertEquals("first name should match what was put in the args", "Superman", user.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", user.getLastName());
		User dbUser = (User) CaffeineObject.find(User.class, 2);
		assertEquals("id should not have been updated", 2, dbUser.getId());
		assertEquals("first name should match what was put in the args", "Superman", dbUser.getFirstName());
		assertEquals("last name should match what was put in the args", "is not as cool as a flawed hero", dbUser.getLastName());
	}

	@Test
	public void updateChangesWithNoArgsFailingValidations() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 2);
		assertEquals("id should be what's in the db", 2, user.getId());
		assertEquals("first name should be what's in the db", "Nick", user.getFirstName());
		assertEquals("last name should be what's in the db", "Case", user.getLastName());
		user.setFirstName("Superman");
		user.setLastName("another illegal name");
		user.update();
		assertEquals("id should not have been updated", 2, user.getId());
		assertEquals("first name should match what was set", "Superman", user.getFirstName());
		assertEquals("last name should match what was put in the args", "another illegal name", user.getLastName());
		User dbUser = (User) CaffeineObject.find(User.class, 2);
		assertEquals("id should not have been updated", 2, dbUser.getId());
		assertEquals("first name should not have been updated in the db", "Nick", dbUser.getFirstName());
		assertEquals("last name should not have been updated in the db", "Case", dbUser.getLastName());
	}

	@Test
	public void updateNotPassingValidations() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("first_name", "Superman");
		args.put("last_name", "another illegal name");
		User user = (User) CaffeineObject.find(User.class, 2);
		user.update(args);
		assertEquals("id should not have been updated", 2, user.getId());
		assertEquals("first name should not have been changed because it failed validation", "Nick", user.getFirstName());
		assertEquals("last name should not have been changed because it failed validation", "Case", user.getLastName());
		User dbUser = (User) CaffeineObject.find(User.class, 2);
		assertEquals("id should not have been updated", 2, dbUser.getId());
		assertEquals("first name should match what was originally in the DB", "Nick", dbUser.getFirstName());
		assertEquals("last name should match what was originally in the DB", "Case", dbUser.getLastName());
	}

	@Test
	public void updateNonExistentUser() throws Exception {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("first_name", "Superman");
		args.put("last_name", "is not as cool as a flawed hero");
		User user = (User) CaffeineObject.find(User.class, 7);
		user.update(args);
		User dbUser = (User) CaffeineObject.find(User.class, 7);
		assertArrayEquals("no user should have been updated or created", new Object[] {0, null, null}, new Object[] {dbUser.getId(), dbUser.getFirstName(), dbUser.getLastName()});
		CaffeineObject.setQueryClass(User.class);
		List<CaffeineObject> users = CaffeineConnection.objectQuery("select * from users");
		assertEquals("size of return should match expected", 3, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		User user3 = (User) users.get(2);
		assertArrayEquals("deleted object should not be in return", new int[] {1, 2, 3}, new int[] {user1.getId(), user2.getId(), user3.getId()});
		assertArrayEquals("deleted object attrs should not be in return objects", new String[] {"Grawr", "Nick", "Test"}, new String[] {user1.getFirstName(), user2.getFirstName(), user3.getFirstName()});
	}

	@Test
	public void delete() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 3);
		boolean result = user.delete();
		assertEquals("return should be whether or not object was deleted", true, result);
		CaffeineObject.setQueryClass(User.class);
		List<CaffeineObject> users = CaffeineConnection.objectQuery("select * from users");
		assertEquals("size of return should match expected", 2, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		assertArrayEquals("deleted object should not be in return", new int[] {1, 2}, new int[] {user1.getId(), user2.getId()});
		assertArrayEquals("deleted object attrs should not be in return objects", new String[] {"Grawr", "Nick"}, new String[] {user1.getFirstName(), user2.getFirstName()});
	}

	@Test
	public void deleteNonExistentUser() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 8);
		boolean result = user.delete();
		assertEquals("return true if sql ran without error", true, result);
		CaffeineObject.setQueryClass(User.class);
		List<CaffeineObject> users = CaffeineConnection.objectQuery("select * from users");
		assertEquals("size of return should match expected", 3, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		User user3 = (User) users.get(2);
		assertArrayEquals("no objects should have been deleted", new int[] {1, 2, 3}, new int[] {user1.getId(), user2.getId(), user3.getId()});
		assertArrayEquals("no object attrs should have been deleted", new String[] {"Grawr", "Nick", "Test"}, new String[] {user1.getFirstName(), user2.getFirstName(), user3.getFirstName()});
	}

	@Test
	public void join() throws Exception {
		CaffeineObject.setQueryClass(Download.class);
		List<CaffeineObject> downloads = CaffeineObject.chainable().join("downloads.user_id", "users.id").where("users.id = ?", 3).execute();
		assertEquals("size of return list should match expected", 1, downloads.size());
		Download download = (Download) downloads.get(0);
		assertEquals("id should match expected", 2, download.id);
		assertEquals("file name should match expected", "FileTest num 2", download.file_file_name);
		assertEquals("user_id should match expected", 3, download.user_id);
	}

	@Test
	public void joinWithChainable() throws Exception {
		List<CaffeineObject> downloads = CaffeineObject.chainable(Download.class).join("downloads.user_id", "users.id").where("users.id = ?", 3).execute();
		assertEquals("size of return list should match expected", 1, downloads.size());
		Download download = (Download) downloads.get(0);
		assertEquals("id should match expected", 2, download.id);
		assertEquals("file name should match expected", "FileTest num 2", download.file_file_name);
		assertEquals("user_id should match expected", 3, download.user_id);
	}

	@Test
	public void joinWithSpecificType() throws Exception {
		List<CaffeineObject> downloads = CaffeineObject.chainable(Download.class).join("inner", "downloads.user_id", "users.id").where("users.id = ?", 3).execute();
		assertEquals("size of return list should match expected", downloads.size(), 1);
		Download download = (Download) downloads.get(0);
		assertEquals("id should match expected", 2, download.id);
		assertEquals("file name should match expected", "FileTest num 2", download.file_file_name);
		assertEquals("user_id should match expected", 3, download.user_id);
	}

	@Test
	public void where() throws Exception {
		CaffeineObject.setQueryClass(User.class);
		List<CaffeineObject> users = CaffeineObject.chainable().where("id in (2, 3)").execute();
		assertEquals("size of return list should match expected", 2, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		assertEquals("id should match expected", 2, user1.getId());
		assertEquals("first name should match expected", "Nick", user1.getFirstName());
		assertEquals("last name should match expected", "Case", user1.getLastName());
		assertEquals("id should match expected", 3, user2.getId());
		assertEquals("first name should match expected", "Test", user2.getFirstName());
		assertEquals("last name should match expected", "User", user2.getLastName());
	}

	@Test
	public void whereWithVariables() throws Exception {
		List<CaffeineObject> users = CaffeineObject.chainable(User.class).where("id = ?", 2).execute();
		assertEquals("size of return list should match expected", 1, users.size());
		User user1 = (User) users.get(0);
		assertEquals("id should match expected", 2, user1.getId());
		assertEquals("first name should match expected", "Nick", user1.getFirstName());
		assertEquals("last name should match expected", "Case", user1.getLastName());
	}

	@Test
	public void whereWithListArgs() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add(2);
		args.add(3);
		List<CaffeineObject> users = CaffeineObject.chainable(User.class).where("id in (?, ?)", args).execute();
		assertEquals("size of return list should match expected", 2, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		assertEquals("id should match expected", 2, user1.getId());
		assertEquals("first name should match expected", "Nick", user1.getFirstName());
		assertEquals("last name should match expected", "Case", user1.getLastName());
		assertEquals("id should match expected", 3, user2.getId());
		assertEquals("first name should match expected", "Test", user2.getFirstName());
		assertEquals("last name should match expected", "User", user2.getLastName());
	}

	@Test
	public void whereWithVarArgs() throws Exception {
		List<CaffeineObject> users = CaffeineObject.chainable(User.class).where("id in (?, ?)", 2, 3).execute();
		assertEquals("size of return list should match expected", 2, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		assertEquals("id should match expected", 2, user1.getId());
		assertEquals("first name should match expected", "Nick", user1.getFirstName());
		assertEquals("last name should match expected", "Case", user1.getLastName());
		assertEquals("id should match expected", 3, user2.getId());
		assertEquals("first name should match expected", "Test", user2.getFirstName());
		assertEquals("last name should match expected", "User", user2.getLastName());
	}

	@Test
	public void or() throws Exception {
		List<CaffeineObject> users = CaffeineObject.chainable(User.class).where("id in (2, 3)").or("first_name = 'Grawr'").execute();
		assertEquals("size of return list should match expected", 3, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		User user3 = (User) users.get(2);
		assertEquals("id should matched expected", 1, user1.getId());
		assertEquals("first name should match expected", "Grawr", user1.getFirstName());
		assertEquals("last name should match expected", "McPhee", user1.getLastName());
		assertEquals("id should match expected", 2, user2.getId());
		assertEquals("first name should match expected", "Nick", user2.getFirstName());
		assertEquals("last name should match expected", "Case", user2.getLastName());
		assertEquals("id should match expected", 3, user3.getId());
		assertEquals("first name should match expected", "Test", user3.getFirstName());
		assertEquals("last name should match expected", "User", user3.getLastName());
	}

	@Test
	public void orWithVariables() throws Exception {
		List<CaffeineObject> users = CaffeineObject.chainable(User.class).where("id = ?", 2).or("id = ?", 3).execute();
		assertEquals("size of return list should match expected", 2, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		assertEquals("id should match expected", 2, user1.getId());
		assertEquals("first name should match expected", "Nick", user1.getFirstName());
		assertEquals("last name should match expected", "Case", user1.getLastName());
		assertEquals("id should match expected", 3, user2.getId());
		assertEquals("first name should match expected", "Test", user2.getFirstName());
		assertEquals("last name should match expected", "User", user2.getLastName());
	}

	@Test
	public void orWithListArgs() throws Exception {
		List<Object> args = new ArrayList<Object>();
		args.add(2);
		args.add(3);
		List<CaffeineObject> users = CaffeineObject.chainable(User.class).where("id = 2").or("id in (?, ?)", args).execute();
		assertEquals("size of return list should match expected", 2, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		assertEquals("id should match expected", 2, user1.getId());
		assertEquals("first name should match expected", "Nick", user1.getFirstName());
		assertEquals("last name should match expected", "Case", user1.getLastName());
		assertEquals("id should match expected", 3, user2.getId());
		assertEquals("first name should match expected", "Test", user2.getFirstName());
		assertEquals("last name should match expected", "User", user2.getLastName());
	}

	@Test
	public void orWithVarArgs() throws Exception {
		List<CaffeineObject> users = CaffeineObject.chainable(User.class).where("id = 2").or("id in (?, ?)", 2, 3).execute();
		assertEquals("size of return list should match expected", 2, users.size());
		User user1 = (User) users.get(0);
		User user2 = (User) users.get(1);
		assertEquals("id should match expected", 2, user1.getId());
		assertEquals("first name should match expected", "Nick", user1.getFirstName());
		assertEquals("last name should match expected", "Case", user1.getLastName());
		assertEquals("id should match expected", 3, user2.getId());
		assertEquals("first name should match expected", "Test", user2.getFirstName());
		assertEquals("last name should match expected", "User", user2.getLastName());
	}

	@Test
	public void getAssociatedHasMany() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 2);
		List<CaffeineObject> downloads = user.getAssociated(Download.class);
		assertEquals("size of return should match expected", 2, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertArrayEquals("ids of associated downloads should match expected", new int[] {1, 4}, new int[] {download1.id, download2.id});
	}

	@Test
	public void getAssociatedHasManyWithForeignKey() throws Exception {
		User user = (User) CaffeineObject.find(User.class, 2);
		List<CaffeineObject> downloads = user.getAssociated(Download.class, "org_id");
		assertEquals("size of return should match expected", 2, downloads.size());
		Download download1 = (Download) downloads.get(0);
		Download download2 = (Download) downloads.get(1);
		assertArrayEquals("ids of associated downloads should match expected", new int[] {1, 3}, new int[] {download1.id, download2.id});
	}

	@Test
	public void getAssociatedBelongsTo() throws Exception {
		Download download = (Download) CaffeineObject.find(Download.class, 3);
		List<CaffeineObject> users = download.getAssociated(User.class);
		assertEquals("return list should only contain the possessing object", 1, users.size());
		User user = (User) users.get(0);
		assertEquals("id should match expected possessing user", 1, user.getId());
	}

	@Test
	public void getAssociatedBelongsToWithForeignKey() throws Exception {
		Download download = (Download) CaffeineObject.find(Download.class, 3);
		List<CaffeineObject> users = download.getAssociated(User.class, "org_id");
		assertEquals("return list should only contain the possessing object", 1, users.size());
		User user = (User) users.get(0);
		assertEquals("id should match expected possessing user", 2, user.getId());
	}

	@Test
	public void createValidations() throws Exception {
		User user = new User();
		user.setFirstName("illegal first name");
		assertEquals("validate on an illegal value should return false", false, user.validate("create"));
		assertEquals("getValiationErrors should report back the reason", "* first_name of illegal first name is not allowed for a User *", user.getValidationErrors());
	}

	@Test
	public void updateValidations() throws Exception {
		User user = new User();
		user.setLastName("another illegal name");
		assertEquals("validate on an illegal value should return false", false, user.validate("update"));
		assertEquals("getValiationErrors should report back the reason", "* last_name of another illegal name is not allowed for a User *", user.getValidationErrors());
	}

	@Test
	public void isDirty() throws Exception {
		User user = new User();
		assertEquals("dirty on a new record should not be true", false, user.isDirty());
		user.setFirstName("Grawr");
		assertEquals("dirty on a changed record should be true", true, user.isDirty());
		user.setFirstName(null);
		assertEquals("dirty on a should be set back to false if the record has the same attrs", false, user.isDirty());
		User user2 = (User) CaffeineObject.find(User.class, 3);
		assertEquals("dirty on a looked-up record should not be true", false, user2.isDirty());
		user2.setFirstName("ohhh yeaaaah, snap into a Slim Jim");
		assertEquals("dirty on an existing record with different attrs than it started should be true", true, user2.isDirty());
		user2.update();
		assertEquals("record should not be considered dirty anymore after updating in the DB", false, user2.isDirty());
		List<CaffeineObject> users = CaffeineConnection.objectQuery("select * from users");
		assertArrayEquals("all returned records should not be dirty", new boolean[] {false, false, false}, new boolean[] {users.get(0).isDirty(), users.get(2).isDirty(), users.get(2).isDirty()});
	}

	@Test
	public void isNewRecord() throws Exception {
		User user = new User();
		assertEquals("newRecord on a new record should be true", true, user.isNewRecord());
		user.setFirstName("Graaw!");
		assertEquals("changing attrs should not affect whether the record was new or not", true, user.isNewRecord());
		user.create();
		assertEquals("once persisted in the DB, the record should not be new anymore", false, user.isNewRecord());
		User user2 = (User) CaffeineObject.find(User.class, 3);
		assertEquals("newRecord on a looked-up record should not be true", false, user2.isNewRecord());
		user2.setFirstName("ohhh yeaaaah, snap into a Slim Jim");
		assertEquals("changing attrs on an existing record should not affect the state of newRecord", false, user2.isNewRecord());
		user2.update();
		assertEquals("existing record should also not change newRecord state after persisting", false, user2.isNewRecord());
		List<CaffeineObject> users = CaffeineConnection.objectQuery("select * from users");
		assertArrayEquals("all returned records should not be flagged as new", new boolean[] {false, false, false}, new boolean[] {users.get(0).isNewRecord(), users.get(2).isNewRecord(), users.get(2).isNewRecord()});
	}

	// Test helper methods

	@Before
	public void setUp() throws Exception {
		// The database must already exist, but should be blank otherwise.
		CaffeineConnection.addDatabaseConnection("primary", System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_TEST_URL"), System.getenv("CAFFEINE_DB_USER"), System.getenv("CAFFEINE_DB_PASSWORD"));
		CaffeineConnection.addDatabaseConnection("secondary", System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_TEST_URL_2"), System.getenv("CAFFEINE_DB_USER"), System.getenv("CAFFEINE_DB_PASSWORD"));
		CaffeineConnection.addDatabaseConnection("tertiary", System.getenv("CAFFEINE_DB_DRIVER"), System.getenv("CAFFEINE_DB_TEST_URL_3"), System.getenv("CAFFEINE_DB_USER"), System.getenv("CAFFEINE_DB_PASSWORD"));
		insertTables();
		insertUsers();
		insertDownloads();
		CaffeineConnection.useDatabase("primary");
	}

	@After
	public void tearDown() throws Exception {
		CaffeineConnection.useDatabase("primary");
		CaffeineConnection.rawUpdate("drop table if exists users");
		CaffeineConnection.rawUpdate("drop table if exists downloads");
	}

	public void insertTables() throws Exception {
		CaffeineConnection.useDatabase("primary");
		CaffeineConnection.rawUpdate("drop table if exists users");
		CaffeineConnection.rawUpdate("drop table if exists downloads");
		CaffeineConnection.rawUpdate("create table if not exists users (" +
			"id serial primary key, " +
			"first_name varchar(255), " +
			"last_name varchar(255), " +
			"encrypted_password varchar(255), " +
			"sign_in_count integer, " +
			"role varchar(255))"
		);
		CaffeineConnection.rawUpdate("create table if not exists downloads (" +
			"id serial primary key, " +
			"file_file_name varchar(255), " +
			"org_id integer, " +
			"user_id integer)"
		);
		CaffeineConnection.useDatabase("secondary");
		CaffeineConnection.rawUpdate("drop table if exists users");
		CaffeineConnection.rawUpdate("create table if not exists users (" +
			"id serial primary key, " +
			"first_name varchar(255), " +
			"last_name varchar(255), " +
			"encrypted_password varchar(255), " +
			"sign_in_count integer, " +
			"role varchar(255))"
		);
		CaffeineConnection.useDatabase("tertiary");
		CaffeineConnection.rawUpdate("drop table if exists users");
		CaffeineConnection.rawUpdate("create table if not exists users (" +
			"id serial primary key, " +
			"first_name varchar(255), " +
			"last_name varchar(255), " +
			"encrypted_password varchar(255), " +
			"sign_in_count integer, " +
			"role varchar(255))"
		);
	}

	private void insertUsers() throws Exception {
		CaffeineConnection.useDatabase("primary");
		CaffeineConnection.rawUpdate("insert into users (first_name, last_name, encrypted_password, sign_in_count, role) values " +
			"('Grawr', 'McPhee', 'qwerqwer', 13, 'admin')," +
			"('Nick', 'Case', 'asdfasdf', 0, 'super')," +
			"('Test', 'User', 'zxcvzxcv', 3, 'moderator')"
		);
		CaffeineConnection.useDatabase("secondary");
		CaffeineConnection.rawUpdate("insert into users (first_name, last_name, encrypted_password, sign_in_count, role) values " +
			"('Saint', 'Nicholas', 'opueritpu', 24, 'admin')," +
			"('Easter', 'Bunny', 'ivobhasd', 4, 'moderator')," +
			"('Freddy', 'Krueger', '0392ujv', 1, 'normal')"
		);
		CaffeineConnection.useDatabase("tertiary");
		CaffeineConnection.rawUpdate("insert into users (first_name, last_name, encrypted_password, sign_in_count, role) values " +
			"('Oliver', 'Cromwell', 'v0fd8h', 99, 'super')," +
			"('Thomas', 'Jefferson', 'as632zz', 357, 'super')," +
			"('Tiger', 'McLion', 'zxcvzxcv', 67, 'moderator')"
		);
	}

	private void insertDownloads() throws Exception {
		CaffeineConnection.useDatabase("primary");
		CaffeineConnection.rawUpdate("insert into downloads (file_file_name, org_id, user_id) values " +
			"('FileTest num 1', 2, 2)," +
			"('FileTest num 2', 1, 3)," +
			"('FileTest num 3', 2, 1)," +
			"('FileTest num 4', 3, 2)"
		);
	}
}
