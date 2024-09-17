package io.intino.ls;

import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.semantics.errorcollector.SemanticException;
import io.intino.tara.processors.dependencyresolution.DependencyException;
import io.intino.tara.processors.model.Model;
import org.antlr.v4.runtime.TokenStream;

import java.io.Serializable;
import java.util.List;

public record ModelUnit(Model model,
						TokenStream tokens,
						TaraGrammar.RootContext tree,
						List<SyntaxException> syntaxErrors,
						List<DependencyException> dependencyErrors,
						List<SemanticException> semanticErrors) implements Serializable {
}
