package io.intino.ls;

import io.intino.tara.language.model.Element;
import io.intino.tara.language.model.ElementContainer;
import io.intino.tara.language.model.MogramReference;
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


	public Element findMogramContainingToken(Element element, Position position) {
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

	private boolean isInRange(Element mogram, Position position) {
		return mogram.textRange().startLine() <= position.getLine() && position.getLine() <= mogram.textRange().endLine();
	}
}
