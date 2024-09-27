package io.intino.ls.codeinsight.completion;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.parsing.CompletionErrorStrategy;
import io.intino.ls.parsing.Parser;
import io.intino.tara.Language;
import io.intino.tara.Source;
import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.model.Element;
import io.intino.tara.language.model.ElementContainer;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.PropertyDescription;
import io.intino.tara.processors.Resolver;
import io.intino.tara.processors.model.Model;
import io.intino.tara.processors.parser.antlr.ModelGenerator;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Position;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static io.intino.ls.IntinoDocumentService.normalize;
import static io.intino.ls.codeinsight.completion.TreeUtils.getMogramContainerOn;

public class CompletionService {
	public static final String TARA_FAKE_TOKEN = "Tara_Fake_Token";
	private final Map<Predicate<CompletionContext>, CompletionProvider> map = new LinkedHashMap<>();

	public CompletionService() {
		map.put(ContextFilters.afterIs, (context, result) -> {
			if (!(context.elementOnPosition() instanceof TaraGrammar.MetaidentifierContext)) return;
			final CompletionUtils completionUtils = new CompletionUtils(context);
			Element element = context.elementOnPosition();
			if (element instanceof Mogram m) result.addAll(completionUtils.collectAllowedFacets(m));
		});
		map.put(ContextFilters.afterNewLineInBody, new BodyCompletionProvider());
		map.put(ContextFilters.afterAs, new AnnotationCompletionProvider());
		map.put(ContextFilters.afterNewLine, (context, result) -> {
					Element element = context.elementOnPosition();
					if (element instanceof Mogram m && ((Mogram) element).types().get(0).equals(TARA_FAKE_TOKEN))
						element = m.container();
					if (element instanceof ElementContainer ec) {
						new Resolver(context.language()).resolve(ec);
						result.addAll(new CompletionUtils(context).collectAllowedComponents(ec));
					}
				}
		);
		map.put(ContextFilters.afterMogramIdentifier, (context, result) -> {
					result.add(CompletionProvider.create("extends ", CompletionItemKind.Keyword));
					result.add(CompletionProvider.create("is ", CompletionItemKind.Keyword));
					result.add(CompletionProvider.create("as ", CompletionItemKind.Keyword));
				}
		);
		map.put(ContextFilters.inParameterName, (context, result) -> {
					if (!(context.elementOnPosition() instanceof PropertyDescription)) return;
					Element element = context.elementOnPosition();
					if (element instanceof Mogram m) result.addAll(new CompletionUtils(context).collectSignatureParameters(m));
				}
		);
	}

	public List<CompletionItem> propose(CompletionContext params) {
		List<CompletionItem> items = new ArrayList<>();
		map.keySet().stream()
				.filter(p -> p.test(params))
				.forEach(p -> map.get(p).addCompletions(params, items));
		return items;
	}


	public static CompletionContext completionContextOf(CompletionParams params, Language language, Source.StringSource originalDoc) {
		try {
			URI uri = URI.create(normalize(params.getTextDocument().getUri()));
			Position position = params.getPosition();
			position.setLine(position.getLine() + 1);
			String fakeDoc = new StringBuilder(originalDoc.stringContent()).insert(indexOf(position, originalDoc.stringContent()), TARA_FAKE_TOKEN).toString();
			Source.StringSource source = new Source.StringSource(originalDoc.uri().getPath(), fakeDoc);
			Parser parser = new Parser(source, new CompletionErrorStrategy());
			CommonTokenStream tokens = parser.tokens();
			TaraGrammar.RootContext tree = parser.parse(tokens);
			Model model = convert(source, tree);
			Token token = TreeUtils.findToken(tokens, position.getLine(), params.getPosition().getCharacter());
			ParserRuleContext ctx = token == null ? null : TreeUtils.findNodeContainingToken(tree, token);
			return new CompletionContext(uri, language, params.getPosition(), getMogramContainerOn(model, params.getPosition()),
					token, ctx, params.getContext().getTriggerCharacter());
		} catch (SyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	public static Model convert(Source.StringSource source, TaraGrammar.RootContext rootContext) throws SyntaxException {
		try {
			ModelGenerator extractor = new ModelGenerator(source);
			new ParseTreeWalker().walk(extractor, rootContext);
			return extractor.getModel();
		} catch (RecognitionException e) {
			return null;
		}
	}

	private static int indexOf(Position position, String textDoc) {
		String[] lines = textDoc.split("\n", -1);
		if (position.getLine() < 1 || position.getLine() > lines.length) return -1;
		if (position.getCharacter() < 0 || position.getCharacter() > lines[position.getLine() - 1].length() + 1)
			return -1;
		int absoluteIndex = 0;
		for (int i = 0; i < position.getLine() - 1; i++)
			absoluteIndex += lines[i].length() + 1;
		return absoluteIndex + position.getCharacter();
	}
}