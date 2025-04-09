package io.quassar.editor.model;

import java.util.List;

public record LanguageRelease(String version, String template, List<String> examples) {
}
