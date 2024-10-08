package io.intino.builderservice.konos.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TarExtractor {

	public static List<File> extractTar(InputStream tarFile, File destDir) throws IOException {
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
}
