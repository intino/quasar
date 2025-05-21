package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtifactoryHelper {

	private static final String DependencyTemplate = "<dependency>\n\t<groupId>%s</groupId>\n\t<artifactId>%s</artifactId>\n\t<version>%s</version>\n</dependency>";
	private static final String RepositoryTemplate = "<repository>\n\t<id>Quassar</id>\n\t<url>%s/releases</url>\n</repository>";
	private static final String ReadersDirectory = "/readers/";
	private static final String ReaderSuffix = "-reader";

	public static void prepareReaderDependency(Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
		File file = archetype.releaseReaderFile(language.key(), release.version(), name);
		if (!file.exists()) return;
		File tempDir = extractReaderToDirectory(language, release, name, archetype, file);
		File jarFile = locateJarFileInReader(release, tempDir);
		if (jarFile == null) return;
		File destination = extractJar(jarFile, language, release, name, archetype);
		modifyManifestAndCopy(destination, language, release, name, archetype);
		removeDirectory(tempDir);
	}

	@Nullable
	private static File locateJarFileInReader(LanguageRelease release, File readerDirectory) {
		File[] files = readerDirectory.listFiles();
		if (files == null) {
			removeDirectory(readerDirectory);
			return null;
		}
		File jarFile = Arrays.stream(files).filter(f -> f.getName().endsWith(release.version() + ".jar")).findFirst().orElse(null);
		if (jarFile == null) {
			removeDirectory(readerDirectory);
			return null;
		}
		return jarFile;
	}

	@NotNull
	private static File extractReaderToDirectory(Language language, LanguageRelease release, String name, Archetype.Languages archetype, File file) {
		File tempDir = archetype.releaseReaderDir(language.key(), release.version(), name);
		if (tempDir.exists()) removeDirectory(tempDir);
		ZipHelper.extract(file, tempDir);
		return tempDir;
	}

	private static File extractJar(File jarFile, Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
		File destination = new File(jarFile.getAbsolutePath().replace(".jar", ""));
		ZipHelper.extract(jarFile, destination);
		return destination;
	}

	private static void modifyManifestAndCopy(File jarDir, Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
		try {
			File manifest = locateManifest(jarDir);
			if (manifest == null) return;
			File manifestDestination = archetype.releaseReaderManifest(language.key(), release.version(), name);
			Files.writeString(manifestDestination.toPath(), manifestOf(manifest, ArtifactoryHelper.dependencyGroup(language), ArtifactoryHelper.dependencyName(archetype.releaseReaderFile(language.key(), release.version(), name)), release.version()));
			Files.writeString(manifest.toPath(), manifestOf(manifest, ArtifactoryHelper.dependencyGroup(language), ArtifactoryHelper.dependencyName(archetype.releaseReaderFile(language.key(), release.version(), name)), release.version()));
			ZipHelper.zip(jarDir.toPath(), archetype.releaseReaderJar(language.key(), release.version(), name).toPath());
		}
		catch (Exception e) {
			Logger.error(e);
		}
	}

	private static File locateManifest(File jarDir) {
		try (Stream<Path> paths = Files.walk(jarDir.toPath())) {
			return paths.map(Path::toFile).filter(p -> p.getName().equals("pom.xml")).findFirst().orElse(null);
		} catch (Exception e) {
			Logger.error(e);
			return null;
		}
	}

	private static String manifestOf(File file, String groupId, String artifactId, String version) throws IOException {
		if (file == null) return null;
		String content = Files.readString(file.toPath());
		content = content.replaceFirst("<groupId>.*?</groupId>", "<groupId>" + groupId + "</groupId>");
		content = content.replaceFirst("<artifactId>.*?</artifactId>", "<artifactId>" + artifactId + "</artifactId>");
		content = content.replaceFirst("<version>.*?</version>", "<version>" + version + "</version>");
		return content;
	}

	public static String dependency(Language language, String version, File dependency) {
		return DependencyTemplate.formatted(dependencyGroup(language), dependencyName(dependency), version);
	}

	public static String repository(String url) {
		return RepositoryTemplate.formatted(url);
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

	private static void removeDirectory(File destination) {
		try {
			FileUtils.deleteDirectory(destination);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
