package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.services.push.User;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.nio.file.Path;

public class HeaderTemplate extends AbstractHeaderTemplate<EditorBox> {
	private Language language;
	private String release;
	private Model model;
	private boolean refreshRequired = true;

	public HeaderTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language value) {
		refreshRequired = refreshRequired || (this.language == null && value != null) || (value != null && !this.language.key().equals(value.key()));
		this.language = value;
	}

	public void release(String value) {
		refreshRequired = refreshRequired || (this.release == null && value != null) || (value != null && !this.release.equals(value));
		this.release = value;
	}

	public void model(Model value) {
		refreshRequired = refreshRequired || (this.model == null && value != null) || (value != null && !this.model.id().equals(value.id()));
		this.model = value;
	}

	@Override
	public void init() {
		super.init();
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			if (box().authService() == null) notifier.redirect(PathHelper.loginUrl(session()));
			else notifier.redirect(session().login(session().browser().baseUrl()));
		});
		user.onRefresh(e -> refreshUser());
		languageLink.onExecute(e -> gotoMetamodel());
	}

	@Override
	public void refresh() {
		super.refresh();
		if (!refreshRequired) return;
		User loggedUser = session().user();
		login.visible(loggedUser == null);
		user.visible(loggedUser != null);
		refreshLanguageBlock();
		refreshRequired = false;
	}

	private void refreshLanguageBlock() {
		languageBlock.visible(language != null);
		if (!languageBlock.isVisible()) return;
		languageLink.title(LanguageHelper.label(language, this::translate));
		Model metamodel = !language.isFoundational() ? box().modelManager().get(language.metamodel()) : null;
		languageLink.readonly(metamodel == null || !PermissionsHelper.hasPermissions(metamodel, session(), box()));
	}

	private void refreshUser() {
		userHomeStamp.refresh();
	}

	private void gotoMetamodel() {
		if (language.isFoundational()) return;
		Model metamodel = box().modelManager().get(language.metamodel());
		notifier.redirect(PathHelper.modelUrl(metamodel, session()));
	}

}