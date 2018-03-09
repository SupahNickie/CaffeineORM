package supahnickie.caffeine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CaffeineParamReplacer {

	private CaffeineParamReplacer() {}

	static final PreparedStatement replaceNamedParameters(Connection c, String sql, List<Object> values) throws Exception {
		PreparedStatement ps = null;
		Pattern p = Pattern.compile("\\$\\d*");
		Matcher m = p.matcher(sql);
		List<String> allMatches = findAllNamedParameterPlaceholders(m);
		Queue<Object> replacementVals = new LinkedList<Object>();
		sql = insertAdditionalNamedParameterPlaceholders(sql, allMatches, values, replacementVals);
		ps = c.prepareStatement(sql);
		insertValuesIntoNamedParametersQuery(ps, replacementVals);
		return ps;
	}

	static final PreparedStatement replaceJDBCParameters(Connection c, String sql, List<Object> values) throws Exception {
		sql = insertAdditionalJDBCPlaceholders(sql, values);
		PreparedStatement ps = c.prepareStatement(sql);
		insertValuesIntoJDBCQuery(ps, values);
		return ps;
	}

	static final PreparedStatement replaceExactNamedParameters(Connection c, String sql, Map<String, Object> values) throws Exception {
		PreparedStatement ps = null;
		String patternString = "";
		for (String key : values.keySet()) {
			patternString = patternString + key + "|";
		}
		patternString = patternString.substring(0, patternString.length() - 1);
		Pattern p = Pattern.compile(patternString);
		Matcher m = p.matcher(sql);
		List<String> allMatches = findAllNamedParameterPlaceholders(m);
		Queue<Object> replacementVals = new LinkedList<Object>();
		sql = insertAdditionalNamedExactParameterPlaceholders(sql, allMatches, values, replacementVals);
		ps = c.prepareStatement(sql);
		insertValuesIntoNamedParametersQuery(ps, replacementVals);
		return ps;
	}

	private static final List<String> findAllNamedParameterPlaceholders(Matcher m) {
		List<String> allMatches = new ArrayList<String>();
		while (m.find()) { allMatches.add(m.group()); }
		return allMatches;
	}

	@SuppressWarnings("unchecked")
	private static final String insertAdditionalNamedParameterPlaceholders(String sql, List<String> allMatches, List<Object> values, Queue<Object> replacementVals) {
		for (String placeholder : allMatches) {
			int indexToGrab = new Integer(placeholder.split("\\$")[1]) - 1;
			Object value = values.get(indexToGrab);
			if ( isList(value) ) {
				List<Object> vals = (List<Object>) value;
				String arrayPlaceholder = "";
				for (int i = 0; i < vals.size() - 1; i++) { arrayPlaceholder = arrayPlaceholder.concat("?, "); }
				arrayPlaceholder = arrayPlaceholder.concat("?");
				sql = sql.replaceAll("\\$" + (indexToGrab + 1), arrayPlaceholder);
				for (int j = 0; j < vals.size(); j++) { replacementVals.add(vals.get(j)); }
			} else {
				replacementVals.add(value);
			}
		}
		sql = sql.replaceAll("\\$\\d*", "?");
		return sql;
	}

	@SuppressWarnings("unchecked")
	private static final String insertAdditionalNamedExactParameterPlaceholders(String sql, List<String> allMatches, Map<String, Object> values, Queue<Object> replacementVals) {
		for (String placeholder : allMatches) {
			Object value = values.get(placeholder);
			if ( isList(value) ) {
				List<Object> vals = (List<Object>) value;
				String arrayPlaceholder = "";
				for (int i = 0; i < vals.size() - 1; i++) { arrayPlaceholder = arrayPlaceholder.concat("?, "); }
				arrayPlaceholder = arrayPlaceholder.concat("?");
				sql = sql.replaceFirst(placeholder, arrayPlaceholder);
				for (int j = 0; j < vals.size(); j++) { replacementVals.add(vals.get(j)); }
			} else {
				replacementVals.add(value);
				sql = sql.replaceFirst(placeholder, "?");
			}
		}
		return sql;
	}

	private static final String insertAdditionalJDBCPlaceholders(String sql, List<Object> values) {
		Pattern p = Pattern.compile("\\?");
		Matcher m = p.matcher(sql);
		int index = 0;
		while (m.find()) {
			Object value = values.get(index);
			if ( isList(value) ) { sql = replaceArrayPlaceholderWithActualCountPlaceholders(sql, value); }
			index++;
		}
		return sql;
	}

	private static final void insertValuesIntoNamedParametersQuery(PreparedStatement ps, Queue<Object> values) throws Exception {
		int counter = 1;
		while (!values.isEmpty()) {
			ps.setObject(counter, values.poll());
			counter++;
		}
	}

	@SuppressWarnings("unchecked")
	private static final void insertValuesIntoJDBCQuery(PreparedStatement ps, List<Object> values) throws Exception {
		int counter = 1;
		for (Object value : values) {
			if ( isList(value) ) {
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
	}

	@SuppressWarnings("unchecked")
	private static final String replaceArrayPlaceholderWithActualCountPlaceholders(String sql, Object value) {
		List<Object> vals = (List<Object>) value;
		String arrayPlaceholder = "";
		for (int i = 0; i < vals.size() - 1; i++) {
			arrayPlaceholder = arrayPlaceholder.concat("?, ");
		}
		arrayPlaceholder = arrayPlaceholder.concat("?");
		sql = sql.replaceFirst("\\(\\?\\)", "( " + arrayPlaceholder + " )");
		return sql;
	}

	private static final boolean isList(Object val) {
		return val.getClass().equals(ArrayList.class) || val.getClass().equals(LinkedList.class);
	}
}