package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.model.Model;

public class NotFoundTemplate extends AbstractNotFoundTemplate<ImeBox> {
	private String type;

	public NotFoundTemplate(ImeBox box) {
		super(box);
	}

	public void type(String type) {
		this.type = type;
	}

	@Override
	public void refresh() {
		super.refresh();
		message.value(String.format(translate("%s was not found"), type));
		userHome.visible(session().user() != null);
		userHome.path(PathHelper.dashboardPath(session()));
	}

}