package io.quassar.editor.box.languages.artifactories;

import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageArtifactory;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.model.GavCoordinates;

import java.io.File;
import java.io.IOException;

public class LocalLanguageArtifactory implements LanguageArtifactory {
	private final Archetype archetype;

	public LocalLanguageArtifactory(Archetype archetype) {
		this.archetype = archetype;
	}

	@Override
	public File retrieve(GavCoordinates gav) throws IOException {
		return archetype.languages().releaseDsl(ArchetypeHelper.languageDirectoryName(gav.languageId()), gav.version());
	}
}
