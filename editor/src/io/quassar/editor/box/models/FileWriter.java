package io.quassar.editor.box.models;

import java.io.InputStream;

public interface FileWriter {
	File create(String filename, InputStream content, File parent);
	File createFolder(String name, File parent);
	void save(File file, InputStream content);
	File rename(File file, String newName);
	File move(File file, File directory);
	File copy(String filename, File source);
	void remove(File file);
}
