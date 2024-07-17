package io.intino.languageeditor.box.util;

import io.intino.languageeditor.box.I18n;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

public class Formatters {
	private static final String DownloadDateFormat = "dd_MM_YYYY_HH_mm_ss";

	public static String downloadDate(Instant date, String language) {
		SimpleDateFormat formatter = new SimpleDateFormat(DownloadDateFormat, Locale.of(language));
		return formatter.format(new Date(date.toEpochMilli()));
	}

	public static String countMessage(long count, String singleLabel, String pluralLabel, String language) {
		if (count == 0) return !pluralLabel.isEmpty() ? I18n.translate("No " + pluralLabel, language) : "0";
		if (count == 1) return "1" + (!singleLabel.isEmpty() ? " " + I18n.translate(singleLabel, language) : "");
		return formattedNumber(count, language) + (!pluralLabel.isEmpty() ? " " + I18n.translate(pluralLabel, language) : "");
	}

	public static String formattedNumber(long value, String language) {
		return formattedNumber(value, Locale.forLanguageTag(language));
	}

	public static String formattedNumber(long value, Locale locale) {
		return NumberFormat.getNumberInstance(locale).format(value);
	}

}
