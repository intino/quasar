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
import java.util.UUID;

public class LanguageHelper {

	public void generateLogo(String language, File destiny) {
		try {
			BufferedImage image = new LanguageLogoGenerator().put(language.charAt(0)).image();
			ImageIO.write(image, "png", destiny);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static final String MavenDslFile = "%s/repository/tara/dsl/%s/%s/%s-%s.jar";
	public static File mavenDslFile(Language language, String version, EditorBox box) {
		return new File(MavenDslFile.formatted(box.configuration().languageRepository(), mavenDirectory(language), version, Formatters.normalizeLanguageName(language.name()), version));
	}

	public static String title(GavCoordinates release) {
		return release.artifactId() + " " + release.version();
	}

	public static Model model(Language language, EditorBox box) {
		return box.modelManager().get(language.metamodel());
	}

	public static URL logo(Language language, EditorBox box) {
		try {
			File logo = box.languageManager().loadLogo(language);
			return logo.exists() ? logo.toURI().toURL() : ModelHelper.class.getResource("/images/language-logo.png");
		} catch (MalformedURLException e) {
			Logger.error(e);
			return null;
		}
	}

	private static String mavenDirectory(Language language) {
		return language.name().toLowerCase().replace("-", "");
	}

}
