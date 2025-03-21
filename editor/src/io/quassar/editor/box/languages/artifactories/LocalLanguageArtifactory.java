package io.quassar.editor.box.languages.artifactories;

import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageArtifactory;

import java.io.File;
import java.io.IOException;

public class LocalLanguageArtifactory implements LanguageArtifactory {
	private final Archetype archetype;

	public LocalLanguageArtifactory(Archetype archetype) {
		this.archetype = archetype;
	}

	@Override
	public File retrieve(String language) throws IOException {
		return archetype.languages().dslFile(language);
	}
}
