package io.intino.ls;

import io.intino.tara.Language;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.eclipse.lsp4j.SymbolKind.File;
import static org.eclipse.lsp4j.jsonrpc.messages.Either.forRight;

class IntinoWorkspaceService implements WorkspaceService {
	private final Language language;
	private final WorkspaceManager documentManager;

	public IntinoWorkspaceService(Language language, WorkspaceManager documentManager) {
		this.language = language;
		this.documentManager = documentManager;
	}

	@Override
	public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(WorkspaceSymbolParams params) {
		return completedFuture(forRight(documentManager.all().stream()
				.map(u -> new WorkspaceSymbol(u.getPath(), File, forRight(new WorkspaceSymbolLocation(u.toString())))).toList()));
	}

	@Override
	public CompletableFuture<WorkspaceEdit> willCreateFiles(CreateFilesParams params) {
		return WorkspaceService.super.willCreateFiles(params);
	}

	@Override
	public void didCreateFiles(CreateFilesParams params) {
		params.getFiles().forEach(f -> {
			documentManager.upsertDocument(URI.create(f.getUri()), language.languageName(), "dsl " + language.languageName() + "\n\n");
		});
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
	public void didDeleteFiles(DeleteFilesParams params) {
		WorkspaceService.super.didDeleteFiles(params);
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

	@Override
	public void didRenameFiles(RenameFilesParams params) {
		WorkspaceService.super.didRenameFiles(params);
	}
}
