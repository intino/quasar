package io.quassar.editor.box.util;

import io.quassar.archetype.Archetype;
import io.quassar.editor.model.Language;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ArtifactoryHelper {

	private static final String DependencyTemplate = "<dependency>\n\t<groupId>%s</groupId>\n\t<artifactId>%s</artifactId>\n\t<version>%s</version>\n</dependency>";
	private static final String ReadersDirectory = "/readers/";
	private static final String ReaderSuffix = "-reader";

	public static void buildReaderDependency(Language language, String version, Archetype archetype) {
	}

	public static String dependency(Language language, String version, File dependency) {
		return DependencyTemplate.formatted(dependencyGroup(language), dependencyName(dependency), version);
	}

	public static String groupId(String path) {
		String[] pathInfo = path.split("/");
		if (pathInfo.length < 4) return null;
		return Arrays.stream(pathInfo, 0, pathInfo.length - 4).collect(Collectors.joining("."));
	}

	public static String artifactId(String path) {
		String[] pathInfo = path.split("/");
		if (pathInfo.length < 4) return null;
		return pathInfo[pathInfo.length-4];
	}

	public static String version(String path) {
		String[] pathInfo = path.split("/");
		if (pathInfo.length < 4) return null;
		return pathInfo[pathInfo.length-2];
	}

	public static String file(String path) {
		String[] pathInfo = path.split("/");
		if (pathInfo.length < 4) return null;
		return pathInfo[pathInfo.length-3].replace(ReaderSuffix, "");
	}

	public static String dependencyGroup(Language language) {
		return (language.group() != null && !language.group().isEmpty() ? language.group() : Language.QuassarGroup) + "." + language.name();
	}

	public static String dependencyName(File dependency) {
		String extension = FilenameUtils.getExtension(dependency.getName());
		String suffix = dependency.getAbsolutePath().contains(ReadersDirectory) ? ReaderSuffix : "";
		return dependency.getName().replace("." + extension, "") + suffix;
	}

}
