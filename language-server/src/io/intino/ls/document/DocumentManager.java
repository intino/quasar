package io.intino.ls.document;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

public interface DocumentManager {

	URI root();

	List<URI> all();

	List<URI> folders();

	void upsertDocument(URI uri, String dsl, String content);

	InputStream getDocumentText(URI uri);

	void move(URI oldUri, URI newUri);

	void remove(URI uri);

	default void commit() {

	}

	default void push() {

	}
}
