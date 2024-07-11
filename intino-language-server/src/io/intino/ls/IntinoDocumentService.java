package io.intino.ls;

import io.intino.tara.Tara;
import io.intino.tara.builder.TaraCompilerRunner;
import io.intino.tara.builder.core.CompilerConfiguration;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class IntinoDocumentService implements TextDocumentService {
	private final Tara dsl;
	private final CompilerConfiguration compilerConfiguration;
	private final TaraCompilerRunner runner;

	public IntinoDocumentService(Tara dsl) {
		this.dsl = dsl;
		compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.language(dsl);
		runner = new TaraCompilerRunner(true);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		analyzeText(uriOf(params.getTextDocument().getUri()));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		analyzeText(uriOf(params.getTextDocument().getUri()));
	}

	private void analyzeText(URI uri) {
		runner.run(compilerConfiguration, Map.of(fileOf(uri),true));
	}

	private File fileOf(URI uri) {
		return null;
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

	private URI uriOf(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			return null;
		}
	}
}