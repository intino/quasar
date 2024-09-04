package io.intino.ls;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraGrammarBaseListener;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.SemanticTokenTypes;

import java.util.List;

import static org.eclipse.lsp4j.SemanticTokenModifiers.Declaration;
import static org.eclipse.lsp4j.SemanticTokenModifiers.Definition;
import static org.eclipse.lsp4j.SemanticTokenTypes.*;

class SemanticTokenVisitor extends TaraGrammarBaseListener {
	private final List<Integer> tokens;

	public SemanticTokenVisitor(List<Integer> tokens) {
		this.tokens = tokens;
	}

	@Override
	public void enterProperty(TaraGrammar.PropertyContext ctx) {
		if (ctx.DEF() != null) addToken(ctx.DEF().getSymbol(), Keyword, Definition);
		if (ctx.propertyType() != null) {
			if (ctx.propertyType().DATE_TYPE() != null)
				addToken(ctx.propertyType().DATE_TYPE().getSymbol(), Keyword, Declaration);
			else if (ctx.propertyType().INT_TYPE() != null)
				addToken(ctx.propertyType().INT_TYPE().getSymbol(), Keyword, Declaration);
			else if (ctx.propertyType().DOUBLE_TYPE() != null)
				addToken(ctx.propertyType().DOUBLE_TYPE().getSymbol(), Keyword, Declaration);
			else if (ctx.propertyType().STRING_TYPE() != null)
				addToken(ctx.propertyType().STRING_TYPE().getSymbol(), Keyword, Declaration);
			else if (ctx.propertyType().INSTANT_TYPE() != null)
				addToken(ctx.propertyType().INSTANT_TYPE().getSymbol(), Keyword, Declaration);
			else if (ctx.propertyType().BOOLEAN_TYPE() != null)
				addToken(ctx.propertyType().BOOLEAN_TYPE().getSymbol(), Keyword, Declaration);
		}
	}

	@Override
	public void enterSignature(TaraGrammar.SignatureContext ctx) {
		if (ctx.metaidentifier() != null && ctx.metaidentifier().IDENTIFIER() != null)
			addToken(ctx.metaidentifier().IDENTIFIER().getSymbol(), SemanticTokenTypes.Type, Declaration);
		if (ctx.SUB() != null) addToken(ctx.SUB().getSymbol(), SemanticTokenTypes.Keyword, null);
	}

	private void addToken(Token token, String type, String modifier) {
		if (token != null) {
			int line = token.getLine() - 1;
			int charPositionInLine = token.getCharPositionInLine();
			int length = token.getText().length();
			int tokenType = IntinoSemanticTokens.tokenTypes.indexOf(type);
			int tokenModifiers = modifier != null ? IntinoSemanticTokens.tokenModifiers.indexOf(modifier) : 0;
			tokens.add(line);
			tokens.add(charPositionInLine);
			tokens.add(length);
			tokens.add(tokenType);
			tokens.add(tokenModifiers);
		}
	}
}
