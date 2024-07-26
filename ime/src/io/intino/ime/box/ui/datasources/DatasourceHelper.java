package io.intino.ime.box.ui.datasources;

import java.text.Normalizer;
import java.util.Arrays;

public class DatasourceHelper {

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

}
