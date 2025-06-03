package io.quassar.editor.box.models;

import java.io.InputStream;
import java.util.List;

public interface FileReader {
	List<File> files();
	boolean exists(String name, File parent);
	boolean exists(io.quassar.editor.box.models.File file);
	File get(String uri);
	InputStream content(String uri);
}
