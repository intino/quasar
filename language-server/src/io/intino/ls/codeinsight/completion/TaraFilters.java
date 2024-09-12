package io.intino.ls.codeinsight.completion;

import io.intino.tara.language.grammar.TaraGrammar;
import io.intino.tara.language.grammar.TaraGrammar.BodyContext;
import io.intino.tara.language.grammar.TaraGrammar.PropertyDescriptiveContext;
import io.intino.tara.language.grammar.TaraGrammar.SignatureContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.function.BiPredicate;

import static io.intino.tara.language.grammar.TaraGrammar.IDENTIFIER;


class TaraFilters {

	static final BiPredicate<Token, ParserRuleContext> AfterNewLine = new AfterNewLinePrimalFilter();
	static final BiPredicate<Token, ParserRuleContext> afterIs = new AfterIsFilter().and(new InFacetFilter());
	static final BiPredicate<Token, ParserRuleContext> afterNewLineInBody = new AfterNewLineInBodyFilter().and(afterIs.negate());
	static final BiPredicate<Token, ParserRuleContext> afterEquals = new AfterEqualsFilter();
	static final BiPredicate<Token, ParserRuleContext> afterNodeIdentifier = new AfterElementTypeFitFilter(IDENTIFIER);
	static final BiPredicate<Token, ParserRuleContext> inParameterName = new InParameters().and(afterEquals.negate());

	private TaraFilters() {
	}

	private static class AfterElementTypeFitFilter implements BiPredicate<Token, ParserRuleContext> {
		int type;

		private AfterElementTypeFitFilter(int type) {
			this.type = type;
		}

		@Override
		public boolean test(Token token, ParserRuleContext context) {
			if (context == null) return false;
			ParserRuleContext prevSibling = prevSibling(context) != null ? prevSibling(context) : prevSibling(context.getParent());
			if (prevSibling != null && prevSibling(prevSibling) != null) {
				ParserRuleContext prevPrevSibling = prevSibling(prevSibling);
				if (token instanceof ParserRuleContext) {
//					if (prevSibling.getElementType() == TaraGrammar.WHITE_SPACE && type.equals(prevPrevSibling.getElementType()))
//						return true;
//					else return type.equals(prevSibling.getElementType());
				}
			}
			return false;
		}
	}

	private static class AfterNewLineInBodyFilter implements BiPredicate<Token, ParserRuleContext> {
		@Override
		public boolean test(Token element, ParserRuleContext context) {
			return isElementAcceptable(element, context) && inBody(context) && afterNewLine(context);
		}

		private boolean afterNewLine(ParserRuleContext context) {
			return prevSibling(context) == null && TaraFilters.in(context, SignatureContext.class);
		}

		private boolean inBody(ParserRuleContext context) {
			return context.getParent() instanceof TaraGrammar.MetaidentifierContext && TaraFilters.in(context, BodyContext.class) && !inAnnotations(context);
		}

		private boolean isElementAcceptable(Object element, ParserRuleContext context) {
			return (element instanceof ParserRuleContext) && context != null && context.getParent() != null;
		}
	}

	private static class AfterIsFilter implements BiPredicate<Token, ParserRuleContext> {
		@Override
		public boolean test(Token element, ParserRuleContext context) {
			return !isNotAcceptable(element, context) && in(context, SignatureContext.class) && afterIs(context);
		}

		private boolean afterIs(ParserRuleContext context) {
			final ParserRuleContext parent = context.getParent().getParent();
			return parent != null && hasPreviousAs(parent);
		}

		private boolean hasPreviousAs(ParserRuleContext parent) {
			ParserRuleContext prevSibling = prevSibling(parent);
			while (prevSibling != null) {
				if (is(prevSibling, TaraGrammar.IS)) return true;
				prevSibling = prevSibling(prevSibling);
			}
			return false;
		}

		private boolean isNotAcceptable(Object element, ParserRuleContext context) {
			return !(element instanceof ParserRuleContext) || context == null || context.getParent() == null;
		}
	}

	private static class AfterEqualsFilter implements BiPredicate<Token, ParserRuleContext> {
		@Override
		public boolean test(Token element, ParserRuleContext context) {
			final ParserRuleContext realContext = TreeUtils.contextOf(context, TaraGrammar.ValueContext.class);
			return isCandidate(element, realContext) && prevSibling(prevSibling(realContext)) != null && isPreviousEquals(realContext);
		}

		private boolean isPreviousEquals(ParserRuleContext context) {
			return prevSibling(prevSibling(context)).getToken(TaraGrammar.EQUALS, 0) != null;
		}
	}

	private static class AfterNewLinePrimalFilter implements BiPredicate<Token, ParserRuleContext> {

		@Override
		public boolean test(Token element, ParserRuleContext context) {
			if (!(element instanceof ParserRuleContext) || context == null || context.getParent() == null) return false;
			if (context.getParent() instanceof TaraGrammar.MetaidentifierContext && !new AfterIsFilter().test(element, context) && !inAnnotations(context)) {
				ParserRuleContext containerContext = TreeUtils.getMogramContainerOf(context);
				if (containerContext == null || prevSibling(containerContext) == null) return false;
				return !TaraFilters.in(context, BodyContext.class) && (previousNewLine(containerContext) || previousNewLineIndent(containerContext));
			}
			return false;
		}
	}

	private static boolean isCandidate(Object element, ParserRuleContext context) {
		return element instanceof ParserRuleContext && context != null && prevSibling(context) != null;
	}

	private static class InFacetFilter implements BiPredicate<Token, ParserRuleContext> {
		@Override
		public boolean test(Token element, ParserRuleContext context) {
			return acceptableParent(element, context) && !(facet(context) && TreeUtils.getMogramContainerOf(context) == null);
		}

		private boolean facet(ParserRuleContext context) {
			return context.getParent() instanceof TaraGrammar.MetaidentifierContext && !inAnnotations(context);
		}
	}

	private static class InParameters implements BiPredicate<Token, ParserRuleContext> {
		@Override
		public boolean test(Token element, ParserRuleContext context) {
			return acceptableParent(element, context) && (parameter(context) && TreeUtils.getMogramContainerOf(context) != null);
		}

		private boolean parameter(ParserRuleContext context) {
			return in(context, PropertyDescriptiveContext.class);
		}
	}

	static class AfterAsFitFilter implements BiPredicate<Token, ParserRuleContext> {
		public boolean test(Token element, ParserRuleContext context) {
			if (context == null) return false;
//			ParserRuleContext ctx = (prevSibling(context) != null) ? context : context.getParent();
//			while (prevSibling(ctx) != null && prevSibling(ctx).getToken(IDENTIFIER, 0) != null) {
//				if (AS.equals(ctx.getElementType())) return true;
//				ctx = prevSibling(ctx);
//			}
//			ctx = ctx.getParent();
//			while (ctx != null && !(ctx instanceof Mogram)) {
//				if (ctx instanceof TaraGrammar.AnnotationsContext) return true;
//				ctx = ctx.getParent();
//			}
			return false;
		}
	}

	private static ParserRuleContext prevSibling(ParserRuleContext ctx) {
		int indexOfCurrentChildNode = ctx.getParent().children.indexOf(ctx);
		return indexOfCurrentChildNode <= 0 ? null : (ParserRuleContext) ctx.parent.getChild(indexOfCurrentChildNode - 1);
	}

	private static boolean acceptableParent(Object element, ParserRuleContext context) {
		return element instanceof ParserRuleContext && context != null && context.getParent() != null;
	}

	private static boolean in(ParserRuleContext context, Class<? extends ParserRuleContext> container) {
		ParserRuleContext parent = context.getParent();
		while (parent != null) {
			if (container.isInstance(parent)) return true;
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

	private static boolean is(ParserRuleContext context, int type) {
		return prevSibling(context) != null && prevSibling(context) != null && prevSibling(context).getToken(type, 0) != null;
	}
}
