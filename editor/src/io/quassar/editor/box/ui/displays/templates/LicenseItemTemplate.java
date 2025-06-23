package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.License;

import java.util.Set;
import java.util.function.Consumer;

public class LicenseItemTemplate extends AbstractLicenseItemTemplate<EditorBox> {
	private License license;
	private Consumer<License> renewListener;

	public LicenseItemTemplate(EditorBox box) {
		super(box);
	}

	public void license(License license) {
		this.license = license;
	}

	public void onRenew(Consumer<License> listener) {
		this.renewListener = listener;
	}

	@Override
	public void init() {
		super.init();
		renew.onExecute(e -> renewListener.accept(license));
	}

	@Override
	public void refresh() {
		super.refresh();
		bullet.formats(Set.of(license.isExpired() ? "inactiveBullet" : "activeBullet"));
		title.value(license.code() + " - " + license.collection().name());
		renew.visible(renewListener != null && PermissionsHelper.canRenew(license, session(), box()));
		expirationInfo.value(DisplayHelper.expirationInfo(license, this::translate, language()));
	}

}