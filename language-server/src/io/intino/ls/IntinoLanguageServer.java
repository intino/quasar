package io.intino.ls;

import io.intino.tara.Tara;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static io.intino.ls.IntinoSemanticTokens.tokenModifiers;
import static io.intino.ls.IntinoSemanticTokens.tokenTypes;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class IntinoLanguageServer implements LanguageServer, LanguageClientAware {
	private final Tara dsl;
	private final DocumentManager documentManager;
	public HashMap<Object, Object> expectedRequests = new HashMap<>();

	public IntinoLanguageServer(Tara dsl, DocumentManager documentManager) {
		this.dsl = dsl;
		this.documentManager = documentManager;
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setSemanticTokensProvider(semanticTokensWithRegistrationOptions());
		capabilities.setDocumentHighlightProvider(new DocumentHighlightOptions());
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		return completedFuture(new InitializeResult(capabilities));
	}

	private static SemanticTokensWithRegistrationOptions semanticTokensWithRegistrationOptions() {
		return new SemanticTokensWithRegistrationOptions(new SemanticTokensLegend(tokenTypes, tokenModifiers), true);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return new IntinoDocumentService(dsl, documentManager);
	}

	@Override
	public WorkspaceService getWorkspaceService() {
		return null;
	}


	public BuildResult build() {
		return new BuildResult();
	}

	@Override
	public CompletableFuture<Object> shutdown() {
		return completedFuture(null);
	}

	@Override
	public void exit() {
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
