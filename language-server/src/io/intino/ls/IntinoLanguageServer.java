package io.intino.ls;

import io.intino.tara.Language;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.intino.ls.IntinoSemanticTokens.tokenModifiers;
import static io.intino.ls.IntinoSemanticTokens.tokenTypes;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class IntinoLanguageServer implements LanguageServer, LanguageClientAware {
	private final Language language;
	private final WorkspaceManager workspaceManager;
	public HashMap<Object, Object> expectedRequests = new HashMap<>();

	public IntinoLanguageServer(Language language, WorkspaceManager workspaceManager) {
		this.language = language;
		this.workspaceManager = workspaceManager;
	}

	@Override
	public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
		ServerCapabilities capabilities = new ServerCapabilities();
		capabilities.setSemanticTokensProvider(semanticTokensWithRegistrationOptions());
		capabilities.setDocumentHighlightProvider(new DocumentHighlightOptions());
		capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
		capabilities.setCompletionProvider(new CompletionOptions(true, List.of()));
//		capabilities.setHoverProvider(true);
//		capabilities.setCodeActionProvider(true);
//		capabilities.setDocumentSymbolProvider(true);
		WorkspaceServerCapabilities workspaceCaps = new WorkspaceServerCapabilities();
		WorkspaceFoldersOptions workspaceFolders = new WorkspaceFoldersOptions();
		workspaceFolders.setChangeNotifications(true);
		workspaceFolders.setSupported(true);
		workspaceCaps.setWorkspaceFolders(workspaceFolders);
		FileOperationsServerCapabilities fileCaps = new FileOperationsServerCapabilities();
		FileOperationOptions fileOptions = new FileOperationOptions();
		FileOperationFilter fileFilter = new FileOperationFilter();
		fileFilter.setPattern(new FileOperationPattern("*.tara"));
		fileOptions.setFilters(List.of(fileFilter));
		fileCaps.setDidCreate(fileOptions);
		capabilities.setWorkspace(workspaceCaps);
		return completedFuture(new InitializeResult(capabilities));
	}

	private static SemanticTokensWithRegistrationOptions semanticTokensWithRegistrationOptions() {
		SemanticTokensLegend legend = new SemanticTokensLegend(tokenTypes, tokenModifiers);
		return new SemanticTokensWithRegistrationOptions(legend, true);
	}

	@Override
	public TextDocumentService getTextDocumentService() {
		return new IntinoDocumentService(language, workspaceManager);
	}

	@Override
	public IntinoWorkspaceService getWorkspaceService() {
		return new IntinoWorkspaceService(language, workspaceManager);
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

	@Override
	public void setTrace(SetTraceParams params) {

	}
}