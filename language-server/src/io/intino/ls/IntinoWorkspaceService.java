package io.intino.ls;

import io.intino.tara.Tara;
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

	private final Tara dsl;
	private final WorkspaceManager documentManager;

	public IntinoWorkspaceService(Tara dsl, WorkspaceManager documentManager) {
		this.dsl = dsl;
		this.documentManager = documentManager;
	}

	@Override
	public CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> symbol(WorkspaceSymbolParams params) {
		return completedFuture(forRight(documentManager.all().stream()
				.map(d -> new WorkspaceSymbol(d.getPath(), File, forRight(new WorkspaceSymbolLocation(d.toString())))).toList()));
	}

	@Override
	public void didCreateFiles(CreateFilesParams params) {
		params.getFiles().forEach(f -> {
			documentManager.upsertDocument(URI.create(f.getUri()), "dsl " + dsl.languageName() + "\n\n");
		});
	}

	@Override
	public CompletableFuture<WorkspaceEdit> willDeleteFiles(DeleteFilesParams params) {
		params.getFiles().forEach(f -> documentManager.removeDocument(URI.create(f.getUri())));
		List<DeleteFile> list = params.getFiles().stream().map(f -> new DeleteFile(f.getUri())).toList();
		return completedFuture(new WorkspaceEdit(list.stream()
				.map((DeleteFile d) -> Either.<TextDocumentEdit, ResourceOperation>forRight(d))
				.collect(Collectors.toList())));
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		System.out.println(params);
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		System.out.println(params);
	}
}
