package io.intino.ls;

import io.intino.alexandria.logger.Logger;
import io.intino.tara.Language;
import io.intino.tara.Source;
import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.model.Element;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.semantics.errorcollector.SemanticException;
import io.intino.tara.language.semantics.errorcollector.SemanticFatalException;
import io.intino.tara.language.semantics.errorcollector.SemanticIssue;
import io.intino.tara.processors.SemanticAnalyzer;
import io.intino.tara.processors.dependencyresolution.DependencyException;
import io.intino.tara.processors.dependencyresolution.DependencyResolver;
import io.intino.tara.processors.model.Model;
import io.intino.tara.processors.parser.Parser;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import tara.dsl.Meta;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosticService {
	private final DocumentManager documentManager;
	private final Map<URI, ModelUnit> models;
	private final Map<URI, TaraGrammar.RootContext> trees = new HashMap<>();


	public DiagnosticService(DocumentManager documentManager, Map<URI, ModelUnit> models) {
		this.documentManager = documentManager;
		this.models = models;
	}


	public void updateModel(Source source) {
		Model model = null;
		List<SyntaxException> syntaxErrors = new ArrayList<>();
		try {
			TaraGrammar.RootContext tree = parse(source, new ParserErrorStrategy());
			model = new Parser(source).convert(tree);
		} catch (SyntaxException e) {
			syntaxErrors.add(e);
		} catch (Exception e) {
			Logger.error(e);
		}
		models.put(source.uri(), new ModelUnit(model, syntaxErrors, new ArrayList<>(), new ArrayList<>()));
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
		Model model = new Model(documentManager.root().getParentFile().toURI());
		Model ref = units.get(0).model();
		model.languageName(ref.languageName());
		units.forEach(unit -> unit.model().mograms()
				.forEach(c -> model.add(c, unit.model().rulesOf(c))));
		for (Mogram mogram : model.mograms()) mogram.container(model);
		return new ModelUnit(model, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

	private static void analyzeWorkspace(ModelUnit context) {
		try {
			dependencyResolver(context.model()).resolve();
			new SemanticAnalyzer(context.model(), new Meta()).analyze();
		} catch (DependencyException e) {
			context.dependencyErrors().add(e);
		} catch (SemanticFatalException e) {
			context.semanticErrors().addAll(e.exceptions());
		}
	}

	private static Diagnostic diagnosticOf(SyntaxException e) {
		Range range = new Range(new Position(e.getLine() - 1, e.getStartColumn()), new Position(e.getEndLine() - 1, e.getEndColumn()));
		return new Diagnostic(range, e.getMessage(), DiagnosticSeverity.Error, e.getSourceLocator().getPath());
	}

	private static Diagnostic diagnosticOf(DependencyException e) {
		Element.TextRange textRange = e.getElement().textRange();
		Range range = new Range(new Position(textRange.startLine() - 1, textRange.startColumn()), new Position(textRange.endLine() - 1, textRange.endColumn()));
		return new Diagnostic(range, e.getMessage(), DiagnosticSeverity.Error, e.getElement().source().getPath());
	}

	private static Diagnostic diagnosticOf(SemanticException e) {
		Element.TextRange textRange = e.origin()[0].textRange();
		Range range = new Range(new Position(textRange.startLine() - 1, textRange.startColumn()), new Position(textRange.endLine() - 1, textRange.endColumn()));
		DiagnosticSeverity level = e.level() == SemanticIssue.Level.ERROR ? DiagnosticSeverity.Error : DiagnosticSeverity.Warning;
		return new Diagnostic(range, e.getMessage(), level, e.getIssue().origin()[0].source().getPath());
	}

	private synchronized TaraGrammar.RootContext parse(Source source, DefaultErrorStrategy strategy) throws IOException, SyntaxException {
		Parser parser = new io.intino.tara.processors.parser.Parser(source, strategy);
		TaraGrammar.RootContext tree = parser.parse();
		trees.put(source.uri(), tree);
		return tree;
	}


	private static DependencyResolver dependencyResolver(io.intino.tara.processors.model.Model model) {
		return new DependencyResolver(model, new Meta(), "io.intino.test", new File("temp/src/io/intino/test/model/rules"), new File(Language.class.getProtectionDomain().getCodeSource().getLocation().getFile()), new File("temp"));
	}
}