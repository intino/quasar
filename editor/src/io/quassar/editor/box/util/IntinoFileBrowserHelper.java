package io.quassar.editor.box.util;

import io.quassar.editor.box.models.File;
import io.quassar.editor.box.schemas.IntinoFileBrowserItem;

import java.util.*;

public class IntinoFileBrowserHelper {

	public static List<IntinoFileBrowserItem> fileBrowserItems(List<File> files) {
		Map<String, IntinoFileBrowserItem> items = new HashMap<>();
		files.forEach(f -> register(f, items));
		return new ArrayList<>(items.values());
	}

	public static IntinoFileBrowserItem itemOf(File file) {
		IntinoFileBrowserItem.Type type = file.isDirectory() ? IntinoFileBrowserItem.Type.Folder : IntinoFileBrowserItem.Type.File;
		return itemOf(file.uri(), file.parents(), type, file.parents().isEmpty());
	}

	private static void register(File file, Map<String, IntinoFileBrowserItem> items) {
		List<String> parents = file.parents();
		if (!items.containsKey(file.uri())) items.put(file.uri(), itemOf(file).id(items.size()));
		for (int i = 0; i < parents.size(); i++) {
			register(uri(parents, i), i > 0 ? uri(parents, i-1) : null, items);
			if (i == parents.size() - 1) register(file, uri(parents, i), i == 0, items);
		}
	}

	private static void register(String directory, String parent, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(directory))
			items.put(directory, itemOf(directory, parent != null ? List.of(parent) : Collections.emptyList(), io.quassar.editor.box.schemas.IntinoFileBrowserItem.Type.Folder, parent == null).id(items.size()));
		if (parent != null && !items.get(parent).children().contains(directory))
			items.get(parent).children().add(directory);
	}

	private static void register(File file, String parent, boolean isRoot, Map<String, IntinoFileBrowserItem> items) {
		if (!items.containsKey(parent))
			items.put(parent, itemOf(parent, Collections.emptyList(), io.quassar.editor.box.schemas.IntinoFileBrowserItem.Type.Folder, isRoot).id(items.size()));
		if (file != null && !items.get(parent).children().contains(file.uri())) items.get(parent).children().add(file.uri());
	}

	private static String uri(List<String> parents, int i) {
		return String.join("/", parents.subList(0, i+1));
	}

	private static IntinoFileBrowserItem itemOf(String uri, List<String> parents, IntinoFileBrowserItem.Type type, boolean isRoot) {
		return new IntinoFileBrowserItem().name(nameOf(uri)).uri(uri).parents(parents).type(type).isRoot(isRoot);
	}

	private static String nameOf(String uri) {
		return uri.contains("/") ? uri.substring(uri.lastIndexOf("/")+1) : uri;
	}

}
