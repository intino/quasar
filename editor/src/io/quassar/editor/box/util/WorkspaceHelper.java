package io.quassar.editor.box.util;

import io.quassar.editor.box.models.ModelContainer;
import org.eclipse.lsp4j.FileCreate;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorkspaceHelper {

	public static boolean isFile(WorkspaceSymbol symbol) {
		return symbol.getKind() == SymbolKind.File || symbol.getKind() == SymbolKind.Package;
	}

	public static List<ModelContainer.File> filesOf(List<? extends WorkspaceSymbol> symbolList) {
		return symbolList.stream().map(WorkspaceHelper::fileOf).toList();
	}

	public static ModelContainer.File fileOf(WorkspaceSymbol symbol) {
		String uri = uriOf(symbol.getLocation().getRight());
		boolean isDirectory = symbol.getKind() != SymbolKind.File;
		return new ModelContainer.File(nameOf(symbol.getName()), uri, isDirectory, WorkspaceHelper.parents(uri));
	}

	public static String nameOf(String relativePath) {
		if (!relativePath.contains("/")) return relativePath;
		String[] split = relativePath.split("/");
		return split[split.length-1];
	}

	public static String uriOf(WorkspaceSymbolLocation location) {
		if (location == null) return null;
		return location.getUri();
	}

	public static FileCreate fileCreateOf(ModelContainer.File file) {
		FileCreate result = new FileCreate();
		result.setUri(file.uri());
		return result;
	}

	public static List<String> parents(String uri) {
		if (!uri.contains("/")) return Collections.emptyList();
		List<String> parents = Arrays.stream(uri.split("/")).toList();
		return parents.subList(0, parents.size()-1);
	}

	public static String parent(String uri) {
		if (!uri.contains("/")) return "";
		return uri.substring(0, uri.lastIndexOf("/"));
	}
}
