package io.quassar.editor.model;

import io.quassar.editor.box.models.File;

public record TokenLocation(File file, FilePosition position) {
}
