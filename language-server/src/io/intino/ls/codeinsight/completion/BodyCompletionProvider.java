package io.intino.ls.codeinsight.completion;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.model.ElementContainer;
import io.intino.tara.model.Level;
import io.intino.tara.model.Mogram;
import io.intino.tara.processors.Resolver;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.List;

import static io.intino.ls.codeinsight.completion.CompletionProvider.create;

public class BodyCompletionProvider implements CompletionProvider {
	@Override
	public void addCompletions(CompletionContext context, List<CompletionItem> result) {
		if (!(context.ruleOnPosition() instanceof TaraGrammar.MetaidentifierContext)) return;
		if (!(context.elementOnPosition() instanceof Mogram m)) return;
		final CompletionUtils utils = new CompletionUtils(context);
		ElementContainer container = (ElementContainer) m.container();
		new Resolver(context.language()).resolve(container);
		result.addAll(utils.collectAllowedComponents(container));
		if (container instanceof Mogram mn) {
			result.addAll(utils.collectBodyParameters(mn));
			if (mn.level() != Level.M1) addKeywords(result);
		}
	}

	private void addKeywords(List<CompletionItem> resultSet) {
		resultSet.add(create("has ", CompletionItemKind.Keyword));
		resultSet.add(create("sub ", CompletionItemKind.Keyword));
		resultSet.add(create("def ", CompletionItemKind.Keyword));
	}

}