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
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Stream;

public class ArtifactoryHelper {

	public static final String ReleasesPath = "/artifacts/releases";
	public static final String DependencyTemplate = "<dependency>\n\t<groupId>%s</groupId>\n\t<artifactId>%s</artifactId>\n\t<version>%s</version>\n</dependency>";
	public static final String RepositoryTemplate = "<repository>\n\t<id>Quassar</id>\n\t<url>%s" + ReleasesPath + "</url>\n</repository>";
	public static final String ParsersDirectory = "/parsers/";
	public static final String ParserPrefix = "parser-";

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
			File manifestDestination = archetype.releaseDslManifest(language.key(), release.version());
			Files.copy(manifest.toPath(), manifestDestination.toPath(), StandardCopyOption.REPLACE_EXISTING);
			createDigest(jarFile, archetype.releaseDslJarDigest(language.key(), release.version()));
			createDigest(manifestDestination, archetype.releaseDslManifestDigest(language.key(), release.version()));
			removeDirectory(destination);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public static void cleanDslCache(Language language, LanguageRelease release, Archetype.Languages archetype) {
		try {
			if (release == null) return;
			File file = archetype.releaseDslJarDigest(language.key(), release.version());
			if (file.exists()) file.delete();
			file = archetype.releaseDslManifestDigest(language.key(), release.version());
			if (file.exists()) file.delete();
		}
		catch (Exception e) {
			Logger.error(e);
		}
	}

	public static void prepareParserDependency(Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
		try {
			File file = archetype.releaseParserFile(language.key(), release.version(), name);
			if (!file.exists()) return;
			File tempDir = extractParserToDirectory(language, release, name, archetype, file);
			File jarFile = locateJarFileInParser(release, tempDir);
			if (jarFile == null) return;
			File destination = extractJar(jarFile);
			modifyParserManifestAndCopy(destination, language, release, name, archetype);
			createDigest(archetype.releaseParserJar(language.key(), release.version(), name), archetype.releaseParserJarDigest(language.key(), release.version(), name));
			createDigest(archetype.releaseParserManifest(language.key(), release.version(), name), archetype.releaseParserManifestDigest(language.key(), release.version(), name));
			removeDirectory(tempDir);
		}
		catch (Exception e) {
			Logger.error(e);
		}
	}

	public static void cleanParserDependencyCache(Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
		try {
			if (release == null) return;
			name = name.replace(".zip", "");
			File file = archetype.releaseParserJar(language.key(), release.version(), name);
			if (file.exists()) file.delete();
			file = archetype.releaseParserJarDigest(language.key(), release.version(), name);
			if (file.exists()) file.delete();
			file = archetype.releaseParserManifest(language.key(), release.version(), name);
			if (file.exists()) file.delete();
			file = archetype.releaseParserManifestDigest(language.key(), release.version(), name);
			if (file.exists()) file.delete();
		}
		catch (Exception e) {
			Logger.error(e);
		}
	}

	@Nullable
	private static File locateJarFileInParser(LanguageRelease release, File parserDirectory) {
		File[] files = parserDirectory.listFiles();
		if (files == null) {
			removeDirectory(parserDirectory);
			return null;
		}
		File jarFile = Arrays.stream(files).filter(f -> f.getName().endsWith(release.version() + ".jar")).findFirst().orElse(null);
		if (jarFile == null) {
			removeDirectory(parserDirectory);
			return null;
		}
		return jarFile;
	}

	@NotNull
	private static File extractParserToDirectory(Language language, LanguageRelease release, String name, Archetype.Languages archetype, File file) {
		File tempDir = archetype.releaseParserDir(language.key(), release.version(), name);
		if (tempDir.exists()) removeDirectory(tempDir);
		ZipHelper.extract(file, tempDir);
		return tempDir;
	}

	private static File extractJar(File jarFile) {
		File destination = new File(jarFile.getAbsolutePath().replace(".jar", ""));
		ZipHelper.extract(jarFile, destination);
		return destination;
	}

	private static void modifyParserManifestAndCopy(File jarDir, Language language, LanguageRelease release, String name, Archetype.Languages archetype) {
		try {
			File manifest = locateManifest(jarDir);
			if (manifest == null) return;
			File manifestDestination = archetype.releaseParserManifest(language.key(), release.version(), name);
			Files.writeString(manifestDestination.toPath(), manifestOf(manifest, ArtifactoryHelper.dependencyGroup(language), ArtifactoryHelper.dependencyName(archetype.releaseParserFile(language.key(), release.version(), name)), release.version()));
			Files.writeString(manifest.toPath(), manifestOf(manifest, ArtifactoryHelper.dependencyGroup(language), ArtifactoryHelper.dependencyName(archetype.releaseParserFile(language.key(), release.version(), name)), release.version()));
			ZipHelper.zip(jarDir.toPath(), archetype.releaseParserJar(language.key(), release.version(), name).toPath());
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

	public static String parserNameFor(String artifactId) {
		return artifactId.replace(ParserPrefix, "");
	}

	public static String dependencyGroup(Language language) {
		if (language.isFoundational()) return language.collection();
		return Language.QuassarGroup + (language.collection() != null && !language.collection().isEmpty() ? "." + language.collection() : "") + "." + Formatters.normalizeLanguageName(language.name()).toLowerCase();
	}

	public static String dependencyName(File dependency) {
		String extension = FilenameUtils.getExtension(dependency.getName());
		String prefix = dependency.getAbsolutePath().contains(ParsersDirectory) ? ParserPrefix : "";
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

	private static void createDigest(File source, File destiny) throws Exception {
		if (destiny.exists()) destiny.delete();
		Files.writeString(destiny.toPath(), DigestHelper.sha1Of(source));
	}

}
