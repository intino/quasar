package io.intino.ls.codeinsight.completion;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraGrammar.BodyContext;
import io.intino.tara.language.grammar.TaraGrammar.SignatureContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.function.Predicate;

import static io.intino.tara.language.grammar.TaraGrammar.IDENTIFIER;


class ContextFilters {
	public static final Predicate<CompletionContext> afterIs = new AfterIsFilter().and(new InFacetFilter());
	public static final Predicate<CompletionContext> afterAs = new AfterElementTypeFitFilter(TaraGrammar.AS);
	public static final Predicate<CompletionContext> afterNewLineInBody = new AfterNewLineInBodyFilter();
	public static final Predicate<CompletionContext> afterNewLine = new AfterNewLineRootFilter();
	public static final Predicate<CompletionContext> afterEquals = new AfterEqualsFilter();
	public static final Predicate<CompletionContext> afterMogramIdentifier = new AfterElementTypeFitFilter(IDENTIFIER);
	public static final Predicate<CompletionContext> inParameters = new InParameters().and(afterEquals.negate());

	private ContextFilters() {
	}

	private static class AfterElementTypeFitFilter implements Predicate<CompletionContext> {
		int type;

		private AfterElementTypeFitFilter(int type) {
			this.type = type;
		}

		@Override
		public boolean test(CompletionContext context) {
			if (context == null) return false;
			ParseTree prevSibling = prevSibling(context.ruleOnPosition(), context.tokenOnPosition());
			return prevSibling instanceof TerminalNode tn && type == tn.getSymbol().getType();
		}
	}

	private static class AfterNewLineInBodyFilter implements Predicate<CompletionContext> {
		@Override
		public boolean test(CompletionContext context) {
			return context.ruleOnPosition() != null && !(context.ruleOnPosition().getParent() instanceof TaraGrammar.FacetContext) &&
					inBody(context.ruleOnPosition()) && afterNewLine(context.ruleOnPosition());
		}

		private boolean afterNewLine(ParserRuleContext context) {
			return prevSibling(context) == null && ContextFilters.in(context, SignatureContext.class);
		}

		private boolean inBody(ParserRuleContext context) {
			return context instanceof TaraGrammar.MetaidentifierContext && ContextFilters.in(context, BodyContext.class) && !inAnnotations(context);
		}
	}

	private static class AfterIsFilter implements Predicate<CompletionContext> {
		@Override
		public boolean test(CompletionContext context) {
			return isAcceptable(context.tokenOnPosition(), context.ruleOnPosition()) &&
					in(context.ruleOnPosition(), SignatureContext.class) &&
					afterIs(context.ruleOnPosition());
		}

		private boolean afterIs(ParserRuleContext context) {
			final ParserRuleContext parent = context.getParent().getParent();
			return parent != null && hasPreviousIs(parent);
		}

		private boolean hasPreviousIs(ParserRuleContext parent) {
			ParseTree prevSibling = prevSibling(parent);
			while (prevSibling != null) {
				if (is(prevSibling, TaraGrammar.IS)) return true;
				prevSibling = prevSibling((ParserRuleContext) prevSibling);
			}
			return false;
		}

		private boolean isAcceptable(Object element, ParserRuleContext context) {
			return context != null && context instanceof ParserRuleContext && context.getParent() != null;
		}
	}

	private static class AfterEqualsFilter implements Predicate<CompletionContext> {
		@Override
		public boolean test(CompletionContext context) {
			final ParserRuleContext realContext = TreeUtils.contextOf(context.ruleOnPosition(), TaraGrammar.ValueContext.class);
			return isCandidate(context.ruleOnPosition(), realContext) &&
					prevSibling(realContext) != null &&
					isPreviousEquals(realContext);
		}

		private boolean isPreviousEquals(ParserRuleContext context) {
			ParseTree token = prevSibling(context);
			return token instanceof TerminalNode t && t.getSymbol().getType() == TaraGrammar.EQUALS;
		}
	}

	private static class AfterNewLineRootFilter implements Predicate<CompletionContext> {
		@Override
		public boolean test(CompletionContext context) {
			return context.position().getCharacter() == 0;
		}
	}

	private static boolean isCandidate(Object element, ParserRuleContext context) {
		return element instanceof ParserRuleContext && context != null && prevSibling(context) != null;
	}

	private static class InFacetFilter implements Predicate<CompletionContext> {
		@Override
		public boolean test(CompletionContext context) {
			return acceptableParent(context.tokenOnPosition(), context.ruleOnPosition()) &&
					!(facet(context.ruleOnPosition()) && TreeUtils.getMogramContainerOf(context.ruleOnPosition()) == null);
		}

		private boolean facet(ParserRuleContext context) {
			return context.getParent() instanceof TaraGrammar.MetaidentifierContext && !inAnnotations(context);
		}
	}

	private static class InParameters implements Predicate<CompletionContext> {
		@Override
		public boolean test(CompletionContext context) {
			return acceptableParent(context.elementOnPosition(), context.ruleOnPosition()) &&
					parameter(context.ruleOnPosition()) &&
					TreeUtils.getMogramContainerOf(context.ruleOnPosition()) != null;
		}

		private boolean parameter(ParserRuleContext context) {
			return in(context, TaraGrammar.SignaturePropertyContext.class);
		}
	}

	private static ParseTree prevSibling(ParserRuleContext ctx) {
		if (ctx == null || ctx.parent == null) return null;
		int indexOfCurrentChildNode = ctx.getParent().children.indexOf(ctx);
		return indexOfCurrentChildNode <= 0 ? null : ctx.parent.getChild(indexOfCurrentChildNode - 1);
	}


	private static ParseTree prevSibling(ParserRuleContext ctx, Token token) {
		if (ctx == null || ctx.parent == null) return null;
		ParseTree childNode = ctx.children.stream().filter(t -> t instanceof TerminalNode tn && tn.getSymbol().equals(token)).findFirst().orElse(null);
		if (childNode == null) return null;
		int i = ctx.children.indexOf(childNode);
		return i <= 0 ? null : ctx.children.get(i - 1);
	}

	private static boolean acceptableParent(Object element, ParserRuleContext context) {
		return context instanceof ParserRuleContext && context != null && context.getParent() != null;
	}

	private static boolean in(ParserRuleContext context, Class<? extends ParserRuleContext> target) {
		ParserRuleContext parent = context.getParent();
		while (parent != null) {
			if (target.isInstance(parent)) return true;
			parent = parent.getParent();
		}
		return false;
	}

	private static boolean inAnnotations(ParserRuleContext context) {
		ParserRuleContext parent = context.getParent();
		while (parent != null) {
			if (parent instanceof TaraGrammar.AnnotationsContext) return true;
			parent = parent.getParent();
		}

		return false;
	}

	private static boolean previousNewLine(ParserRuleContext context) {
		return prevSibling(context) != null && (is(context, TaraGrammar.NEWLINE) || context instanceof TaraGrammar.ImportsContext ||
				context instanceof TaraGrammar.DslDeclarationContext);
	}

	private static boolean previousNewLineIndent(ParserRuleContext context) {
		return prevSibling(context) != null && is(context, TaraGrammar.NEW_LINE_INDENT);
	}

	private static boolean is(ParseTree context, int type) {
		return true;
//		return prevSibling(context) instanceof ParserRuleContext pc && pc.getToken(type, 0) != null;
	}
}
