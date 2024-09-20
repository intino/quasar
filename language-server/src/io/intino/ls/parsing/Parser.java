package io.intino.ls.parsing;

import io.intino.tara.Source;
import io.intino.tara.language.grammar.SyntaxException;
import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraLexer;
import io.intino.tara.processors.model.Model;
import io.intino.tara.processors.parser.GrammarErrorListener;
import io.intino.tara.processors.parser.antlr.ModelGenerator;
import io.intino.tara.processors.parser.antlr.TaraErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.antlr.v4.runtime.CharStreams.fromString;

public class Parser {
	private static final Logger LOG = Logger.getGlobal();
	private final Source source;
	private final DefaultErrorStrategy errorHandler;

	public Parser(Source source, DefaultErrorStrategy errorStrategy) {
		this.source = source;
		this.errorHandler = errorStrategy;
	}

	public Parser(Source source) {
		this.source = source;
		this.errorHandler = new TaraErrorStrategy();
	}

	public CommonTokenStream tokens() throws SyntaxException {
		try (InputStream content = source.content()) {
			TaraLexer lexer = new TaraLexer(fromString(new String(content.readAllBytes()), source.charset().name()));
			lexer.reset();
			return new CommonTokenStream(lexer);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage());
			return null;
		} catch (RecognitionException e) {
			throwError(e);
			return null;
		}
	}

	public TaraGrammar.RootContext parse(CommonTokenStream tokens) throws SyntaxException {
		try {
			TaraGrammar grammar = new TaraGrammar(tokens);
			grammar.setErrorHandler(errorHandler);
			grammar.addErrorListener(new GrammarErrorListener());
			return grammar.root();
		} catch (RecognitionException e) {
			LOG.log(Level.INFO, e.getMessage(), e);
			throwError(e);
			return null;
		}

	}

	public Model convert(TaraGrammar.RootContext rootContext) throws SyntaxException {
		try {
			ParseTreeWalker walker = new ParseTreeWalker();
			ModelGenerator extractor = new ModelGenerator(source);
			walker.walk(extractor, rootContext);
			if (!extractor.getErrors().isEmpty()) throw extractor.getErrors().get(0);
			return extractor.getModel();
		} catch (RecognitionException e) {
			LOG.log(Level.SEVERE, e.getMessage());
			return throwError(e);
		}
	}

	private Model throwError(RecognitionException e) throws SyntaxException {
		org.antlr.v4.runtime.Parser recognizer = (org.antlr.v4.runtime.Parser) e.getRecognizer();
		Token token = recognizer.getCurrentToken();
		throw new SyntaxException("Syntax error in " + source.uri(), source.uri(), token.getLine(), token.getCharPositionInLine(), getExpectedTokens(recognizer));
	}

	private String getExpectedTokens(org.antlr.v4.runtime.Parser recognizer) {
		try {
			return recognizer.getExpectedTokens().toString(recognizer.getVocabulary());
		} catch (Exception e) {
			LOG.log(Level.INFO, e.getMessage(), e);
			return "";
		}
	}
}