package io.intino.ls;

import org.antlr.v4.runtime.*;

public class ParserErrorStrategy extends DefaultErrorStrategy {

	@Override
	public void reportError(Parser recognizer, RecognitionException e) {
	}

	@Override
	protected void reportNoViableAlternative(Parser recognizer, NoViableAltException e) {
	}

	@Override
	protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
	}

	@Override
	protected void reportFailedPredicate(Parser recognizer, FailedPredicateException e) {
	}

	@Override
	protected void reportUnwantedToken(Parser recognizer) {
	}

	@Override
	protected void reportMissingToken(Parser recognizer) {
	}


	@Override
	public void reportMatch(Parser recognizer) {

	}
}
