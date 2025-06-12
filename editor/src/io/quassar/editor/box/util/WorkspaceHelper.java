package io.quassar.editor.box.util;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.models.File;
import io.quassar.editor.box.models.Workspace;
import io.quassar.editor.model.Model;
import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.FileCreate;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolLocation;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorkspaceHelper {

	public static URI workspace(Model model, String release, Archetype archetype) {
		try {
			java.io.File workspace = archetype.models().workspace(ArchetypeHelper.relativePath(model), model.id());
			if (release != null && !release.equals(Model.DraftRelease)) workspace = releaseWorkSpace(model, release, archetype);
			return workspace.getAbsoluteFile().getCanonicalFile().toURI();
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public static java.io.File releaseWorkSpace(Model model, String release, Archetype archetype) {
		try {
			java.io.File releaseFile = archetype.models().release(ArchetypeHelper.relativePath(model), model.id(), release);
			java.io.File workspace = archetype.tmp().releaseWorkspace(model.id(), release);
			if (!releaseFile.exists()) return workspace;
			if (workspace.exists()) {
				BasicFileAttributes attrs = Files.readAttributes(workspace.toPath(), BasicFileAttributes.class);
				boolean isOld = Duration.between(LocalDateTime.now(), LocalDateTime.ofInstant(attrs.creationTime().toInstant(), ZoneId.of("UTC"))).abs().toHours() > 24;
				if (!isOld) return workspace;
				FileUtils.deleteDirectory(workspace);
			}
			ZipHelper.extract(releaseFile, workspace);
			return workspace;
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public static boolean isFile(WorkspaceSymbol symbol) {
		return symbol.getKind() == SymbolKind.File || symbol.getKind() == SymbolKind.Package;
	}

	public static List<File> filesOf(List<? extends WorkspaceSymbol> symbolList) {
		return symbolList.stream().map(WorkspaceHelper::fileOf).toList();
	}

	public static File fileOf(WorkspaceSymbol symbol) {
		String uri = uriOf(symbol.getLocation().getRight());
		boolean isDirectory = symbol.getKind() != SymbolKind.File;
		return new File(nameOf(symbol.getName()), uri, isDirectory, WorkspaceHelper.parents(uri));
	}

	public static File fileOf(Path path, Workspace workspace) {
		return fileOf(path.toFile(), workspace);
	}

	public static File fileOf(java.io.File file, Workspace workspace) {
		String uri = relativePath(file, workspace);
		return new File(file.getName(), uri, file.isDirectory(), WorkspaceHelper.parents(uri));
	}

	public static String relativePath(java.io.File file, Workspace workspace) {
		return file.getAbsolutePath().replace(workspace.root().getAbsolutePath() + "/", "");
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

	public static FileCreate fileCreateOf(File file) {
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
