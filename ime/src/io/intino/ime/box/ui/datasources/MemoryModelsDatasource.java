package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Model;

import java.util.List;

public class MemoryModelsDatasource extends ModelsDatasource {
	private final List<Model> models;

	public MemoryModelsDatasource(ImeBox box, UISession session, List<Model> models) {
		super(box, session, null);
		this.models = models;
	}

	@Override
	protected List<Model> load() {
		return models;
	}
}
