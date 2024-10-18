package io.intino.ls.document;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static org.apache.commons.io.FilenameUtils.getExtension;

public interface DocumentManager {

	URI root();

	List<URI> all();

	List<URI> folders();

	void upsertDocument(URI uri, String language, String content);

	default void upsertDocument(URI uri, String content) {
		upsertDocument(uri, getExtension(new File(uri.getPath()).getName()), content);
	}

	InputStream getDocumentText(URI uri);

	void move(URI oldUri, URI newUri);

	void remove(URI uri);

	default void commit(String user) {

	}

	default void push() {

	}
}
