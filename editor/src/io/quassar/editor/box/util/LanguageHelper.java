package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class LanguageHelper {

	public static void generateLogo(String language, File destiny) {
		try {
			BufferedImage image = new LanguageLogoGenerator().put(language.charAt(0)).image();
			ImageIO.write(image, "png", destiny);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static final String TaraDslPackage = "tara.dsl.";
	public static final String MavenDslFile = "%s/repository/tara/dsl/%s/%s/%s-%s.jar";
	public static File mavenDslFile(Model metamodel, String version, EditorBox box) {
		return new File(MavenDslFile.formatted(box.configuration().languageRepository(), mavenDirectory(metamodel.name()), version, Formatters.normalizeLanguageName(metamodel.name()), version));
	}

	public static File mavenDslFile(Language language, String version, EditorBox box) {
		return new File(MavenDslFile.formatted(box.configuration().languageRepository(), mavenDirectory(language.name()), version, Formatters.normalizeLanguageName(language.name()), version));
	}

	public static String title(GavCoordinates release) {
		return release.artifactId().toLowerCase() + " " + release.version();
	}

	public static Model model(Language language, EditorBox box) {
		return box.modelManager().get(language.metamodel());
	}

	public static URL logo(Language language, EditorBox box) {
		try {
			if (language == null) return ModelHelper.class.getResource("/images/language-logo.png");
			File logo = box.languageManager().loadLogo(language);
			return logo.exists() ? logo.toURI().toURL() : ModelHelper.class.getResource("/images/language-logo.png");
		} catch (MalformedURLException e) {
			Logger.error(e);
			return null;
		}
	}

	private static String mavenDirectory(String name) {
		return name.toLowerCase().replace("-", "");
	}

}
