package io.intino.ls.codeinsight;

import io.intino.ls.ModelUnit;
import io.intino.tara.model.Element;
import io.intino.tara.model.ElementContainer;
import io.intino.tara.model.MogramReference;
import org.eclipse.lsp4j.Position;

import java.net.URI;
import java.util.Map;

public class ReferenceResolver {
	private final Map<URI, ModelUnit> models;

	public ReferenceResolver(Map<URI, ModelUnit> models) {
		this.models = models;
	}

	public Element resolveToDeclaration(URI referenceUri, Position referencePosition) {
		ModelUnit modelUnit = models.get(referenceUri);
		if (modelUnit == null) return null;
		Element rerefenceElement = findMogramContainingToken(modelUnit.model(), referencePosition);
		if (rerefenceElement == null) return null;
		if (rerefenceElement instanceof MogramReference m) {
			if (m.target().resolved()) return m.target().get();
		}
		return null;
	}


	public static Element findMogramContainingToken(Element element, Position position) {
		if (isInRange(element, position)) {
			if (element instanceof ElementContainer ec)
				for (Element e : ec.elements()) {
					Element result = findMogramContainingToken(e, position);
					if (result != null) return result;
				}
			return element;
		}
		return null;
	}

	private static boolean isInRange(Element mogram, Position position) {
		return mogram != null && mogram.textRange().startLine() <= position.getLine() && position.getLine() <= mogram.textRange().endLine();
	}
}