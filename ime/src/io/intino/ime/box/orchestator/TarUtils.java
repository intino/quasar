package io.intino.ime.box.orchestator;

import io.intino.ls.document.DocumentManager;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TarUtils {

	public static void decompressTarFile(byte[] tarFile, DocumentManager manager, String path, boolean replace) throws IOException {
		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new ByteArrayInputStream(tarFile))) {
			TarArchiveEntry entry;
			while ((entry = tarInput.getNextTarEntry()) != null) processEntry(manager, path, entry, tarInput, replace);
		}
	}

	private static void processEntry(DocumentManager manager, String path, TarArchiveEntry entry, TarArchiveInputStream tarInput, boolean replace) throws IOException {
		File entryFile = new File(manager.root().getPath(), path + "/" + entry.getName());
		if (entry.isDirectory()) {
			if (!entryFile.exists()) entryFile.mkdirs();
		} else {
			if (entryFile.exists() && !replace) return;
			File parent = entryFile.getParentFile();
			if (!parent.exists()) parent.mkdirs();
			try (OutputStream out = new FileOutputStream(entryFile)) {
				copyStream(tarInput, out);
			}
		}
		// manager.addDocument(path, inputStream);
	}

	private static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		while ((length = in.read(buffer)) > 0) {
			out.write(buffer, 0, length);
		}
	}

	public static File createTarFile(List<URI> uris, File outputTarFile) throws IOException {
		try (TarArchiveOutputStream tarOut = new TarArchiveOutputStream(new FileOutputStream(outputTarFile))) {
			for (URI uri : uris) {
				Path path = Paths.get(uri);
				File file = path.toFile();
				if (file.exists()) {
					addFileToTar(tarOut, file, "");
				}
			}
			return outputTarFile;
		}
	}

	private static void addFileToTar(TarArchiveOutputStream tarOut, File file, String parentDir) throws IOException {
		String entryName = parentDir + file.getName();

		if (file.isDirectory()) tarDirectory(tarOut, file, entryName);
		else tarFile(tarOut, file, entryName);
	}

	private static void tarFile(TarArchiveOutputStream tarOut, File file, String entryName) throws IOException {
		TarArchiveEntry entry = new TarArchiveEntry(file, entryName);
		tarOut.putArchiveEntry(entry);
		try (FileInputStream fileIn = new FileInputStream(file)) {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fileIn.read(buffer)) > 0) {
				tarOut.write(buffer, 0, len);
			}
		}
		tarOut.closeArchiveEntry();
	}

	private static void tarDirectory(TarArchiveOutputStream tarOut, File file, String entryName) throws IOException {
		TarArchiveEntry entry = new TarArchiveEntry(file, entryName + "/");
		tarOut.putArchiveEntry(entry);
		tarOut.closeArchiveEntry();
		for (File child : file.listFiles()) addFileToTar(tarOut, child, entryName + "/");
	}

}
