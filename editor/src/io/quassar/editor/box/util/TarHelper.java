package io.quassar.editor.box.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.*;

public class TarHelper {

	public static void extract(InputStream stream, File destination) throws IOException {
		if (!destination.exists()) {
			destination.mkdirs();
		}

		try (TarArchiveInputStream tarIn = new TarArchiveInputStream(stream)) {
			TarArchiveEntry entry;

			while ((entry = tarIn.getNextTarEntry()) != null) {
				File outputFile = new File(destination, entry.getName());

				if (entry.isDirectory()) {
					outputFile.mkdirs();
				} else {
					outputFile.getParentFile().mkdirs();

					try (FileOutputStream out = new FileOutputStream(outputFile)) {
						byte[] buffer = new byte[4096];
						int len;
						while ((len = tarIn.read(buffer)) != -1) {
							out.write(buffer, 0, len);
						}
					}
				}
			}
		}
	}

}
