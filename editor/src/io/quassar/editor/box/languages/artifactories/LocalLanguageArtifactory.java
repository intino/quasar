package io.quassar.editor.box.languages.artifactories;

import io.quassar.archetype.Archetype;
import io.quassar.editor.box.languages.LanguageArtifactory;
import io.quassar.editor.box.languages.MetamodelProvider;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.StringHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;

import java.io.File;

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
		String groupId = gav.groupId().equals(Language.QuassarCollection) ? Language.TaraGroup : Language.QuassarGroup + "." + gav.groupId();
		return groupId + "." + Formatters.firstUpperCase(StringHelper.kebabCaseToCamelCase(gav.artifactId()));
	}
}
