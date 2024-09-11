package io.intino.ls;

import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.model.Element;
import io.intino.tara.language.semantics.errorcollector.SemanticException;
import io.intino.tara.language.semantics.errorcollector.SemanticIssue;
import io.intino.tara.processors.dependencyresolution.DependencyException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiagnosticService {
	private final DocumentManager documentManager;
	private final Map<URI, ModelContext> models;

	public DiagnosticService(DocumentManager documentManager, Map<URI, ModelContext> models) {
		this.documentManager = documentManager;
		this.models = models;
	}

	public List<Diagnostic> analyze(URI uri) {
		ModelContext model = models.get(uri);
		if (model == null) return List.of();
		List<Diagnostic> diagnostics = new ArrayList<>();
		for (SyntaxException e : model.syntaxErrors()) {
			Diagnostic d = new Diagnostic();
			d.setRange(new Range(new Position(e.getLine() - 1, e.getStartColumn()), new Position(e.getEndLine() - 1, e.getEndColumn())));
			d.setMessage(e.getMessage());
			d.setSeverity(DiagnosticSeverity.Error);
			diagnostics.add(d);
		}

		for (DependencyException e : model.dependencyErrors()) {
			Diagnostic d = new Diagnostic();
			Element.TextRange textRange = e.getElement().textRange();
			d.setRange(new Range(new Position(textRange.line() - 1, textRange.startColumn()), new Position(textRange.line(), textRange.startColumn())));
			d.setMessage(e.getMessage());
			d.setSeverity(DiagnosticSeverity.Error);
			diagnostics.add(d);
		}
		for (SemanticException e : model.semanticErrors()) {
			Diagnostic d = new Diagnostic();
			Element.TextRange textRange = e.origin()[0].textRange();
			d.setRange(new Range(new Position(textRange.line() - 1, textRange.startColumn()), new Position(textRange.line() - 1, textRange.startColumn() + 1)));
			d.setMessage(e.getMessage());
			d.setSeverity(e.level() == SemanticIssue.Level.ERROR ? DiagnosticSeverity.Error : DiagnosticSeverity.Warning);
			diagnostics.add(d);
		}
		return diagnostics;
	}
}
