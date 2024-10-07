package io.intino.ls.codeinsight.completion;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.model.Element;
import io.intino.tara.model.ElementContainer;
import io.intino.tara.model.Mogram;
import io.intino.tara.processors.model.Model;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.Position;

import java.util.Objects;

public class TreeUtils {
	public static ParserRuleContext contextOf(ParserRuleContext element, Class<? extends ParserRuleContext> contestClass) {
		return null;
	}

	public static ParserRuleContext getMogramContainerOf(ParserRuleContext context) {
		ParserRuleContext parent = context.getParent();
		while (parent != null) {
			if (parent instanceof TaraGrammar.MogramContext) return parent;
			parent = parent.getParent();
		}
		return null;
	}

	public static Mogram getMogramContainerOn(Model model, Position position) {
		return model.elements().stream()
				.map(element -> findInElement((ElementContainer) element, position))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	private static Mogram findInElement(ElementContainer element, Position position) {
		Element.TextRange range = element.textRange();
		if (!contains(range, position)) return null;
		if (element.mograms().isEmpty()) return (Mogram) element;
		else return element.mograms().stream()
				.map(mogram -> findInElement(mogram, position))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}

	public static boolean contains(Element.TextRange range, Position position) {
		return isBeforeOrEqual(new Position(range.startLine(), range.startColumn()), position) && isBeforeOrEqual(position, new Position(range.endLine(), range.endColumn()));
	}

	private static boolean isBefore(Position p1, Position p2) {
		return p1.getLine() < p2.getLine() || (p1.getLine() == p2.getLine() && p1.getCharacter() < p2.getCharacter());
	}

	private static boolean isBeforeOrEqual(Position p1, Position p2) {
		return p1.getLine() < p2.getLine() || (p1.getLine() == p2.getLine() && p1.getCharacter() <= p2.getCharacter());
	}

	private static boolean isAfter(Position p1, Position p2) {
		return isBefore(p2, p1);
	}

	public static Token findToken(TokenStream tokens, int line, int column) {
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			if (token.getLine() == line) {
				int tokenStart = token.getCharPositionInLine();
				int tokenEnd = tokenStart + token.getText().length();
				if (column >= tokenStart && column < tokenEnd) {
					return token;
				}
			}
		}
		return null;
	}

	public static ParserRuleContext findNodeContainingToken(ParserRuleContext ctx, Token token) {
		Token startToken = ctx.getStart();
		Token stopToken = ctx.getStop();
		if (token.getTokenIndex() >= startToken.getTokenIndex() && token.getTokenIndex() <= stopToken.getTokenIndex()) {
			for (int i = 0; i < ctx.getChildCount(); i++) {
				ParseTree child = ctx.getChild(i);
				if (child instanceof ParserRuleContext) {
					ParserRuleContext result = findNodeContainingToken((ParserRuleContext) child, token);
					if (result != null) return result;
				}
			}
			return ctx;
		}
		return null;
	}
}
