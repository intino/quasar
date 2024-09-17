package io.intino.ls.codeinsight.completion;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

import java.util.List;

public interface CompletionProvider {
	void addCompletions(CompletionContext context, List<CompletionItem> result);

	static CompletionItem create(String text, CompletionItemKind kind) {
		CompletionItem item = new CompletionItem();
		item.setInsertText(text);
		item.setKind(kind);
		return item;
	}
}