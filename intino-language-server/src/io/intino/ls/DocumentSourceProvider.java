package io.intino.ls;

import io.intino.tara.builder.SourceProvider;

import javax.xml.transform.Source;
import java.util.stream.Stream;

public class DocumentSourceProvider implements SourceProvider {
	private final DocumentManager documentManager;

	public DocumentSourceProvider(DocumentManager documentManager) {

		this.documentManager = documentManager;
	}

	public

	@Override
	public Stream<Source> all() {
		return Stream.empty();
	}
}
