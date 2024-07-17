package io.intino.ls;

import io.intino.alexandria.logger.Logger;
import org.eclipse.lsp4j.TextDocumentItem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DocumentManager {
	private final ConcurrentHashMap<URI, TextDocumentItem> documents;
	private final File projectRoot;

	public DocumentManager(File projectRoot) throws IOException {
		this.projectRoot = projectRoot;
		this.documents = new ConcurrentHashMap<>();
		documents.putAll(collectDocuments(projectRoot));
	}

	private static Map<URI, TextDocumentItem> collectDocuments(File projectRoot) throws IOException {
		return Files.walk(projectRoot.toPath())
				.filter(p -> p.toFile().isFile() && p.toFile().getName().endsWith(".tara"))
				.map(Path::toFile)
				.collect(Collectors.toMap(File::toURI, DocumentManager::documentItem));
	}

	private static TextDocumentItem documentItem(File f) {
		return new TextDocumentItem(f.toURI().toString(), dslOf(f), (int) f.lastModified(), content(f));
	}

	public void upsertDocument(URI uri, String content) {
		try {
			File file = new File(projectRoot, uri.getPath());
			documents.put(uri, new TextDocumentItem(uri.toString(), dslOf(file), (int) Instant.now().toEpochMilli(), content));
			file.getParentFile().mkdirs();
			Files.writeString(file.toPath(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public InputStream getDocumentText(URI uri) {
		TextDocumentItem document = documents.get(uri);
		return document != null ? new ByteArrayInputStream(document.getText().getBytes()) : null;
	}

	public void removeDocument(URI uri) {
		documents.remove(uri);
	}

	private static String content(File f) {
		try {
			return Files.readString(f.toPath());
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}

	private static String dslOf(File f) {
		try {
			return Files.lines(f.toPath()).filter(l -> !l.trim().isEmpty()).findFirst().get();
		} catch (IOException e) {
			Logger.error(e);
			return "";
		}
	}
}