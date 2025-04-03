package io.quassar.editor.box.models;

import io.intino.alexandria.logger.Logger;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class File {
	private final String name;
	private final String uri;
	private final boolean isDirectory;
	private final List<String> parents;

	private static Map<String, String> LanguagesMap = null;

	public enum Type { Model, Resource }

	public File(String name, String uri, boolean isDirectory, List<String> parents) {
		this.name = name;
		this.uri = uri;
		this.isDirectory = isDirectory;
		this.parents = parents;
		loadLanguages();
	}

	public String name() {
		return name;
	}

	public String extension() {
		return extensionOf(name);
	}

	public static final String ResourcesDirectory = "resources";
	public Type type() {
		return isResource(uri) ? Type.Resource : Type.Model;
	}

	public boolean isResource() {
		return isResource(uri);
	}

	public static boolean isResource(String file) {
		return file.startsWith(ResourcesDirectory);
	}

	public static String withResourcesPath(String name) {
		return ResourcesDirectory + (name.startsWith(java.io.File.separator) ? "" : java.io.File.separator) + name;
	}

	public List<String> parents() {
		return parents;
	}

	public String uri() {
		return uri;
	}

	private static final String DefaultLanguage = "tara";

	public String language() {
		return LanguagesMap.getOrDefault(extensionOf(name), DefaultLanguage);
	}

	private static final String DefaultExtension = "tara";

	private String extensionOf(String name) {
		if (!name.contains(".")) return DefaultExtension;
		return name.substring(name.lastIndexOf(".") + 1);
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public File clone(String newUri) {
		return new File(name, newUri, isDirectory, parents());
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
