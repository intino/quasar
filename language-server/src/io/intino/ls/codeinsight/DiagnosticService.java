package io.intino.ls.codeinsight;

import io.intino.ls.ModelUnit;
import io.intino.ls.document.DocumentManager;
import io.intino.tara.Language;
import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.semantics.errorcollector.SemanticException;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;
import io.intino.tara.language.semantics.errorcollector.SemanticIssue;
import io.intino.tara.model.Element;
import io.intino.tara.model.Mogram;
import io.intino.tara.processors.SemanticAnalyzer;
import io.intino.tara.processors.dependencyresolution.DependencyException;
import io.intino.tara.processors.dependencyresolution.DependencyResolver;
import io.intino.tara.processors.model.Model;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import tara.dsl.Metta;

import java.io.File;
import java.net.URI;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiagnosticService {
	private final DocumentManager documentManager;
	private final Language language;
	private final Map<URI, ModelUnit> models;

	public DiagnosticService(DocumentManager documentManager, Language language, Map<URI, ModelUnit> models) {
		this.documentManager = documentManager;
		this.language = language;
		this.models = models;
	}

	public record Statistics(Map<String, Integer> mogramsPerUnit) {
	}

	public Statistics statistics() {
		return new Statistics(models.values().stream().collect(Collectors.toMap(m -> m.model().name(), m -> m.syntaxErrors().isEmpty() ? m.model().mograms().size() : 0)));
	}

	public List<Diagnostic> analyzeWorkspace() {
		ModelUnit model = merge(new ArrayList<>(models.values()));
		analyzeWorkspace(model);
		List<Diagnostic> diagnostics = new ArrayList<>();
		model.syntaxErrors().stream().map(DiagnosticService::diagnosticOf).forEach(diagnostics::add);
		model.dependencyErrors().stream().map(DiagnosticService::diagnosticOf).forEach(diagnostics::add);
		model.semanticErrors().stream().map(DiagnosticService::diagnosticOf).forEach(diagnostics::add);
		return diagnostics;
	}

	private ModelUnit merge(List<ModelUnit> units) {
		Model model = new Model(new File(documentManager.root().getPath()).getParentFile().toURI());
		ModelUnit reference = units.get(0);
		if (reference.model() == null)
			return new ModelUnit(model, reference.tokens(), reference.tree(), reference.syntaxErrors(), reference.dependencyErrors(), reference.semanticErrors());
		model.languageName(reference.model().languageName());
		units.stream().filter(u -> u.model() != null).forEach(unit -> unit.model().mograms()
				.forEach(c -> model.add(c, unit.model().rulesOf(c))));
		for (Mogram mogram : model.mograms()) mogram.container(model);
		return new ModelUnit(model, null, null,
				units.stream().flatMap(u -> u.syntaxErrors().stream()).collect(Collectors.toList()),
				units.stream().flatMap(u -> u.dependencyErrors().stream()).collect(Collectors.toList()),
				units.stream().flatMap(u -> u.semanticErrors().stream()).collect(Collectors.toList()));
	}

	private void analyzeWorkspace(ModelUnit context) {
		try {
			DependencyResolver dependencyResolver = dependencyResolver(context.model(), this.language);
			dependencyResolver.resolve();
			for (DependencyException e : dependencyResolver.rulesNotLoaded()) context.dependencyErrors().add(e);
			new SemanticAnalyzer(context.model(), language).analyze();
		} catch (DependencyException e) {
			context.dependencyErrors().add(e);
		} catch (SemanticFatalException e) {
			context.semanticErrors().addAll(e.exceptions());
		}
	}

	private static Diagnostic diagnosticOf(SyntaxException e) {
		Range range = new Range(new Position(e.getLine() - 1, e.getStartColumn()), new Position(e.getEndLine() - 1, e.getEndColumn()));
		return new Diagnostic(range, e.getOriginalMessage(), DiagnosticSeverity.Error, e.getUri().getPath());
	}

	private static Diagnostic diagnosticOf(DependencyException e) {
		Element.TextRange textRange = e.getElement().textRange();
		Range range = new Range(new Position(textRange.startLine() - 1, textRange.startColumn()), new Position(textRange.endLine() - 1, textRange.endColumn()));
		return new Diagnostic(range, e.getMessage().substring(0, e.getMessage().indexOf("@") - 1), DiagnosticSeverity.Error, e.getElement().source().getPath());
	}

	private static Diagnostic diagnosticOf(SemanticException e) {
		Element.TextRange textRange = e.origin()[0].textRange();
		Range range = new Range(new Position(textRange.startLine() - 1, textRange.startColumn()), new Position(textRange.endLine() - 1, textRange.endColumn()));
		DiagnosticSeverity level = e.level() == SemanticIssue.Level.ERROR ? DiagnosticSeverity.Error : DiagnosticSeverity.Warning;
		return new Diagnostic(range, e.getMessage(), level, e.getIssue().origin()[0].source().getPath());
	}

	private DependencyResolver dependencyResolver(Model model, Language language) {
		return new DependencyResolver(model, language, "io.intino.test", new File("temp/src/io/intino/test/model/rules"), new File(Language.class.getProtectionDomain().getCodeSource().getLocation().getFile()), new File("temp"));
	}
}