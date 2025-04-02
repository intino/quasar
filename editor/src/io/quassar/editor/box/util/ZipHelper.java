package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipHelper {

	public static void zipFolder(Path sourceFolderPath, Path zipPath) throws Exception {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()));
		Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
				Files.copy(file, zos);
				zos.closeEntry();
				return FileVisitResult.CONTINUE;
			}
		});
		zos.close();
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
					// If it's a file, unzip it
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
