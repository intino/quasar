package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.model.Model;

public class NotFoundTemplate extends AbstractNotFoundTemplate<ImeBox> {
	private Model model;

	public NotFoundTemplate(ImeBox box) {
		super(box);
	}

	public void model(String name) {
		this.model = box().modelManager().model(name);
	}

	@Override
	public void refresh() {
		super.refresh();
		message.value(translate("Model was not found"));
		userHome.visible(session().user() != null);
		userHome.path(PathHelper.dashboardPath(session()));
	}

}