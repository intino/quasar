package io.intino.ls.document;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.lsp4j.TextDocumentItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileDocumentManager implements DocumentManager {
	private final Object lock = new Object();
	private final Map<URI, TextDocumentItem> documents;
	private final File root;

	public FileDocumentManager(File root) throws IOException {
		this.root = root;
		this.documents = loadDocuments(root);
	}

	public URI root() {
		return root.toURI();
	}

	@Override
	public List<URI> all() {
		return documents.keySet().stream().toList();
	}

	@Override
	public List<URI> folders() {
		return FileUtils.listFilesAndDirs(root, fileFilter(), fileFilter()).stream().filter(f -> !f.getPath().equals(root.getPath())).map(this::relativePath).toList();
	}

	@Override
	public void upsertDocument(URI uri, String language, String content) {
		synchronized (lock) {
			try {
				File file = fileOf(uri);
				documents.put(uri, new TextDocumentItem(uri.toString(), language, (int) Instant.now().toEpochMilli(), content));
				file.getParentFile().mkdirs();
				Files.writeString(file.toPath(), content);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}

	@Override
	public InputStream getDocumentText(URI uri) {
		synchronized (lock) {
			TextDocumentItem document = documents.get(uri);
			return document != null ? new ByteArrayInputStream(document.getText().getBytes()) : null;
		}
	}

	@Override
	public void move(URI oldUri, URI newUri) {
		TextDocumentItem textDocumentItem = documents.get(oldUri);
		if (textDocumentItem != null) try {
			remove(oldUri);
			documents.put(newUri, textDocumentItem);
			Files.move(fileOf(oldUri).toPath(), fileOf(newUri).toPath());
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@Override
	public void remove(URI uri) {
		documents.remove(uri);
		File file = fileOf(uri);
		if (file.isFile()) file.delete();
		else removeDirectory(file);
	}

	private static void removeDirectory(File file) {
		try {
			FileUtils.deleteDirectory(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String content(File f) {
		try {
			return Files.readString(f.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	private File fileOf(URI uri) {
		return new File(root, uri.getPath());
	}

	private Map<URI, TextDocumentItem> loadDocuments(File projectRoot) {
		return FileUtils.listFiles(projectRoot, null, true).stream()
				.filter(File::isFile)
				.filter(f -> !f.getName().startsWith("."))
				.collect(Collectors.toMap(this::relativePath, this::documentItem, (a, b) -> a, ConcurrentHashMap::new));
	}

	private TextDocumentItem documentItem(File f) {
		return new TextDocumentItem(relativePath(f).getPath(), languageOf(f), (int) f.lastModified(), content(f));
	}

	private String languageOf(File f) {
		try {
			if (!f.exists()) return "";
			if (f.getName().endsWith(".tara"))
				return Files.lines(f.toPath()).filter(l -> !l.trim().isEmpty()).findFirst().orElse("");
			return extensionOf(f);
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	private String extensionOf(File f) {
		String fileName = f.getName();
		int lastIndex = fileName.lastIndexOf('.');
		return lastIndex > 0 && lastIndex < fileName.length() - 1 ? fileName.substring(lastIndex + 1) : "";
	}

	private IOFileFilter fileFilter() {
		return new IOFileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() && !file.getPath().equals(root.getPath());
			}

			@Override
			public boolean accept(File file, String s) {
				return true;
			}
		};
	}

	private URI relativePath(File f) {
		return URI.create(root.toPath().relativize(f.toPath()).toFile().getPath());
	}
}