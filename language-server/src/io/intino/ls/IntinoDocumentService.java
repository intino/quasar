package io.intino.ls;

import io.intino.alexandria.logger.Logger;
import io.intino.builder.CompilerMessage;
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
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
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
	private final WorkspaceManager documentManager;
	private final Checker checker;
	private final DocumentSourceProvider documentSourceProvider;

	public IntinoDocumentService(Tara dsl, WorkspaceManager workspaceManager) {
		this.documentManager = workspaceManager;
		documentSourceProvider = new DocumentSourceProvider(workspaceManager);
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

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		System.out.println("Document closed: " + params.getTextDocument().getUri());
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		documentManager.upsertDocument(URI.create(params.getTextDocument().getUri()), params.getText());
	}

	private void analyzeText(String uri) {
		List<CompilerMessage> run = runner.run(compilerConfiguration, documentSourceProvider);
	}

	@Override
	public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
		var data = new ArrayList<Integer>();
		var result = new SemanticTokens();
		try {
			InputStream content = documentManager.getDocumentText(URI.create(params.getTextDocument().getUri()));
			if (content == null) return CompletableFuture.completedFuture(result);
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


	@Override
	public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem unresolved) {
		return TextDocumentService.super.resolveCompletionItem(unresolved);
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		return TextDocumentService.super.hover(params);
	}

	@Override
	public CompletableFuture<SignatureHelp> signatureHelp(SignatureHelpParams params) {
		return TextDocumentService.super.signatureHelp(params);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> declaration(DeclarationParams params) {
		return TextDocumentService.super.declaration(params);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
		return TextDocumentService.super.definition(params);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> typeDefinition(TypeDefinitionParams params) {
		return TextDocumentService.super.typeDefinition(params);
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> implementation(ImplementationParams params) {
		return TextDocumentService.super.implementation(params);
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return TextDocumentService.super.references(params);
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(DocumentHighlightParams params) {
		return TextDocumentService.super.documentHighlight(params);
	}

	@Override
	public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
		return TextDocumentService.super.codeAction(params);
	}

	@Override
	public CompletableFuture<CodeAction> resolveCodeAction(CodeAction unresolved) {
		return TextDocumentService.super.resolveCodeAction(unresolved);
	}

	@Override
	public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams params) {
		return TextDocumentService.super.codeLens(params);
	}

	@Override
	public CompletableFuture<CodeLens> resolveCodeLens(CodeLens unresolved) {
		return TextDocumentService.super.resolveCodeLens(unresolved);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
		return TextDocumentService.super.formatting(params);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams params) {
		return TextDocumentService.super.rangeFormatting(params);
	}

	@Override
	public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams params) {
		return TextDocumentService.super.onTypeFormatting(params);
	}

	@Override
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return TextDocumentService.super.rename(params);
	}

	@Override
	public CompletableFuture<LinkedEditingRanges> linkedEditingRange(LinkedEditingRangeParams params) {
		return TextDocumentService.super.linkedEditingRange(params);
	}

	@Override
	public void willSave(WillSaveTextDocumentParams params) {
		TextDocumentService.super.willSave(params);
	}

	@Override
	public CompletableFuture<List<TextEdit>> willSaveWaitUntil(WillSaveTextDocumentParams params) {
		return TextDocumentService.super.willSaveWaitUntil(params);
	}

	@Override
	public CompletableFuture<List<DocumentLink>> documentLink(DocumentLinkParams params) {
		return TextDocumentService.super.documentLink(params);
	}

	@Override
	public CompletableFuture<DocumentLink> documentLinkResolve(DocumentLink params) {
		return TextDocumentService.super.documentLinkResolve(params);
	}

	@Override
	public CompletableFuture<List<ColorPresentation>> colorPresentation(ColorPresentationParams params) {
		return TextDocumentService.super.colorPresentation(params);
	}

	@Override
	public CompletableFuture<List<FoldingRange>> foldingRange(FoldingRangeRequestParams params) {
		return TextDocumentService.super.foldingRange(params);
	}

	@Override
	public CompletableFuture<Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior>> prepareRename(PrepareRenameParams params) {
		return TextDocumentService.super.prepareRename(params);
	}

	@Override
	public CompletableFuture<List<TypeHierarchyItem>> prepareTypeHierarchy(TypeHierarchyPrepareParams params) {
		return TextDocumentService.super.prepareTypeHierarchy(params);
	}

	@Override
	public CompletableFuture<List<TypeHierarchyItem>> typeHierarchySupertypes(TypeHierarchySupertypesParams params) {
		return TextDocumentService.super.typeHierarchySupertypes(params);
	}

	@Override
	public CompletableFuture<List<TypeHierarchyItem>> typeHierarchySubtypes(TypeHierarchySubtypesParams params) {
		return TextDocumentService.super.typeHierarchySubtypes(params);
	}

	@Override
	public CompletableFuture<List<CallHierarchyItem>> prepareCallHierarchy(CallHierarchyPrepareParams params) {
		return TextDocumentService.super.prepareCallHierarchy(params);
	}

	@Override
	public CompletableFuture<List<CallHierarchyIncomingCall>> callHierarchyIncomingCalls(CallHierarchyIncomingCallsParams params) {
		return TextDocumentService.super.callHierarchyIncomingCalls(params);
	}

	@Override
	public CompletableFuture<List<CallHierarchyOutgoingCall>> callHierarchyOutgoingCalls(CallHierarchyOutgoingCallsParams params) {
		return TextDocumentService.super.callHierarchyOutgoingCalls(params);
	}

	@Override
	public CompletableFuture<List<SelectionRange>> selectionRange(SelectionRangeParams params) {
		return TextDocumentService.super.selectionRange(params);
	}

	@Override
	public CompletableFuture<Either<SemanticTokens, SemanticTokensDelta>> semanticTokensFullDelta(SemanticTokensDeltaParams params) {
		return TextDocumentService.super.semanticTokensFullDelta(params);
	}

	@Override
	public CompletableFuture<SemanticTokens> semanticTokensRange(SemanticTokensRangeParams params) {
		return TextDocumentService.super.semanticTokensRange(params);
	}

	@Override
	public CompletableFuture<List<Moniker>> moniker(MonikerParams params) {
		return TextDocumentService.super.moniker(params);
	}

	@Override
	public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
		return TextDocumentService.super.inlayHint(params);
	}

	@Override
	public CompletableFuture<InlayHint> resolveInlayHint(InlayHint unresolved) {
		return TextDocumentService.super.resolveInlayHint(unresolved);
	}

	@Override
	public CompletableFuture<List<InlineValue>> inlineValue(InlineValueParams params) {
		return TextDocumentService.super.inlineValue(params);
	}

	@Override
	public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
		return TextDocumentService.super.diagnostic(params);
	}
}