package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

public class PermissionsTemplate extends AbstractPermissionsTemplate<EditorBox> {
	private String username;
	private Language language;
	private Model model;
	private String callback;

	public PermissionsTemplate(EditorBox box) {
		super(box);
	}

	public void username(String name) {
		this.username = name;
	}

	public void language(String id) {
		if (id == null || id.isEmpty()) return;
		this.language = box().languageManager().get(id);
	}

	public void model(String name) {
		if (name == null || name.isEmpty()) return;
		this.model = box().modelManager().get(name);
	}

	public void callback(String value) {
		this.callback = value;
	}

	@Override
	public void init() {
		super.init();
		loginButton.onExecute(e -> {
			session().add("callback", callback);
			if (box().authService() == null) notifier.redirect(PathHelper.loginUrl(session()));
			else notifier.redirect(session().login(session().browser().baseUrl()));
		});
		logoutButton.onExecute(e -> {
			session().logout();
			notifier.redirect(session().browser().baseUrl());
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		message.value(String.format(translate("You dont have access permissions for %s"), model != null ? ModelHelper.label(model, language(), box()) : language.name()));
		logoutButton.visible(session().user() != null);
		loginButton.visible(session().user() == null);
	}

}