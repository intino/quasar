package io.intino.ls;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraGrammarBaseListener;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.SemanticTokenTypes;

import java.util.List;

import static io.intino.ls.IntinoSemanticTokens.tokenModifiers;
import static io.intino.ls.IntinoSemanticTokens.tokenTypes;
import static java.lang.Integer.max;
import static org.eclipse.lsp4j.SemanticTokenModifiers.Declaration;
import static org.eclipse.lsp4j.SemanticTokenModifiers.Definition;
import static org.eclipse.lsp4j.SemanticTokenTypes.*;

class SemanticTokenVisitor extends TaraGrammarBaseListener {
	private final List<Integer> tokens;

	public SemanticTokenVisitor(List<Integer> tokens) {
		this.tokens = tokens;
	}

	@Override
	public void enterDslDeclaration(TaraGrammar.DslDeclarationContext ctx) {
		if (ctx.DSL() != null) addToken(ctx.DSL().getSymbol(), Keyword, Definition);
	}

	@Override
	public void enterProperty(TaraGrammar.PropertyContext ctx) {
		if (ctx.DEF() != null) addToken(ctx.DEF().getSymbol(), Keyword);
		if (ctx.propertyType() != null) {
			if (ctx.propertyType().DATE_TYPE() != null)
				addToken(ctx.propertyType().DATE_TYPE().getSymbol(), Type, Declaration);
			else if (ctx.propertyType().INT_TYPE() != null)
				addToken(ctx.propertyType().INT_TYPE().getSymbol(), Type, Declaration);
			else if (ctx.propertyType().DOUBLE_TYPE() != null)
				addToken(ctx.propertyType().DOUBLE_TYPE().getSymbol(), Type, Declaration);
			else if (ctx.propertyType().STRING_TYPE() != null)
				addToken(ctx.propertyType().STRING_TYPE().getSymbol(), Type, Declaration);
			else if (ctx.propertyType().INSTANT_TYPE() != null)
				addToken(ctx.propertyType().INSTANT_TYPE().getSymbol(), Type, Declaration);
			else if (ctx.propertyType().BOOLEAN_TYPE() != null)
				addToken(ctx.propertyType().BOOLEAN_TYPE().getSymbol(), Type, Declaration);
		}
	}

	@Override
	public void enterSignature(TaraGrammar.SignatureContext ctx) {
		if (ctx.metaidentifier() != null && ctx.metaidentifier().IDENTIFIER() != null)
			addToken(ctx.metaidentifier().IDENTIFIER().getSymbol(), SemanticTokenTypes.Class, Declaration);
		if (ctx.SUB() != null) addToken(ctx.SUB().getSymbol(), SemanticTokenTypes.Keyword);
		if (ctx.annotations() != null && ctx.annotations().AS() != null)
			addToken(ctx.annotations().AS().getSymbol(), Keyword);
		if (ctx.facetTarget() != null && ctx.facetTarget() != null)
			addToken(ctx.facetTarget().ON().getSymbol(), Keyword);
		if (ctx.with() != null) addToken(ctx.with().WITH().getSymbol(), Keyword);
		if (ctx.annotations() != null && ctx.facets() != null)
			addToken(ctx.facets().IS().getSymbol(), Keyword);

	}

	private void addToken(Token token, String type) {
		addToken(token, type, "");
	}

	private void addToken(Token token, String type, String modifier) {
		if (token == null) return;
		tokens.add(token.getLine() - 1);
		tokens.add(token.getCharPositionInLine());
		tokens.add(token.getText().length());
		tokens.add(tokenTypes.indexOf(type));
		tokens.add(max(tokenModifiers.indexOf(modifier), 0));
	}
}