package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.ModelHelper;
import io.intino.ime.model.Model;

public class PermissionsTemplate extends AbstractPermissionsTemplate<ImeBox> {
	private String username;
	private Model model;
	private String callback;

	public PermissionsTemplate(ImeBox box) {
		super(box);
	}

	public void username(String name) {
		this.username = name;
	}

	public void model(String name) {
		this.model = box().modelManager().model(name);
	}

	public void callback(String value) {
		this.callback = value;
	}

	@Override
	public void init() {
		super.init();
		loginButton.onExecute(e -> {
			session().add("callback", callback);
			notifier.redirect(session().login(session().browser().baseUrl()));
		});
		logoutButton.onExecute(e -> {
			session().logout();
			notifier.redirect(session().browser().baseUrl());
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		message.value(String.format(translate("You dont have access permissions for %s"), ModelHelper.label(model, language(), box())));
		userHome.visible(session().user() != null);
		userHome.path(PathHelper.dashboardPath(session()));
		logoutButton.visible(session().user() != null);
		loginButton.visible(session().user() == null);
	}
}