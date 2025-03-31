package io.intino.ls.parsing;

import io.intino.ls.ModelUnit;
import io.intino.tara.Source;
import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.processors.model.Model;
import org.antlr.v4.runtime.CommonTokenStream;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParsingService {
	private final Map<URI, ModelUnit> models;

	public ParsingService(Map<URI, ModelUnit> models) {
		this.models = models;
	}

	public synchronized void updateModel(Source source) {
		Model model = null;
		CommonTokenStream tokenStream = null;
		TaraGrammar.RootContext tree = null;
		ParserErrorStrategy strategy = new ParserErrorStrategy();
		List<SyntaxException> syntaxErrors = new ArrayList<>();
		Parser parser = new Parser(source, strategy);
		try {
			tokenStream = parser.tokens();
			tree = parser.parse(tokenStream);
			model = new Parser(source).convert(tree);
		} catch (SyntaxException e) {
			syntaxErrors.add(e);
		} catch (Exception e) {
			syntaxErrors.add(new SyntaxException(e.getMessage(), source.uri(), 0, 0, ""));
		}
		syntaxErrors.addAll(strategy.errors());
		models.put(source.uri(), new ModelUnit(model, tokenStream, tree, syntaxErrors, new ArrayList<>(), new ArrayList<>()));
	}
}