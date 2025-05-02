package io.intino.builderservice.konos.utils;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tar {

	public static List<File> unTar(InputStream tarFile, File destDir) throws IOException {
		List<File> files = new ArrayList<>();
		try (TarArchiveInputStream tarIn = new TarArchiveInputStream(tarFile)) {
			TarArchiveEntry entry;
			while ((entry = tarIn.getNextTarEntry()) != null) {
				File outputFile = new File(destDir, entry.getName());
				if (entry.isDirectory()) outputFile.mkdirs();
				else if (!new File(entry.getName()).getName().startsWith(".")) {
					outputFile.getParentFile().mkdirs();
					try (OutputStream out = new FileOutputStream(outputFile)) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = tarIn.read(buffer)) != -1) {
							out.write(buffer, 0, len);
						}
					}
					files.add(outputFile.getCanonicalFile());
				}
			}
		}
		return files;
	}

	public static void tar(File sourceDir, FileFilter filter, File tarFile) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(tarFile);
			 TarArchiveOutputStream tarOut = new TarArchiveOutputStream(fos)) {
			for (File f : Objects.requireNonNull(sourceDir.listFiles())) addFileToTar(tarOut, f, "", filter);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private static void addFileToTar(TarArchiveOutputStream tarOut, File file, String base, FileFilter filter) throws IOException {
		if (!filter.accept(file)) return;
		String entryName = base + file.getName();
		TarArchiveEntry tarEntry = new TarArchiveEntry(file, entryName);
		tarOut.putArchiveEntry(tarEntry);
		if (file.isFile()) {
			try (FileInputStream fis = new FileInputStream(file)) {
				IOUtils.copy(fis, tarOut);
			}
			tarOut.closeArchiveEntry();
		} else if (file.isDirectory()) {
			tarOut.closeArchiveEntry();
			File[] children = file.listFiles(filter);
			if (children != null) for (File child : children) addFileToTar(tarOut, child, entryName + "/", filter);
		}
	}
}
