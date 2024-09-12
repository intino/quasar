package io.intino.ls.codeinsight.completion;

import org.antlr.v4.runtime.ParserRuleContext;

public interface ElementFilter {
	ElementFilter[] EMPTY_ARRAY = new ElementFilter[0];

	boolean isAcceptable(Object element, ParserRuleContext context);

	/**
	 * Quick check if the filter is acceptable for elements of the given class at all.
	 *
	 * @param hintClass class for which we are looking for metadata
	 * @return true if class matched
	 */
	boolean isClassAcceptable(Class<?> hintClass);

	// To be used only for debug purposes!
	String toString();
}
