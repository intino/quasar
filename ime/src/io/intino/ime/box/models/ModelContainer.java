package io.intino.ime.box.models;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelContainer {
	private List<File> fileList;
	private static Map<String, String> LanguagesMap = null;

	public ModelContainer() {
		fileList = new ArrayList<>();
		loadLanguages();
	}

	public List<File> files() {
		return fileList;
	}

	public File file(String name) {
		return fileList.stream().filter(f -> f.name().equals(name)).findFirst().orElse(null);
	}

	public void add(List<File> fileList) {
		this.fileList.addAll(fileList);
	}

	public void add(File file) {
		fileList.add(file);
	}

	public void remove(List<File> fileList) {
		this.fileList.removeAll(fileList);
	}

	public void remove(File file) {
		fileList.remove(file);
	}

	public static class File {
		private final String name;
		private final List<String> parents;
		private final java.io.File content;

		public File(String name, List<String> parents, java.io.File content) {
			this.name = name;
			this.parents = parents;
			this.content = content;
		}

		public String name() {
			return name;
		}

		public String extension() {
			return extensionOf(name);
		}

		public List<String> parents() {
			return parents;
		}

		public java.io.File content() {
			return content;
		}

		private static final String DefaultLanguage = "tara";
		public String language() {
			return LanguagesMap.getOrDefault(extensionOf(name), DefaultLanguage);
		}

		private static final String DefaultExtension = "tara";
		private String extensionOf(String name) {
			if (!name.contains(".")) return DefaultExtension;
			return name.substring(name.lastIndexOf(".")+1);
		}

	}

	private void loadLanguages() {
		try {
			if (LanguagesMap != null) return;
			InputStream stream = ModelContainer.class.getResourceAsStream("/programming-languages.tsv");
			if (stream == null) return;
			LanguagesMap = IOUtils.readLines(stream, StandardCharsets.UTF_8).stream().collect(Collectors.toMap(l -> l.split("\t")[0], l -> l.split("\t")[1]));
		}
		catch (Throwable e) {
			Logger.error(e);
		}
	}

}
