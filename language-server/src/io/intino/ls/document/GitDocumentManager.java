package io.intino.ls.document;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.lsp4j.TextDocumentItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GitDocumentManager implements DocumentManager {
	private final File root;
	private final Object lock = new Object();
	private final Map<URI, TextDocumentItem> documents;
	private final CredentialsProvider credentialsProvider;

	public GitDocumentManager(File root, URL gitUrl, CredentialsProvider credentialsProvider) throws IOException {
		this.root = root;
		this.documents = loadDocuments(root);
		this.credentialsProvider = credentialsProvider;
	}


	public File root() {
		return root;
	}

	private TextDocumentItem documentItem(File f) {
		return new TextDocumentItem(relativePath(f).getPath(), dslOf(f), (int) f.lastModified(), content(f));
	}

	public List<URI> all() {
		return documents.keySet().stream().toList();
	}

	public List<URI> folders() {
		return FileUtils.listFilesAndDirs(root, fileFilter(), fileFilter()).stream().filter(f -> !f.getPath().equals(root.getPath())).map(this::relativePath).toList();
	}

	public void upsertDocument(URI uri, String dsl, String content) {
		synchronized (lock) {
			try {
				content = content.isEmpty() ? "dsl " + dsl + "\n\n" : content;
				File file = fileOf(uri);
				documents.put(uri, new TextDocumentItem(uri.toString(), dsl, (int) Instant.now().toEpochMilli(), content));
				file.getParentFile().mkdirs();
				Files.writeString(file.toPath(), content);
			} catch (IOException e) {
				Logger.error(e);
			}
		}
	}

	public InputStream getDocumentText(URI uri) {
		synchronized (lock) {
			TextDocumentItem document = documents.get(uri);
			return document != null ? new ByteArrayInputStream(document.getText().getBytes()) : null;
		}
	}

	public void move(URI oldUri, URI newUri) {
		TextDocumentItem textDocumentItem = documents.get(oldUri);
		if (textDocumentItem != null) try {
			removeDocument(oldUri);
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

	@Override
	public void commit() {
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

	private String dslOf(File f) {
		try {
			if (!f.exists()) return "";
			return Files.lines(f.toPath()).filter(l -> !l.trim().isEmpty()).findFirst().orElse("");
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	private Map<URI, TextDocumentItem> loadDocuments(File projectRoot) throws IOException {
		return Files.walk(projectRoot.toPath())
				.filter(p -> p.toFile().isFile() && p.toFile().getName().endsWith(".tara"))
				.map(Path::toFile)
				.collect(Collectors.toMap(this::relativePath, this::documentItem, (a, b) -> a, ConcurrentHashMap::new));
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