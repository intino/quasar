package io.quassar.editor.box.util;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.VersionType;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ModelHelper {

	public static final String FirstReleaseVersion = "1.0.0";

	public static String label(Model model, String language, EditorBox box) {
		return model.title() != null && !model.title().isEmpty() ? model.title() : model.name();
	}

	public static String proposeName() {
		String uuid = UUID.randomUUID().toString();
		return uuid.substring(uuid.lastIndexOf("-")+1) + new Timetag(Instant.now(), Scale.Month).value();
	}

	public static boolean isMetamodel(Model model, EditorBox box) {
		Language language = box.languageManager().get(model.name());
		return language != null && (language.level() == Language.Level.L2 || language.level() == Language.Level.L3);
	}

	private static final String VersionPatternMask = "%s.%s.%s";
	public static String nextVersion(Model model, VersionType type, EditorBox box) {
		List<String> lastVersion = box.modelManager().releases(model).stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1, o2)).toList();
		if (lastVersion.isEmpty()) return FirstReleaseVersion;
		String[] parts = lastVersion.getLast().split("\\.");
		if (type == VersionType.MajorVersion) return String.format(VersionPatternMask, Integer.parseInt(parts[0])+1, 0, 0);
		if (type == VersionType.MinorVersion) return String.format(VersionPatternMask, parts[0], Integer.parseInt(parts[1])+1, 0);
		return String.format(VersionPatternMask, parts[0], parts[1], Integer.parseInt(parts[2])+1);
	}

	private static final Pattern VersionPattern = Pattern.compile("^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$");
	public static boolean validReleaseName(String version, Function<String, String> translator) {
		return version != null && !version.equals(translator.apply(Model.DraftRelease)) && VersionPattern.matcher(version).matches();
	}
}
