package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

	public static void zip(Path folder, String manifest, Path destiny) throws Exception {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destiny.toFile()));
		appendFolder(folder, zos);
		append("manifest.json", manifest, zos);
		zos.close();
	}

	public static void zip(Path folder, Path destiny) throws Exception {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destiny.toFile()));
		appendFolder(folder, zos);
		zos.close();
	}

	private static void appendFolder(Path folder, ZipOutputStream stream) throws Exception {
		Files.walkFileTree(folder, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				append(file, folder.relativize(file), stream);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static void append(Path file, Path relativePath, ZipOutputStream stream) {
		try {
			stream.putNextEntry(new ZipEntry(relativePath.toString()));
			Files.copy(file, stream);
			stream.closeEntry();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private static void append(String name, String content, ZipOutputStream stream) {
		try {
			stream.putNextEntry(new ZipEntry(name));
			stream.write(content.getBytes(StandardCharsets.UTF_8));
			stream.closeEntry();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public static void extract(File zipFile, File destiny) {
		try {
			extract(new FileInputStream(zipFile), destiny);
		} catch (FileNotFoundException e) {
			Logger.error(e);
		}
	}

	public static void extract(InputStream zipStream, File destiny) {
		if (!destiny.exists()) destiny.mkdirs();

		try (ZipInputStream zipInputStream = new ZipInputStream(zipStream)) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				File destFile = new File(destiny, entry.getName());
				if (entry.isDirectory()) destFile.mkdirs();
				else {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) parentDir.mkdirs();
					try (FileOutputStream fos = new FileOutputStream(destFile)) {
						byte[] buffer = new byte[1024];
						int bytesRead;
						while ((bytesRead = zipInputStream.read(buffer)) != -1) {
							fos.write(buffer, 0, bytesRead);
						}
					}
				}
				zipInputStream.closeEntry();
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
