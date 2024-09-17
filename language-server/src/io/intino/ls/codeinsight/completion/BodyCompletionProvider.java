package io.intino.ls.codeinsight.completion;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.model.Element;
import io.intino.tara.language.model.Level;
import io.intino.tara.language.model.Mogram;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.List;

import static io.intino.ls.codeinsight.ReferenceResolver.findMogramContainingToken;

public class BodyCompletionProvider implements CompletionProvider {
	@Override
	public void addCompletions(CompletionContext context, List<CompletionItem> result) {
		if (!(context.elementOnPosition() instanceof TaraGrammar.MetaidentifierContext)) return;
		Element element = findMogramContainingToken(context.model(), context.position());
		if (!(element instanceof Mogram m)) return;
		final CompletionUtils completionUtils = new CompletionUtils(context);
		result.addAll(completionUtils.collectAllowedComponents(m));
		result.addAll(completionUtils.collectBodyParameters(m));
		if (m.level() != Level.M1) addKeywords(result);
	}

	private void addKeywords(List<CompletionItem> resultSet) {
		resultSet.add(create("has ", CompletionItemKind.Keyword));
		resultSet.add(create("sub ", CompletionItemKind.Keyword));
		resultSet.add(create("def ", CompletionItemKind.Keyword));
	}

	private CompletionItem create(String text, CompletionItemKind kind) {
		CompletionItem item = new CompletionItem();
		item.setInsertText(text);
		item.setKind(kind);
		return item;
	}
}