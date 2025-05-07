package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;
import io.quassar.editor.box.SubjectGenerator;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class DirUtils {

	public static void copyDir(String dir, File destiny) {
		URL resourceUrl = DirUtils.class.getClassLoader().getResource(dir);
		if (resourceUrl == null) return;
		if (resourceUrl.getProtocol().equals("jar")) copyDirFromJarFile(dir, resourceUrl, destiny);
		else copyDirFromResources(dir, resourceUrl, destiny);
	}

	public static void copyDirFromJarFile(String dir, URL resourceUrl, File destiny) {
		String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));
		try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.getName().startsWith(dir)) {
					String relativePath = entry.getName().substring(dir.length()).replaceFirst("^/", "");
					Path entryDestination = destiny.toPath().resolve(relativePath);

					if (entry.isDirectory()) {
						Files.createDirectories(entryDestination);
					} else {
						Files.createDirectories(entryDestination.getParent());
						try (InputStream in = jarFile.getInputStream(entry)) {
							Files.copy(in, entryDestination, StandardCopyOption.REPLACE_EXISTING);
						}
					}
				}
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static void copyDirFromResources(String dir, URL resourceUrl, File destiny) {
		try {
			Path source = Paths.get(SubjectGenerator.class.getResource("/" + dir).toURI());
			try(Stream<Path> paths = Files.walk(source)) {
				paths.forEach(path -> {
					try {
						Path destinoPath = destiny.toPath().resolve(source.relativize(path).toString());
						if (Files.isDirectory(path)) {
							Files.createDirectories(destinoPath);
						} else {
							Files.createDirectories(destinoPath.getParent());
							Files.copy(path, destinoPath, StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
		} catch (URISyntaxException | IOException e) {
			Logger.error(e);
		}
	}

}
