package io.intino.ls;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.IntinoSemanticTokens.SemanticToken;
import io.intino.ls.codeinsight.DiagnosticService;
import io.intino.ls.codeinsight.ReferenceResolver;
import io.intino.tara.Language;
import io.intino.tara.Source.StringSource;
import io.intino.tara.language.grammar.TaraLexer;
import io.intino.tara.language.model.Element;
import org.antlr.v4.runtime.CommonTokenStream;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.antlr.v4.runtime.CharStreams.fromString;

public class IntinoDocumentService implements TextDocumentService {
	private final Language language;
	private final DocumentManager workspaceManager;
	private final DocumentSourceProvider documentSourceProvider;
	private final DiagnosticService diagnosticService;
	private final ReferenceResolver referenceResolver;
	private final Map<URI, ModelUnit> models;

	public IntinoDocumentService(Language language, DocumentManager workspaceManager, DiagnosticService diagnosticService, Map<URI, ModelUnit> models) {
		this.language = language;
		this.workspaceManager = workspaceManager;
		this.documentSourceProvider = new DocumentSourceProvider(workspaceManager);
		this.diagnosticService = diagnosticService;
		referenceResolver = new ReferenceResolver(models);
		this.models = models;
		laodModels();
	}

	private void laodModels() {
		documentSourceProvider.all().forEach(u -> {
			try {
				diagnosticService.updateModel(new StringSource(u.uri().getPath(), new String(u.content().readAllBytes())));
			} catch (IOException e) {
				Logger.error(e);
			}
		});
	}

	@Override
	public void didOpen(DidOpenTextDocumentParams params) {
		URI uri = URI.create(normalize(params.getTextDocument().getUri()));
		diagnosticService.updateModel(new StringSource(uri.getPath(), params.getTextDocument().getText()));
	}

	@Override
	public void didChange(DidChangeTextDocumentParams params) {
		try {
			URI uri = URI.create(normalize(params.getTextDocument().getUri()));
			InputStream doc = workspaceManager.getDocumentText(uri);
			String content = applyChanges(doc != null ? new String(doc.readAllBytes()) : "", params.getContentChanges());
			workspaceManager.upsertDocument(uri, language.languageName(), content == null ? "" : content);
			diagnosticService.updateModel(new StringSource(uri.getPath(), content));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@Override
	public void didClose(DidCloseTextDocumentParams params) {
		System.out.println("Document closed: " + normalize(params.getTextDocument().getUri()));
	}

	@Override
	public void didSave(DidSaveTextDocumentParams params) {
		String uri = normalize(params.getTextDocument().getUri());
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

	@Override
	public CompletableFuture<DocumentDiagnosticReport> diagnostic(DocumentDiagnosticParams params) {
		return completedFuture(new DocumentDiagnosticReport(new RelatedFullDocumentDiagnosticReport(diagnosticService.analyzeWorkspace())));
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

	@Override
	public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
		try {
			return completedFuture(semanticTokens(URI.create(normalize(params.getTextDocument().getUri()))));
		} catch (IOException e) {
			Logger.error(e);
		}
		return completedFuture(new SemanticTokens());
	}

	private SemanticTokens semanticTokens(URI uri) throws IOException {
		List<SemanticToken> tokens = new ArrayList<>();
		try {
			TaraLexer lexer = new TaraLexer(fromString(new String(workspaceManager.getDocumentText(uri).readAllBytes()), uri.getPath()));
			lexer.reset();
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			tokenStream.fill();
			tokens.addAll(new IntinoSemanticTokens(List.of(language.lexicon())).semanticTokens(tokenStream));
		} catch (Throwable e) {
			Logger.error(e);
		}
		return new SemanticTokens(tokens.stream().flatMap(s -> s.raw().stream()).toList());
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
		URI uri = URI.create(normalize(params.getTextDocument().getUri()));
		Position position = params.getPosition();
		position.setLine(position.getLine() + 1);
		Element element = referenceResolver.resolveToDeclaration(uri, position);
		return completedFuture(Either.forRight(List.of(new LocationLink())));
	}

	@Override
	public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params) {
		URI uri = URI.create(normalize(params.getTextDocument().getUri()));
		Position position = params.getPosition();
		position.setLine(position.getLine() + 1);
		Element element = referenceResolver.resolveToDeclaration(uri, position);
		if (element == null) return completedFuture(Either.forLeft(List.of()));
		Range targetRange = rangeOf(element);
		position.setLine(position.getLine() - 1);
		return completedFuture(Either.forLeft(List.of(new Location(element.source().getPath(), targetRange))));
	}

	private Range rangeOf(Element element) {
		Element.TextRange textRange = element.textRange();
		return new Range(new Position(element.textRange().startLine() - 1, textRange.startColumn()),
				new Position(textRange.endLine() - 1, textRange.endColumn()));
	}

	@Override
	public CompletableFuture<List<? extends Location>> references(ReferenceParams params) {
		return TextDocumentService.super.references(params);
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
	public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
		return TextDocumentService.super.rename(params);//TODO
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
	public CompletableFuture<List<InlayHint>> inlayHint(InlayHintParams params) {
		return TextDocumentService.super.inlayHint(params);//TODO nombres de parametros
	}

	@Override
	public CompletableFuture<InlayHint> resolveInlayHint(InlayHint unresolved) {
		return TextDocumentService.super.resolveInlayHint(unresolved);
	}

	@Override
	public CompletableFuture<List<InlineValue>> inlineValue(InlineValueParams params) {
		return TextDocumentService.super.inlineValue(params);
	}

	public void setTrace(SetTraceParams params) {
	}

	private String normalize(String uri) {
		return uri.replace("file:///", "");
	}
}