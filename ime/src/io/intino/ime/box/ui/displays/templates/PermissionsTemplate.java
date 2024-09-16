package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.model.Model;

public class PermissionsTemplate extends AbstractPermissionsTemplate<ImeBox> {
	private String username;
	private Model model;

	public PermissionsTemplate(ImeBox box) {
		super(box);
	}

	public void username(String name) {
		this.username = name;
	}

	public void model(String name) {
		this.model = box().modelManager().model(name);
	}

	@Override
	public void init() {
		super.init();
		logoutButton.onExecute(e -> {
			session().logout();
			notifier.redirect(session().browser().baseUrl());
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		message.value(String.format(translate("You dont have access permissions for %s"), model.title()));
		myModels.visible(session().user() != null);
		myModels.path(PathHelper.modelsPath(session()));
		logoutButton.visible(session().user() != null);
		//loginButton.visible(session().user() == null);
		//loginButton.path(PathHelper.modelPath(username, model));
	}
}