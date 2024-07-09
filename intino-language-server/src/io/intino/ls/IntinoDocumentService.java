package io.intino.ls;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IntinoDocumentService implements TextDocumentService {

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		analyzeText(params.getTextDocument().getText());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		analyzeText(params.getTextDocument().toString());
	}

	private void analyzeText(String text) {
		//TODO bind with antlr
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		System.out.println("Document closed: " + params.getTextDocument().getUri());
	}

	@Override
	public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {

	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
		return CompletableFuture.completedFuture(highlights(params.getTextDocument()));
	}

	private List<? extends DocumentHighlight> highlights(TextDocumentIdentifier textDocument) {
		//TODO
		return List.of();
	}

	@Override
	public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
		CompletionItem item = new CompletionItem();
		item.setLabel("exampleCompletion");
		item.setInsertText("Example insert text");
//		item.setTextEdit(new TextEdit(null, "Example replacement text"));
		CompletionList completionList = new CompletionList();
		completionList.setIsIncomplete(false);
		completionList.setItems(Collections.singletonList(item));
		return CompletableFuture.completedFuture(Either.forRight(completionList));
	}
}