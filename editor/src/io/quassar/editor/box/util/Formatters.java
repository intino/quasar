package io.quassar.editor.box.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.function.Function;

public class Formatters {

	private static final String DayFormat = "yyyy/MM/dd";

	public static String firstLowerCase(String value) {
		return value != null ? value.substring(0, 1).toLowerCase() + value.substring(1) : null;
	}

	public static String firstUpperCase(String value) {
		return value != null ? value.substring(0, 1).toUpperCase() + value.substring(1) : null;
	}

	public static String normalizeLanguageName(String value) {
		return Formatters.firstLowerCase(StringHelper.snakeCaseToCamelCase(StringHelper.kebabCaseToCamelCase(value)));
	}

	public static String date(Instant date, String language, Function<String, String> translator) {
		return date(date, DayFormat, language, translator);
	}

	public static String date(Instant date, String format, String language, Function<String, String> translator) {
		if (date == null) return null;
		return formatDate(translator.apply(format), date, locale(language));
	}

	private static String formatDate(String pattern, Instant instant, Locale locale) {
		SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
		return format.format(Date.from(instant));
	}

	private static Locale locale(String language) {
		if (language.toLowerCase().contains("es")) return Locale.of("es", "ES");
		if (language.toLowerCase().contains("pt")) return Locale.of("pt", "PT");
		return Locale.of("en", "EN");
	}

}
