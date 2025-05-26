package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.model.GavCoordinates;
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
import java.util.stream.Stream;

public class ArtifactoryHelper {

	public static final String ReleasesPath = "/artifacts/releases";
	public static final String DependencyTemplate = "<dependency>\n\t<groupId>%s</groupId>\n\t<artifactId>%s</artifactId>\n\t<version>%s</version>\n</dependency>";
	public static final String RepositoryTemplate = "<repository>\n\t<id>Quassar</id>\n\t<url>%s" + ReleasesPath + "</url>\n</repository>";
	public static final String ReadersDirectory = "/readers/";
	public static final String ReaderPrefix = "reader-";

	public static void prepareDsl(Language language, LanguageRelease release, Archetype.Languages archetype) {
		try {
			File jarFile = archetype.releaseDslJar(language.key(), release.version());
			if (jarFile == null || !jarFile.exists()) return;
			File destination = extractJar(jarFile);
			File manifest = locateManifest(destination);
			if (manifest == null) {
				removeDirectory(destination);
				return;
			}
			Files.copy(manifest.toPath(), archetype.releaseDslManifest(language.key(), release.version()).toPath());
			removeDirectory(destination);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static void prepareReaderDependency(Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
		File file = archetype.releaseReaderFile(language.key(), release.version(), name);
		if (!file.exists()) return;
		File tempDir = extractReaderToDirectory(language, release, name, archetype, file);
		File jarFile = locateJarFileInReader(release, tempDir);
		if (jarFile == null) return;
		File destination = extractJar(jarFile);
		modifyReaderManifestAndCopy(destination, language, release, name, archetype);
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

	private static File extractJar(File jarFile) {
		File destination = new File(jarFile.getAbsolutePath().replace(".jar", ""));
		ZipHelper.extract(jarFile, destination);
		return destination;
	}

	private static void modifyReaderManifestAndCopy(File jarDir, Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
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

	public static GavCoordinates parse(String path) {
		String[] parts = path.split("/");
		if (parts.length < 4) return null;

		String version = parts[parts.length - 2];
		String artifactId = parts[parts.length - 3];
		StringBuilder groupIdBuilder = new StringBuilder();

		for (int i = 0; i < parts.length - 3; i++) {
			if (i > 0) groupIdBuilder.append(".");
			groupIdBuilder.append(parts[i]);
		}

		return new GavCoordinates(groupIdBuilder.toString(), artifactId, version);
	}

	public static String readerNameFrom(String artifactId) {
		return artifactId.replace(ReaderPrefix, "");
	}

	public static String dependencyGroup(Language language) {
		return (language.group() != null && !language.group().isEmpty() ? language.group() : Language.QuassarGroup) + "." + language.name();
	}

	public static String dependencyName(File dependency) {
		String extension = FilenameUtils.getExtension(dependency.getName());
		String prefix = dependency.getAbsolutePath().contains(ReadersDirectory) ? ReaderPrefix : "";
		return prefix + dependency.getName().replace("." + extension, "");
	}

	private static void removeDirectory(File destination) {
		try {
			if (!destination.exists()) return;
			FileUtils.deleteDirectory(destination);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
