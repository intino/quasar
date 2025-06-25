package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.GavCoordinates;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.License;

import java.util.Set;

public class LicenseExpiredBanner extends AbstractLicenseExpiredBanner<EditorBox> {
	private Language language;
	private String hint;

	public LicenseExpiredBanner(EditorBox box) {
		super(box);
	}

	public void language(GavCoordinates language) {
		this.language = box().languageManager().get(language);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void hint(String value) {
		this.hint = value;
	}

	@Override
	public void init() {
		super.init();
		addLicenseEditor.onAdd(e -> reload());
		renewLicense.onExecute(e -> openRenewDialog());
		renewLicenseDialog.onRenew(e -> notifier.redirect());
	}

	@Override
	public void refresh() {
		super.refresh();
		content.visible(user() != null && !PermissionsHelper.hasValidLicense(language, session(), box()));
		if (!content.isVisible()) return;
		License license = license();
		bullet.formats(Set.of(license == null || license.isExpired() ? "inactiveBullet" : "activeBullet"));
		title.value(license != null ? license.code() + " - " + license.collection().name() : "License required");
		expirationInfo.value(DisplayHelper.expirationInfo(license, this::translate, language()));
		expirationMessage.value(hint);
		renewLicense.visible(license != null);
		addLicenseBlock.visible(license == null);
	}

	private void openRenewDialog() {
		renewLicenseDialog.license(license());
		renewLicenseDialog.open();
	}

	private License license() {
		return box().collectionManager().anyLicense(language.collection(), username());
	}

	private void reload() {
		notifier.redirect();
	}

}