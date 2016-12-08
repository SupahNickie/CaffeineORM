package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CaffeineParamReplacer {
	@SuppressWarnings("unchecked")
	static final PreparedStatement replaceNamedParameters(Connection c, String sql, List<Object> values) throws Exception {
		int counter = 1;
		PreparedStatement ps = null;
		Pattern p = Pattern.compile("\\$\\d*");
		Matcher m = p.matcher(sql);
		List<String> allMatches = new ArrayList<String>();
		Queue<Object> replacementVals = new LinkedList<Object>();
		while (m.find()) { allMatches.add(m.group()); }
		for (String placeholder : allMatches) {
			int indexToGrab = new Integer(placeholder.split("\\$")[1]) - 1;
			Object val = values.get(indexToGrab);
			if (val.getClass().equals(ArrayList.class) || val.getClass().equals(LinkedList.class)) {
				List<Object> vals = (List<Object>) val;
				String arrayPlaceholder = "";
				for (int i = 0; i < vals.size() - 1; i++) { arrayPlaceholder = arrayPlaceholder.concat("?, "); }
				arrayPlaceholder = arrayPlaceholder.concat("?");
				sql = sql.replaceAll("\\$" + (indexToGrab + 1), arrayPlaceholder);
				for (int j = 0; j < vals.size(); j++) { replacementVals.add(vals.get(j)); }
			} else {
				replacementVals.add(val);
			}
		}
		sql = sql.replaceAll("\\$\\d*", "?");
		ps = c.prepareStatement(sql);
		while (!replacementVals.isEmpty()) {
			ps.setObject(counter, replacementVals.poll());
			counter++;
		}
		return ps;
	}

	@SuppressWarnings("unchecked")
	static final PreparedStatement replaceJDBCParameters(Connection c, String sql, List<Object> values) throws Exception {
		int counter = 1;
		sql = injectAdditionalPlaceholders(sql, values);
		PreparedStatement ps = c.prepareStatement(sql);
		for (Object value : values) {
			if (value.getClass().equals(ArrayList.class) || value.getClass().equals(LinkedList.class)) {
				List<Object> vals = (List<Object>) value;
				for (int j = 0; j < vals.size(); j++) {
					ps.setObject(counter, vals.get(j));
					counter++;
				}
			} else {
				ps.setObject(counter, value);
				counter++;
			}
		}
		return ps;
	}

	@SuppressWarnings("unchecked")
	private static final String injectAdditionalPlaceholders(String sql, List<Object> values) {
		Pattern p = Pattern.compile("\\?");
		Matcher m = p.matcher(sql);
		int index = 0;
		while (m.find()) {
			Object newVal = values.get(index);
			if (newVal.getClass().equals(ArrayList.class) || newVal.getClass().equals(LinkedList.class)) {
				List<Object> vals = (List<Object>) newVal;
				String arrayPlaceholder = "";
				for (int i = 0; i < vals.size() - 1; i++) {
					arrayPlaceholder = arrayPlaceholder.concat("?, ");
				}
				arrayPlaceholder = arrayPlaceholder.concat("?");
				sql = sql.replaceFirst("\\(\\?\\)", "( " + arrayPlaceholder + " )");
				index++;
			} else {
				index++;
			}
		}
		return sql;
	}
}