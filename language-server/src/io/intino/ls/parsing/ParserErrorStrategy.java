package io.intino.ls.parsing;

import io.intino.tara.language.grammar.SyntaxException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Parser;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParserErrorStrategy extends DefaultErrorStrategy {
	List<SyntaxException> errors = new ArrayList<>();

	public ParserErrorStrategy() {
	}

	@Override
	public void reportError(org.antlr.v4.runtime.Parser recognizer, RecognitionException e) {
		Token token = recognizer.getCurrentToken();
		URI uri = URI.create(recognizer.getSourceName());
		errors.add(new SyntaxException("Syntax error in " + uri, uri, token.getLine(), token.getCharPositionInLine(), expectedTokens(recognizer)));
	}

	@Override
	public Token recoverInline(org.antlr.v4.runtime.Parser recognizer) throws RecognitionException {
		reportError(recognizer, new InputMismatchException(recognizer));
		return null;
	}

	@Override
	protected void reportNoViableAlternative(org.antlr.v4.runtime.Parser recognizer, NoViableAltException e) {
		reportError(recognizer, null);
	}

	@Override
	protected void reportInputMismatch(org.antlr.v4.runtime.Parser recognizer, InputMismatchException e) {
		reportError(recognizer, null);
	}

	@Override
	protected void reportFailedPredicate(org.antlr.v4.runtime.Parser recognizer, FailedPredicateException e) {
		reportError(recognizer, null);
	}

	@Override
	protected void reportUnwantedToken(org.antlr.v4.runtime.Parser recognizer) {
		reportError(recognizer, null);
	}

	@Override
	protected void reportMissingToken(Parser recognizer) {
		reportError(recognizer, null);
	}

	private String expectedTokens(org.antlr.v4.runtime.Parser recognizer) {
		try {
			return recognizer.getExpectedTokens().toString(recognizer.getVocabulary());
		} catch (Exception e) {
			return "";
		}
	}

	public Collection<SyntaxException> errors() {
		return errors;
	}
}
