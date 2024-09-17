package io.intino.ls;

import io.intino.tara.language.grammar.SyntaxException;
import org.antlr.v4.runtime.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ParserErrorStrategy extends DefaultErrorStrategy {
	List<SyntaxException> errors = new ArrayList<>();

	public ParserErrorStrategy() {
	}

	@Override
	public void reportError(Parser recognizer, RecognitionException e) {
		Token token = recognizer.getCurrentToken();
		URI uri = URI.create(recognizer.getSourceName());
		errors.add(new SyntaxException("Syntax error in " + uri, uri, token.getLine(), token.getCharPositionInLine(), expectedTokens(recognizer)));
	}

	@Override
	public Token recoverInline(Parser recognizer) throws RecognitionException {
		reportError(recognizer, new InputMismatchException(recognizer));
		return null;
	}

	@Override
	protected void reportNoViableAlternative(Parser recognizer, NoViableAltException e) {
		reportError(recognizer, null);
	}

	@Override
	protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
		reportError(recognizer, null);
	}

	@Override
	protected void reportFailedPredicate(Parser recognizer, FailedPredicateException e) {
		reportError(recognizer, null);
	}

	@Override
	protected void reportUnwantedToken(Parser recognizer) {
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
