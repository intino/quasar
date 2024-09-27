package io.intino.ls.codeinsight.completion;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraGrammar.MogramReferenceContext;
import io.intino.tara.language.grammar.TaraGrammar.SignatureContext;
import io.intino.tara.language.model.Annotation;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.List;

import static io.intino.tara.language.semantics.Annotations.*;

public class AnnotationCompletionProvider implements CompletionProvider {
	@Override
	public void addCompletions(CompletionContext context, List<CompletionItem> result) {
		var rule = context.ruleOnPosition();
		if (rule instanceof TaraGrammar.AnnotationsContext) rule = rule.getParent();
		if (rule instanceof SignatureContext) addMogramAnnotations(result);
		else if (rule instanceof MogramReferenceContext) addHasAnnotations(result);
		else if (rule != null) addPropertyAnnotations(result);
	}

	private void addPropertyAnnotations(List<CompletionItem> result) {
		forProperty().stream().map(this::createCompletionItem).forEach(result::add);
	}

	private void addHasAnnotations(List<CompletionItem> result) {
		forMogramReference().stream().map(this::createCompletionItem).forEach(result::add);
	}

	private void addMogramAnnotations(List<CompletionItem> result) {
		forRootMogram().stream().map(this::createCompletionItem).forEach(result::add);
	}

	private CompletionItem createCompletionItem(Annotation annotation) {
		CompletionItem item = new CompletionItem(annotation.name().toLowerCase());
		item.setInsertText(annotation.name().toLowerCase());
		item.setKind(CompletionItemKind.Event);
		return item;
	}

}
