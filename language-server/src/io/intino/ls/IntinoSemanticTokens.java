package io.intino.ls;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.intino.tara.language.grammar.TaraGrammar.*;
import static org.eclipse.lsp4j.SemanticTokenModifiers.Declaration;
import static org.eclipse.lsp4j.SemanticTokenModifiers.Documentation;
import static org.eclipse.lsp4j.SemanticTokenTypes.*;

public class IntinoSemanticTokens {
	public static final List<String> tokenTypes = new ArrayList<>();
	public static final List<String> tokenModifiers = new ArrayList<>();
	public static final Map<Integer, Integer> ANTLR_TOKEN_TYPES = new HashMap<>();
	public static final Map<Integer, Integer> ANTLR_TOKEN_MODIFIERS = new HashMap<>();

	static {
		tokenTypes.add(Class);
		tokenTypes.add(Keyword);
		tokenTypes.add(Comment);
		tokenTypes.add(Decorator);
		tokenTypes.add(Operator);
		tokenTypes.add(Type);
		tokenTypes.add(Modifier);
		tokenTypes.add(Property);
		tokenTypes.add(String);
		tokenTypes.add(Number);
		tokenTypes.add(Declaration);

		tokenModifiers.add(Documentation);

		ANTLR_TOKEN_MODIFIERS.put(DOC, tokenModifiers.indexOf(Documentation));
		ANTLR_TOKEN_TYPES.put(DSL, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(EMPTY, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(HAS, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(EXTENDS, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(AT, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(COMPONENT, tokenTypes.indexOf(Decorator));
		ANTLR_TOKEN_TYPES.put(PRIVATE, tokenTypes.indexOf(Decorator));
		ANTLR_TOKEN_TYPES.put(FEATURE, tokenTypes.indexOf(Decorator));
		ANTLR_TOKEN_TYPES.put(REACTIVE, tokenTypes.indexOf(Decorator));
		ANTLR_TOKEN_TYPES.put(DECORABLE, tokenTypes.indexOf(Decorator));
		ANTLR_TOKEN_TYPES.put(REQUIRED, tokenTypes.indexOf(Decorator));
		ANTLR_TOKEN_TYPES.put(FINAL, tokenTypes.indexOf(Decorator));
		ANTLR_TOKEN_TYPES.put(LINE_COMMENT, tokenTypes.indexOf(Comment));
		ANTLR_TOKEN_TYPES.put(IDENTIFIER, tokenTypes.indexOf(Class));
		ANTLR_TOKEN_TYPES.put(SUB, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(FACET, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(USE, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(IS, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(DEF, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(AS, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(WITH, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(COLON, tokenTypes.indexOf(Operator));
		ANTLR_TOKEN_TYPES.put(EQUALS, tokenTypes.indexOf(Operator));
		ANTLR_TOKEN_TYPES.put(LEFT_SQUARE, tokenTypes.indexOf(Operator));
		ANTLR_TOKEN_TYPES.put(RIGHT_SQUARE, tokenTypes.indexOf(Operator));
		ANTLR_TOKEN_TYPES.put(LEFT_CURLY, tokenTypes.indexOf(Operator));
		ANTLR_TOKEN_TYPES.put(RIGHT_CURLY, tokenTypes.indexOf(Operator));

		ANTLR_TOKEN_TYPES.put(WORD, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(STRING_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(DOUBLE_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(FUNCTION_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(RESOURCE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(OBJECT_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(INT_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(LONG_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(DATE_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(INSTANT_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(TIME_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(BOOLEAN_TYPE, tokenTypes.indexOf(Property));
		ANTLR_TOKEN_TYPES.put(DOC, tokenTypes.indexOf(Comment));
		ANTLR_TOKEN_TYPES.put(DOUBLE_VALUE, tokenTypes.indexOf(Number));
		ANTLR_TOKEN_TYPES.put(NATURAL_VALUE, tokenTypes.indexOf(Number));
		ANTLR_TOKEN_TYPES.put(NEGATIVE_VALUE, tokenTypes.indexOf(Number));
		ANTLR_TOKEN_TYPES.put(BOOLEAN_VALUE, tokenTypes.indexOf(Keyword));
		ANTLR_TOKEN_TYPES.put(QUOTE_BEGIN, tokenTypes.indexOf(String));
		ANTLR_TOKEN_TYPES.put(QUOTE_END, tokenTypes.indexOf(String));
		ANTLR_TOKEN_TYPES.put(CHARACTER, tokenTypes.indexOf(String));
	}

	private final List<java.lang.String> metaIdentifiers;

	public IntinoSemanticTokens(List<String> metaIdentifiers) {
		this.metaIdentifiers = metaIdentifiers;
	}

	public List<SemanticToken> semanticTokens(CommonTokenStream tokens) {
		int lastLine = 0;
		int lastCharPosition = 0;
		List<SemanticToken> tokenTypes = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			Integer type = ANTLR_TOKEN_TYPES.get(token.getType());
			if (type != null && (token.getType() != IDENTIFIER || metaIdentifiers.contains(token.getText()))) {
				int deltaLine = token.getLine() - 1 - lastLine;
				int deltaChar = (deltaLine == 0) ? token.getCharPositionInLine() - lastCharPosition : token.getCharPositionInLine();
				int length = token.getText().length();
				int modifier = ANTLR_TOKEN_MODIFIERS.getOrDefault(token.getType(), 0);
				tokenTypes.add(new SemanticToken(deltaLine, deltaChar, length, type, modifier));
				lastLine = token.getLine() - 1;
				lastCharPosition = token.getCharPositionInLine();
			}
		}
		return tokenTypes;
	}

	public record SemanticToken(int line, int column, int length, int type, int modifiers) {
		public List<Integer> raw() {
			return List.of(line, column, length, type, modifiers);
		}
	}
}