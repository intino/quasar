package io.quassar.editor.box.util;

import io.intino.ls.document.DocumentManager;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.net.URI;
import java.util.List;

public class TarUtils {

	public static void decompressTarFile(byte[] tarFile, DocumentManager manager, String path, boolean replace) throws IOException {
		try (TarArchiveInputStream tarInput = new TarArchiveInputStream(new ByteArrayInputStream(tarFile))) {
			TarArchiveEntry entry;
			while ((entry = tarInput.getNextTarEntry()) != null) processEntry(manager, path, entry, tarInput, replace);
		}
	}

	private static void processEntry(DocumentManager manager, String path, TarArchiveEntry entry, TarArchiveInputStream tarInput, boolean replace) throws IOException {
		File entryFile = new File(manager.root().getPath(), path + File.separator + entry.getName());
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

	public static File createTarFile(DocumentManager manager, List<URI> uris, File outputTarFile) throws IOException {
		try (TarArchiveOutputStream tarOut = new TarArchiveOutputStream(new FileOutputStream(outputTarFile))) {
			for (URI uri : uris) tarFile(tarOut, manager.getDocumentText(uri), uri.getPath());
			return outputTarFile;
		}
	}

	private static void tarFile(TarArchiveOutputStream tarOut, InputStream is, String entryName) throws IOException {
		byte[] bytes = is.readAllBytes();
		TarArchiveEntry entry = new TarArchiveEntry(entryName);
		entry.setSize(bytes.length);
		tarOut.putArchiveEntry(entry);
		tarOut.write(bytes);
		tarOut.closeArchiveEntry();
	}

	private static void tarDirectory(TarArchiveOutputStream tarOut, File file, String entryName) throws IOException {
		TarArchiveEntry entry = new TarArchiveEntry(file, entryName + File.separator);
		tarOut.putArchiveEntry(entry);
		tarOut.closeArchiveEntry();
		for (File child : file.listFiles()) {
			String entryName1 = entryName + File.separator + child.getName();

			if (child.isDirectory()) tarDirectory(tarOut, child, entryName1);
			else tarFile(tarOut, null, entryName1); // TODO remove?
		}
	}

}
