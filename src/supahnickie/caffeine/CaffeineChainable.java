package supahnickie.caffeine;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaffeineChainable {
	String currentQuery;
	boolean firstCondition;
	List<Object> placeholders = new ArrayList<Object>();

	@SuppressWarnings("unchecked")
	public final List<CaffeineObject> execute() throws Exception {
		String sql = getCurrentQuery();
		PreparedStatement ps = CaffeineConnection.setup().prepareStatement(sql);
		int counter = 1;
		for (int i = 0; i < getPlaceholders().size(); i++) {
			if (getPlaceholders().get(i).getClass().equals(ArrayList.class)) {
				List<Object> arrayArgs = (List<Object>) getPlaceholders().get(i);
				for (int j = 0; j < arrayArgs.size(); j++) {
					ps.setObject(counter, arrayArgs.get(j));
					counter++;
				}
			} else {
				ps.setObject(counter, getPlaceholders().get(i));
				counter++;
			}
		}
		List<CaffeineObject> results = CaffeineSQLRunner.executeQuery(ps);
		resetQueryState();
		return results;
	}

	public final CaffeineChainable join(String typeOfJoin, String fromJoin, String toJoin) throws Exception {
		String[] fromJoins = fromJoin.split("\\.");
		String[] toJoins = toJoin.split("\\.");
		String sql;
		sql = (getCurrentQuery() == null) ? CaffeineObject.baseQuery() : getCurrentQuery();
		typeOfJoin = (typeOfJoin.equals("")) ? "join " : typeOfJoin + " join ";
		sql = sql + " " + typeOfJoin + toJoins[0] + " on " + toJoins[0] + "." + toJoins[1] + " = " + fromJoins[0] + "." + fromJoins[1];
		setCurrentQuery(sql);
		return this;
	}

	public final CaffeineChainable join(String fromJoin, String toJoin) throws Exception {
		return join("", fromJoin, toJoin);
	}

	public final CaffeineChainable where(String condition) throws Exception {
		return appendCondition("and", condition);
	}

	public final CaffeineChainable where(String condition, Object placeholderValue) throws Exception {
		getPlaceholders().add(placeholderValue);
		return where(condition);
	}

	public final CaffeineChainable where(String condition, List<Object> placeholderValues) throws Exception {
		getPlaceholders().add(placeholderValues);
		return where(condition);
	}

	public final CaffeineChainable or(String condition) throws Exception {
		return appendCondition("or", condition);
	}

	public final CaffeineChainable or(String condition, Object placeholderValue) throws Exception {
		getPlaceholders().add(placeholderValue);
		return or(condition);
	}

	public final CaffeineChainable or(String condition, List<Object> placeholderValues) throws Exception {
		getPlaceholders().add(placeholderValues);
		return or(condition);
	}

	private final CaffeineChainable appendCondition(String type, String condition) throws Exception {
		if (getCurrentQuery() == null) {
			setCurrentQuery(CaffeineObject.baseQuery());
		}
		Pattern p = Pattern.compile("where");
		Matcher m = p.matcher(getCurrentQuery());
		if ( !m.find() ) setFirstCondition(true);
		if ( getFirstCondition() ) {
			setCurrentQuery(getCurrentQuery() + " where ");
		} else {
			setCurrentQuery(getCurrentQuery() + type + " ");
		}
		setFirstCondition(false);
		setCurrentQuery(getCurrentQuery() + condition + " ");
		return this;
	}

	private final void resetQueryState() throws Exception {
		setPlaceholders(new ArrayList<Object>());
		setCurrentQuery(null);
		setFirstCondition(true);
	}

	/* Getters */

	final List<Object> getPlaceholders() throws Exception {
		return this.placeholders;
	}

	final String getCurrentQuery() throws Exception {
		return this.currentQuery;
	}

	final boolean getFirstCondition() throws Exception {
		return this.firstCondition;
	}

	/* Setters */

	final void setPlaceholders(List<Object> placeholders) throws Exception {
		this.placeholders = placeholders;
	}

	final void setCurrentQuery(String sql) throws Exception {
		this.currentQuery = sql;
	}

	final void setFirstCondition(Boolean bool) throws Exception {
		this.firstCondition = bool;
	}

}