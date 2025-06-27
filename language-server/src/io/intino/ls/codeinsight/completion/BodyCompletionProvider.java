package io.intino.ls.codeinsight.completion;

import io.intino.tara.Language;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.model.ElementContainer;
import io.intino.tara.model.Level;
import io.intino.tara.model.Mogram;
import io.intino.tara.processors.Resolver;
import org.eclipse.lsp4j.CompletionItem;

import java.util.List;

import static io.intino.ls.codeinsight.completion.CompletionProvider.create;
import static org.eclipse.lsp4j.CompletionItemKind.Keyword;

public class BodyCompletionProvider implements CompletionProvider {
	private final Language language;

	public BodyCompletionProvider(Language language) {
		this.language = language;
	}

	@Override
	public void addCompletions(CompletionContext context, List<CompletionItem> result) {
		if (!(context.ruleOnPosition() instanceof TaraGrammar.MetaidentifierContext)) return;
		if (!(context.mogramOnPosition() instanceof Mogram m)) return;
		final CompletionUtils utils = new CompletionUtils(context);
		ElementContainer container = (ElementContainer) m.container();
		new Resolver(context.language()).resolve(container);
		result.addAll(utils.collectAllowedComponents(container));
		if (container instanceof Mogram mn) result.addAll(utils.collectBodyProperties(mn));
		if (m.level() != null && m.level() != Level.M1 || m.level() == null && ((Mogram) m.container()).level() != Level.M1)
			addKeywords(result);
	}

	private void addKeywords(List<CompletionItem> resultSet) {
		resultSet.add(create("has ", Keyword));
		resultSet.add(create("sub ", Keyword));
		resultSet.add(create("def ", Keyword));
		resultSet.add(create("facet ", Keyword));
	}

}