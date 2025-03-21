package io.quassar.editor.box.util;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.filters.GroupFilter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatasourceHelper {

	public static final String Owner = "owner";

	public static boolean matches(String value, String[] conditions) {
		if (value == null) return false;
		final String cleanedValue = clean(value.toLowerCase());
		return Arrays.stream(conditions).allMatch(c -> cleanedValue.contains(clean(c)));
	}

	private static String clean(String value) {
		value = Normalizer.normalize(value, Normalizer.Form.NFD);
		value = value.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
		return value;
	}

	public static List<String> categories(String grouping, List<Filter> filters) {
		if (filters == null) return Collections.emptyList();
		Filter filter = filters.stream().filter(f -> f.grouping().equalsIgnoreCase(grouping)).findFirst().orElse(null);
		return filter != null ? new ArrayList<>(((GroupFilter)filter).groups()) : Collections.emptyList();
	}
}
