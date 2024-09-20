package io.intino.ime.box.util;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.*;

import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ModelHelper {

	public static final String FirstReleaseVersion = "1.0.0";

	public static String proposeName() {
		String uuid = UUID.randomUUID().toString();
		return uuid.substring(uuid.lastIndexOf("-")+1) + new Timetag(Instant.now(), Scale.Month).value();
	}

	public static ModelLevel level(Model model, ImeBox box) {
		Release release = box.languageManager().release(model);
		if (release == null) return ModelLevel.M1;
		LanguageLevel level = release.level();
		if (level == LanguageLevel.L3) return ModelLevel.M3;
		else if (level == LanguageLevel.L2) return ModelLevel.M2;
		return ModelLevel.M1;
	}

	private static final String VersionPattern = "%s.%s.%s";
	public static String nextReleaseVersion(Model model, VersionType type, ImeBox box) {
		List<Release> lastRelease = box.languageManager().releases(model).stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o1.version(), o2.version())).toList();
		if (lastRelease.isEmpty()) return FirstReleaseVersion;
		String[] parts = lastRelease.getLast().version().split("\\.");
		if (type == VersionType.MajorVersion) return String.format(VersionPattern, Integer.parseInt(parts[0])+1, parts[1], parts[2]);
		if (type == VersionType.MinorVersion) return String.format(VersionPattern, parts[0], Integer.parseInt(parts[1])+1, parts[2]);
		return String.format(VersionPattern, parts[0], parts[1], Integer.parseInt(parts[2])+1);
	}

	public static List<Operation> operations(Model model, ImeBox box) {
		Language language = box.languageManager().get(model.modelingLanguage());
		return language != null ? language.operations() : Collections.emptyList();
	}

	public static boolean canOpenModel(Model model, String username) {
		if (username == null) return model.isPublic();
		return model.isPublic() || model.owner().equals(username);
	}

	public static boolean canOpenModel(Language language, Release release, User user) {
		if (user == null) return false;
		if (!language.owner().equals(user.username())) return false;
		if (release == null) return true;
		return release.model() != null && !release.model().isEmpty();
	}

	public static boolean canAddModel(Release release) {
		return release != null && release.level() == LanguageLevel.L1;
	}

	public static boolean canRemove(Model model, ImeBox box) {
		if (model.isTemporal()) return false;
		Release release = box.languageManager().release(model);
		return release != null && release.level() == LanguageLevel.L1;
	}

	public static Language language(Model model, ImeBox box) {
		return box.languageManager().get(model.modelingLanguage());
	}

	public static URL logo(Model model, ImeBox box) {
		Language language = box.languageManager().get(model.modelingLanguage());
		return LanguageHelper.logo(language, box);
	}

	public static boolean hasRelease(Model model, Release release, ImeBox box) {
		return box.languageManager().releases(model).stream().anyMatch(r -> r.id().equals(release.id()));
	}

	public static boolean canClone(Model model, ImeBox box) {
		Release release = box.languageManager().release(model);
		return release != null && release.level() == LanguageLevel.L1;
	}

	public static boolean canCreateRelease(Model model, String user, ImeBox box) {
		if (user == null) return false;
		if (!model.owner().equals(user)) return false;
		Release release = box.languageManager().release(model);
		return release != null && release.level() != LanguageLevel.L1;
	}

	public static Model m3Model(Model model, ImeBox box) {
		if (level(model, box) == ModelLevel.M3) return null;
		return metamodel(model, ModelLevel.M3, box);
	}

	public static Model m2Model(Model model, ImeBox box) {
		if (level(model, box) == ModelLevel.M3) return null;
		if (level(model, box) == ModelLevel.M2) return null;
		return metamodel(model, ModelLevel.M2, box);
	}

	public static Model metamodel(Model model, ModelLevel metamodelLevel, ImeBox box) {
		Model result = metamodel(model, box);
		ModelLevel level = level(result, box);
		while (level != metamodelLevel) {
			result = metamodel(result, box);
			if (result == null) return null;
			level = level(result, box);
		}
		return result;
	}

	private static Model metamodel(Model model, ImeBox box) {
		Language language = box.languageManager().get(model.modelingLanguage());
		return box.modelManager().modelWith(language);
	}
}
