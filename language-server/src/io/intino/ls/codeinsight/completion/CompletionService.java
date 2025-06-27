package io.intino.ls.codeinsight.completion;

import io.intino.alexandria.logger.Logger;
import io.intino.ls.codeinsight.ReferenceResolver;
import io.intino.ls.parsing.CompletionErrorStrategy;
import io.intino.ls.parsing.Parser;
import io.intino.tara.Language;
import io.intino.tara.Source;
import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraGrammar.PropertyDescriptiveContext;
import io.intino.tara.language.grammar.TaraGrammar.SignaturePropertyContext;
import io.intino.tara.language.grammar.TaraGrammar.StringValueContext;
import io.intino.tara.model.*;
import io.intino.tara.model.rules.property.WordRule;
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
import static io.intino.ls.codeinsight.completion.CompletionProvider.create;
import static io.intino.ls.codeinsight.completion.CompletionProvider.createKeyword;
import static io.intino.ls.codeinsight.completion.TreeUtils.getMogramContainerOn;
import static io.intino.tara.model.Primitive.*;
import static org.eclipse.lsp4j.CompletionItemKind.Keyword;
import static org.eclipse.lsp4j.CompletionItemKind.Variable;

public class CompletionService {
	public static final String TARA_FAKE_TOKEN = "Tara_Fake_Token";
	private final Map<Predicate<CompletionContext>, CompletionProvider> map = new LinkedHashMap<>();

	public CompletionService(Language language) {
		map.put(ContextFilters.afterIs, (context, result) -> afterIs(context, result));
		map.put(ContextFilters.afterDef, (context, result) -> afterDef(result));
		map.put(ContextFilters.afterNewLineInBody, new BodyCompletionProvider(language));
		map.put(ContextFilters.afterAs, new AnnotationCompletionProvider());
		map.put(ContextFilters.afterNewLine, (context, result) -> afterNewLine(context, result));
		map.put(ContextFilters.afterEquals, (context, result) -> afterEquals(context, result));
		map.put(ContextFilters.afterMogramIdentifier, (context, result) -> afterMogramIdentifier(context, result));
		map.put(ContextFilters.inParameters, (context, result) -> inParameters(context, result));
	}

	public List<CompletionItem> propose(CompletionContext params) {
		List<CompletionItem> items = new ArrayList<>();
		map.keySet().stream()
				.filter(p -> p.test(params))
				.forEach(p -> map.get(p).addCompletions(params, items));
		return items;
	}

	private static void afterMogramIdentifier(CompletionContext context, List<CompletionItem> result) {
		if (!(context.mogramOnPosition() instanceof Mogram m)) return;
		if (m.level() != Level.M1)
			result.addAll(List.of(create("extends ", Keyword), create("as ", Keyword)));
		else if (!m.applicableFacets().isEmpty())
			result.add(create("is ", Keyword));
	}

	private static void afterEquals(CompletionContext context, List<CompletionItem> result) {
		Element valued = context.elementOnPosition();
		ParserRuleContext rule = context.ruleOnPosition();
		if (valued == null) return;
		new Resolver(context.language()).resolve(context.mogramOnPosition());
		if (valued instanceof Valued p && WORD.equals(p.type())) {
			if (p.rule(WordRule.class) != null)
				(p.rule(WordRule.class)).words().forEach(w -> result.add(create(w, CompletionItemKind.Enum)));
		} else if (valued instanceof Valued pd) {
			if (REFERENCE.equals(pd.type()) && !(rule.getParent() instanceof StringValueContext))

				result.add(createKeyword("empty"));

			else if (BOOLEAN.equals(pd.type()))
				result.addAll(List.of(createKeyword("true"), createKeyword("false")));
		}
	}

	private static void afterNewLine(CompletionContext context, List<CompletionItem> result) {
		ElementContainer ec = context.mogramOnPosition() instanceof Mogram ? context.mogramOnPosition().container() : context.mogramOnPosition();
		result.addAll(new CompletionUtils(context).collectAllowedComponents(ec));
	}

	private static void afterDef(List<CompletionItem> result) {
		for (Primitive primitive : Primitive.getPrimitives()) {
			String name = primitive.getName().toLowerCase();
			if (primitive.equals(WORD)) result.add(create(name, name + ":{}", Variable));
			else result.add(create(name, CompletionItemKind.Variable));
		}
	}

	private static void afterIs(CompletionContext context, List<CompletionItem> result) {
		if (!(context.ruleOnPosition() instanceof TaraGrammar.MetaidentifierContext)) return;
		final CompletionUtils completionUtils = new CompletionUtils(context);
		Element element = context.mogramOnPosition();
		if (element instanceof Mogram m) {
			new Resolver(context.language()).resolve(m);
			result.addAll(completionUtils.collectAllowedFacets(m));
		}
	}

	private static void inParameters(CompletionContext context, List<CompletionItem> result) {
		if (context.mogramOnPosition() instanceof Mogram m) {
			new Resolver(context.language()).resolve(m);
			result.addAll(new CompletionUtils(context).collectSignatureParameters(m));
		}
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
			Mogram container = getMogramContainerOn(model, params.getPosition());
			return new CompletionContext(uri, language, params.getPosition(), container, elementOnPosition(container, ctx, params.getPosition()),
					token, ctx, params.getContext().getTriggerCharacter());
		} catch (SyntaxException e) {
			Logger.error(e);
			return null;
		}
	}

	private static Element elementOnPosition(Mogram container, ParserRuleContext ctx, Position position) {
		if (container == null) return null;
		for (Element element : container.elements())
			if (element.textRange().startLine() == position.getLine()) return element;
		SignaturePropertyContext sigProperty = (SignaturePropertyContext) TreeUtils.contextOf(ctx, SignaturePropertyContext.class);
		if (sigProperty != null) return container.parameters().stream().filter(pd -> {
			if (sigProperty.IDENTIFIER() != null && pd.name().equals(sigProperty.IDENTIFIER().getText()))
				return true;
			return false;
		}).findFirst().orElse(null);
		PropertyDescriptiveContext description = (PropertyDescriptiveContext) TreeUtils.contextOf(ctx, PropertyDescriptiveContext.class);
		if (description != null) return container.parameters().stream().filter(pd -> {
			if (description.IDENTIFIER() != null && pd.name().equals(description.IDENTIFIER().getText()))
				return true;
			return false;
		}).findFirst().orElse(null);
		return null;
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