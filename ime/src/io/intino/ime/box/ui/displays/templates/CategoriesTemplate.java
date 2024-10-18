package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.exceptions.*;
import io.intino.ime.box.*;
import io.intino.ime.box.schemas.*;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.displays.templates.AbstractCategoriesTemplate;

public class CategoriesTemplate extends AbstractCategoriesTemplate<ImeBox> {
	private String type;

	public CategoriesTemplate(ImeBox box) {
		super(box);
	}

	public void type(String type) {
		this.type = type;
	}
}