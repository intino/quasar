package io.intino.ls;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.codeinsight.DiagnosticService;
import io.intino.ls.document.FileDocumentManager;
import io.intino.tara.Language;
import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.eclipse.lsp4j.SymbolKind.File;
import static org.eclipse.lsp4j.jsonrpc.messages.Either.forRight;

public class IntinoWorkspaceService implements WorkspaceService {
	private final Language language;
	private final FileDocumentManager documentManager;
	private final DiagnosticService diagnosticService;
	private Integer version;

	public IntinoWorkspaceService(Language language, FileDocumentManager documentManager, DiagnosticService diagnosticService) {
		this.language = language;
		this.documentManager = documentManager;
		this.diagnosticService = diagnosticService;
		version = 0;
	}

	@Override
	public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(WorkspaceSymbolParams params) {
		List<WorkspaceSymbol> list = documentManager.all().stream()
				.map(u -> new WorkspaceSymbol(u.getPath(), File, forRight(new WorkspaceSymbolLocation(u.toString())))).collect(Collectors.toList());
		list.addAll(documentManager.folders().stream()
				.map(u -> new WorkspaceSymbol(u.getPath(), SymbolKind.Package, forRight(new WorkspaceSymbolLocation(u.toString())))).toList());
		return completedFuture(forRight(list));
	}

	@Override
	public CompletableFuture<WorkspaceEdit> willCreateFiles(CreateFilesParams params) {
		return WorkspaceService.super.willCreateFiles(params);
	}

	@Override
	public void didCreateFiles(CreateFilesParams params) {
		params.getFiles().forEach(f -> documentManager.upsertDocument(URI.create(f.getUri()), language.languageName(), "dsl " + language.languageName() + "\n\n"));
	}

	@Override
	public CompletableFuture<WorkspaceEdit> willDeleteFiles(DeleteFilesParams params) {
		params.getFiles().forEach(f -> documentManager.removeDocument(URI.create(f.getUri())));
		List<DeleteFile> list = params.getFiles().stream().map(f -> new DeleteFile(f.getUri())).toList();
		return completedFuture(new WorkspaceEdit(list.stream()
				.map(Either::<TextDocumentEdit, ResourceOperation>forRight)
				.collect(Collectors.toList())));
	}

	@Override
	public void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
		try {
			params.getEvent().getAdded().forEach(folder -> new File(documentManager.root(), folder.getUri()).mkdirs());
			for (WorkspaceFolder folder : params.getEvent().getRemoved()) {
				FileUtils.deleteDirectory(new File(documentManager.root(), folder.getUri()));
				removeContainedDocuments(folder.getUri());
			}
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private void removeContainedDocuments(String uri) {
		documentManager.all().stream().filter(u -> u.getPath().startsWith(uri))
				.forEach(u -> documentManager.removeDocument(URI.create(u.getPath())));
	}

	public InputStream content(URI uri) {
		return documentManager.getDocumentText(uri);
	}

	@Override
	public void didDeleteFiles(DeleteFilesParams params) {
		params.getFiles().forEach(f -> documentManager.removeDocument(URI.create(f.getUri())));
	}

	@Override
	public void didRenameFiles(RenameFilesParams params) {
		params.getFiles().forEach(f -> documentManager.move(URI.create(f.getOldUri()), URI.create(f.getNewUri())));
	}

	@Override
	public CompletableFuture<WorkspaceDiagnosticReport> diagnostic(WorkspaceDiagnosticParams params) {
		Map<String, List<Diagnostic>> result = diagnosticService.analyzeWorkspace().stream().collect(Collectors.groupingBy(Diagnostic::getSource));
		List<WorkspaceDocumentDiagnosticReport> reports = result.keySet().stream().map(k -> new WorkspaceDocumentDiagnosticReport(new WorkspaceFullDocumentDiagnosticReport(result.get(k), k, version = version + 1))).toList();
		return completedFuture(new WorkspaceDiagnosticReport(reports));
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		System.out.println(params);
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		System.out.println(params);
	}

	@Override
	public CompletableFuture<WorkspaceEdit> willRenameFiles(RenameFilesParams params) {
		return WorkspaceService.super.willRenameFiles(params);
	}

}
