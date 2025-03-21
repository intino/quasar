package io.quassar.editor.box.languages;

import io.quassar.editor.model.Language;

import java.io.File;
import java.io.IOException;

public interface LanguageArtifactory {
	File retrieve(String language) throws IOException;
}
