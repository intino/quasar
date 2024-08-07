package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Workspace;

public class PermissionsTemplate extends AbstractPermissionsTemplate<ImeBox> {
	private Workspace workspace;

	public PermissionsTemplate(ImeBox box) {
		super(box);
	}

	public void workspace(String name) {
		this.workspace = box().workspaceManager().workspace(name);
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
		message.value(String.format(translate("You dont have access permissions for %s"), workspace.title()));
		gotoHome.visible(session().user() == null);
		logoutButton.visible(session().user() != null);
	}
}