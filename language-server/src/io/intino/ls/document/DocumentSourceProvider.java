package io.intino.ls.document;


import io.intino.ls.SourceProvider;
import io.intino.tara.Source;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.stream.Stream;

public class DocumentSourceProvider implements SourceProvider {
	private final DocumentManager documentManager;

	public DocumentSourceProvider(DocumentManager documentManager) {
		this.documentManager = documentManager;
	}

	@Override
	public Stream<Source> all() {
		return documentManager.all().stream().map(this::get);
	}

	public Source get(URI uri) {
		return new Source() {
			@Override
			public URI uri() {
				return uri;
			}

			@Override
			public Charset charset() {
				return Charset.defaultCharset();
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
