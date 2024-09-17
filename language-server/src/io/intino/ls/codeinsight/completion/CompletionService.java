package io.intino.ls.codeinsight.completion;

import io.intino.ls.codeinsight.ReferenceResolver;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.model.Element;
import io.intino.tara.language.model.ElementContainer;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.PropertyDescription;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.*;
import java.util.function.BiPredicate;

public class CompletionService {
	private final Map<BiPredicate<Token, ParserRuleContext>, CompletionProvider> map = new LinkedHashMap<>();

	public CompletionService() {
		map.put(TaraFilters.afterIs, (context, result) -> {
			if (!(context.elementOnPosition() instanceof TaraGrammar.MetaidentifierContext)) return;
			final CompletionUtils completionUtils = new CompletionUtils(context);
			Element element = ReferenceResolver.findMogramContainingToken(context.model(), context.position());
			if (element == null) return;
			if (element instanceof Mogram m) result.addAll(completionUtils.collectAllowedFacets(m));
		});
		map.put(TaraFilters.afterNewLineInBody, new BodyCompletionProvider());
		map.put(TaraFilters.afterNewLine, (context, result) -> {
					Element element = ReferenceResolver.findMogramContainingToken(context.model(), context.position());
					if (element instanceof ElementContainer ec) result.addAll(new CompletionUtils(context).collectAllowedComponents(ec));
				}
		);
		map.put(TaraFilters.afterMogramIdentifier, (context, result) -> {
					result.add(CompletionProvider.create("extends ", CompletionItemKind.Keyword));
					result.add(CompletionProvider.create("is ", CompletionItemKind.Keyword));
					result.add(CompletionProvider.create("as ", CompletionItemKind.Keyword));
				}
		);
		map.put(TaraFilters.inParameterName, (context, result) -> {
					if (!(context.elementOnPosition() instanceof PropertyDescription)) return;
					Element element = ReferenceResolver.findMogramContainingToken(context.model(), context.position());
					if (element instanceof Mogram m) result.addAll(new CompletionUtils(context).collectSignatureParameters(m));
				}
		);
	}

	public List<CompletionItem> propose(CompletionContext params) {
		List<CompletionItem> items = new ArrayList<>();
		for (BiPredicate<Token, ParserRuleContext> predicate : map.keySet())
			if (predicate.test(params.tokenOnPosition(), params.elementOnPosition()))
				map.get(predicate).addCompletions(params, items);
		return items;
	}
}