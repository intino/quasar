package io.quassar.editor.box.languages.artifactories;

import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageArtifactory;
import io.quassar.editor.box.languages.LanguageManager;
import io.quassar.editor.box.languages.MetamodelProvider;
import io.quassar.editor.box.util.ArchetypeHelper;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.StringHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.io.File;
import java.io.IOException;

public class LocalLanguageArtifactory implements LanguageArtifactory {
	private final Archetype archetype;
	private final MetamodelProvider metamodelProvider;

	public LocalLanguageArtifactory(Archetype archetype, MetamodelProvider provider) {
		this.archetype = archetype;
		this.metamodelProvider = provider;
	}

	@Override
	public File retrieve(GavCoordinates gav) {
		return archetype.languages().releaseDslJar(gav.languageId(), gav.version());
	}

	public String mainClass(GavCoordinates gav) {
		return gav.groupId() + "." + Formatters.firstUpperCase(StringHelper.kebabCaseToCamelCase(gav.artifactId()));
	}
}
