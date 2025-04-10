package io.quassar.editor.box.languages;

import io.quassar.editor.model.Model;

public interface MetamodelProvider {
	Model provide(String id);
}