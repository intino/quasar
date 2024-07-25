package io.intino.ls;

import io.intino.tara.builder.SourceProvider;

import java.io.InputStream;
import java.net.URI;
import java.util.stream.Stream;

public class DocumentSourceProvider implements SourceProvider {
	private final WorkspaceManager documentManager;

	public DocumentSourceProvider(WorkspaceManager documentManager) {
		this.documentManager = documentManager;
	}

	@Override
	public Stream<Source> all() {
		return Stream.empty();
	}

	public Source get(URI uri) {
		return new Source() {
			@Override
			public URI uri() {
				return uri;
			}

			@Override
			public InputStream content() {
				return documentManager.getDocumentText(uri);
			}

			@Override
			public boolean dirty() {
				return false;
			}
		};
	}
}
