package io.quassar.editor.box.util;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.types.VersionType;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.ModelRelease;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ModelHelper {

	public static final String FirstReleaseVersion = "1.0.0";

	public static boolean isM1Release(Model model, ModelRelease release) {
		if (release == null) return false;
		return isM1Release(model, release.version());
	}

	public static boolean isM1Release(Model model, String release) {
		return !model.isTemplate() && release != null && !release.equals(Model.DraftRelease) && !model.language().artifactId().equals(Language.Metta);
	}

	public static String label(Model model, String language, EditorBox box) {
		String result = model.isTitleQualified() ? model.qualifiedTitle() : model.title();
		if (result == null || result.isEmpty()) result = model.name();
		return model.isTemplate() ? box.translatorService().translate("%s template", language).formatted(result).toUpperCase() : result;
	}

	public static String proposeName() {
		return ModelNameGenerator.generate();
	}

	public static boolean isMetamodel(Model model, EditorBox box) {
		return box.languageManager().getWithMetamodel(model) != null;
	}

	private static final String VersionPatternMask = "%s.%s.%s";
	public static String nextVersion(Model model, VersionType type, EditorBox box) {
		List<String> lastVersion = box.modelManager().releases(model).stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1, o2)).filter(v -> !v.equals(Model.DraftRelease)).toList();
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

	public static boolean isZip(String filename) {
		return filename.endsWith(".zip");
	}

	private static final String ArchetypeFilename = "archetype.zip";
	public static boolean isArchetype(String filename) {
		return filename.equalsIgnoreCase(ArchetypeFilename);
	}

	public static String validWorkspaceFileName(String name) {
		return StringHelper.camelCaseToKebabCase(name).replace("/-", "/").replace(" ", "-");
	}

}
