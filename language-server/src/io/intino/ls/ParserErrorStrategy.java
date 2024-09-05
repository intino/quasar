package io.intino.ls;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;

public class ParserErrorStrategy extends DefaultErrorStrategy {

	@Override
	public void reportError(Parser recognizer, RecognitionException e) {
	}
}
