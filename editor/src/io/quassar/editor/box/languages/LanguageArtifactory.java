package io.quassar.editor.box.languages;

import io.quassar.editor.model.GavCoordinates;

import java.io.File;
import java.io.IOException;

public interface LanguageArtifactory {
	File retrieve(GavCoordinates gav) throws IOException;
	String mainClass(GavCoordinates gav);
}
