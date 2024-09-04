package io.intino.ls;

import io.intino.alexandria.logger.Logger;
import io.intino.tara.Language;
import io.intino.tara.Source;
import io.intino.tara.Source.StringSource;
import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.grammar.TaraGrammar.RootContext;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;
import io.intino.tara.processors.SemanticAnalyzer;
import io.intino.tara.processors.dependencyresolution.DependencyResolver;
import io.intino.tara.processors.model.Model;
import io.intino.tara.processors.parser.Parser;
import io.intino.tara.processors.parser.antlr.TaraErrorStrategy;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.eclipse.lsp4j.services.TextDocumentService;
import tara.dsl.Meta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class IntinoDocumentService implements TextDocumentService {
	private final Language language;
	private final WorkspaceManager workspaceManager;
	private final DocumentSourceProvider documentSourceProvider;
	private final Map<URI, Model> models = new HashMap<>();
	private final Map<URI, RootContext> trees = new HashMap<>();

	public IntinoDocumentService(Language language, WorkspaceManager workspaceManager) {
		this.language = language;
		this.workspaceManager = workspaceManager;
		this.documentSourceProvider = new DocumentSourceProvider(workspaceManager);
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		URI uri = URI.create(params.getTextDocument().getUri());
		workspaceManager.upsertDocument(uri, language.languageName(), "");
		analyzeText(new StringSource(uri.getPath(), params.getTextDocument().getText()));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		URI uri = URI.create(params.getTextDocument().getUri());
		try {
			InputStream doc = workspaceManager.getDocumentText(uri);
			String content = applyChanges(doc != null ? new String(doc.readAllBytes()) : "", params.getContentChanges());
			workspaceManager.upsertDocument(uri, language.languageName(), content == null ? "" : content);
			analyzeText(new StringSource(uri.getPath(), content));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		System.out.println("Document closed: " + params.getTextDocument().getUri());
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		String uri = params.getTextDocument().getUri();
		workspaceManager.upsertDocument(URI.create(uri), language.languageName(), params.getText());
	}

	private String applyChanges(String content, List<TextDocumentContentChangeEvent> contentChanges) {
		StringBuilder sb = new StringBuilder(content);
		for (TextDocumentContentChangeEvent change : contentChanges) {
			if (change.getRange() != null) {
				int startOffset = getOffset(change.getRange().getStart(), content);
				int endOffset = getOffset(change.getRange().getEnd(), content);
				sb.replace(startOffset, endOffset, change.getText());
			} else sb = new StringBuilder(change.getText());
		}
		return sb.toString();
	}

	private void analyzeText(Source source) {
		try {
			RootContext tree = parse(source, new TaraErrorStrategy());
			Model model = new Parser(source).convert(tree);
			models.put(source.uri(), model);
			DependencyResolver resolver = dependencyResolver(model);
			new SemanticAnalyzer(model, new Meta()).analyze();
		} catch (IOException e) {
			Logger.error(e);
		} catch (SyntaxException e) {
		} catch (SemanticFatalException e) {
		}
	}

	private int getOffset(Position position, String content) {
		int offset = 0;
		int line = 0;
		int column = 0;
		for (char c : content.toCharArray()) {
			if (line == position.getLine() && column == position.getCharacter()) break;
			if (c == '\n') {
				line++;
				column = 0;
			} else column++;
			offset++;
		}
		return offset;
	}

	private synchronized RootContext parse(Source source, DefaultErrorStrategy strategy) throws IOException, SyntaxException {
		Parser parser = new io.intino.tara.processors.parser.Parser(source, strategy);
		RootContext tree = parser.parse();
		trees.put(source.uri(), tree);
		return tree;
	}

	private static DependencyResolver dependencyResolver(io.intino.tara.processors.model.Model model) {
		return new DependencyResolver(model, new Meta(), "io.intino.test", new File("temp/src/io/intino/test/model/rules"), new File(Language.class.getProtectionDomain().getCodeSource().getLocation().getFile()), new File("temp"));
	}

	@Override
	public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
		var data = new ArrayList<Integer>();
		var result = new SemanticTokens();
		try {
			String uri = params.getTextDocument().getUri();
			InputStream content = workspaceManager.getDocumentText(URI.create(uri));
			RootContext tree = parse(new StringSource(uri, new String(content.readAllBytes())), new ParserErrorStrategy());
			SemanticTokenVisitor listener = new SemanticTokenVisitor(data);
			new ParseTreeWalker().walk(listener, tree);
			result.setData(data.stream().mapToInt(i -> i).boxed().toList());
		} catch (IOException e) {
			Logger.error(e);
		} catch (SyntaxException e) {
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
		//TODO
		return completedFuture(unresolved);
	}

	@Override
	public CompletableFuture<Hover> hover(HoverParams params) {
		return completedFuture(new Hover());
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
		return completedFuture(List.of());
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

	public void setTrace(SetTraceParams params) {

	}
}