package io.intino.ls;

import io.intino.tara.Tara;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class IntinoLanguageServer implements LanguageServer, LanguageClientAware {
	private final Tara dsl;

	public IntinoLanguageServer(Tara dsl) {
		this.dsl = dsl;
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setDocumentHighlightProvider(new DocumentHighlightOptions());
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		return completedFuture(new InitializeResult(capabilities));
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return new IntinoDocumentService(dsl);
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return completedFuture(null);
	}

	@Override
	public void exit() {
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		// Retorna tu implementación personalizada de WorkspaceService
		return null;
	}

	@Override
	public void initialized(InitializedParams params) {
		LanguageServer.super.initialized(params);
	}

	@Override
	public void cancelProgress(WorkDoneProgressCancelParams params) {
		LanguageServer.super.cancelProgress(params);
	}

	@Override
	public void connect(LanguageClient client) {

	}
}
