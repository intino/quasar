package io.intino.ls;

import org.eclipse.lsp4j.TextDocumentItem;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class DocumentManager {
	private final ConcurrentHashMap<String, TextDocumentItem> documents;

	public DocumentManager(File projectRoot) {
		this.documents = new ConcurrentHashMap<>();
	}

	public void upsertDocument(TextDocumentItem document) {
		documents.put(document.getUri(), document);
	}

	public String getDocumentText(String uri) {
		TextDocumentItem document = documents.get(uri);
		return document != null ? document.getText() : null;
	}

	public void removeDocument(String uri) {
		documents.remove(uri);
	}
}