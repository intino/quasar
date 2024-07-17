package io.intino.ls;

import io.intino.alexandria.logger.Logger;
import io.intino.tara.Checker;
import io.intino.tara.Tara;
import io.intino.tara.builder.TaraCompilerRunner;
import io.intino.tara.builder.core.CompilerConfiguration;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class IntinoDocumentService implements TextDocumentService {
	private final CompilerConfiguration compilerConfiguration;
	private final TaraCompilerRunner runner;
	private final DocumentManager documentManager;
	private final Checker checker;
	private final DocumentSourceProvider documentSourceProvider;

	public IntinoDocumentService(Tara dsl, DocumentManager documentManager) {
		this.documentManager = documentManager;
		documentSourceProvider = new DocumentSourceProvider(documentManager);
		compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.language(dsl);
		runner = new TaraCompilerRunner(true);
		checker = new Checker(dsl);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		analyzeText(params.getTextDocument().getUri());
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		analyzeText(params.getTextDocument().getUri());
	}

	private void analyzeText(String uri) {
		runner.run(compilerConfiguration, documentSourceProvider);
	}


	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		System.out.println("Document closed: " + params.getTextDocument().getUri());
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		documentManager.upsertDocument(URI.create(params.getTextDocument().getUri()), params.getText());
	}

	@Override
	public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
		var data = new ArrayList<Integer>();
		var result = new SemanticTokens();
		try {
			InputStream content = documentManager.getDocumentText(URI.create(params.getTextDocument().getUri()));
			TaraLexer lexer = new TaraLexer(CharStreams.fromStream(content));
			lexer.reset();
			var parser = new TaraGrammar(new CommonTokenStream(lexer));
			SemanticTokenVisitor listener = new SemanticTokenVisitor(parser, data);
			new ParseTreeWalker().walk(listener, parser.root());
			result.setData(data.stream().mapToInt(i -> i).boxed().toList());
		} catch (IOException e) {
			Logger.error(e);
		}
		return completedFuture(result);
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
		return completedFuture(Either.forRight(completionList));
	}

}