package io.intino.ls;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraGrammarBaseListener;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;

import java.util.List;

class SemanticTokenVisitor extends TaraGrammarBaseListener {
	private final List<Integer> tokens;

	public SemanticTokenVisitor(TaraGrammar grammar, List<Integer> tokens) {
		this.tokens = tokens;
	}

	@Override
	public void enterProperty(TaraGrammar.PropertyContext ctx) {
		addToken(ctx.IDENTIFIER().getSymbol(), SemanticTokenTypes.Variable, SemanticTokenModifiers.Declaration);
	}


	private void addToken(Token token, String type, String modifier) {
		if (token != null) {
			int line = token.getLine() - 1;
			int charPositionInLine = token.getCharPositionInLine();
			int length = token.getText().length();
			int tokenType = IntinoSemanticTokens.tokenTypes.indexOf(type);
			int tokenModifiers = IntinoSemanticTokens.tokenModifiers.indexOf(modifier);
			tokens.add(line);
			tokens.add(charPositionInLine);
			tokens.add(length);
			tokens.add(tokenType);
			tokens.add(tokenModifiers);
		}
	}
}
