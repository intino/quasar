package io.intino.ls.codeinsight.completion;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class TreeUtils {
	public static ParserRuleContext contextOf(ParserRuleContext element, Class<? extends ParserRuleContext> contestClass) {
		return null;
	}

	public static ParserRuleContext getMogramContainerOf(ParserRuleContext context) {
		return null;
	}

	public Token findToken(TokenStream tokens, int line, int column) {
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

	public ParserRuleContext findNodeContainingToken(ParserRuleContext ctx, Token token) {
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
