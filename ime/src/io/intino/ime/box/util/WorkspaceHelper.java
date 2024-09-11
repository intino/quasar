package io.intino.ime.box.util;

import io.intino.ime.box.models.ModelContainer;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WorkspaceHelper {

	public static boolean isFile(WorkspaceSymbol symbol) {
		return symbol.getKind() == SymbolKind.File || symbol.getKind() == SymbolKind.Package;
	}

	public static List<ModelContainer.File> filesOf(List<? extends WorkspaceSymbol> symbolList) {
		return symbolList.stream().map(WorkspaceHelper::fileOf).toList();
	}

	public static ModelContainer.File fileOf(WorkspaceSymbol symbol) {
		return new ModelContainer.File(symbol.getName(), uriOf(symbol.getLocation().getRight()), Collections.emptyList());
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

	public static String parent(String uri) {
		if (!uri.contains("/")) return "";
		return uri.substring(0, uri.lastIndexOf("/"));
	}
}
