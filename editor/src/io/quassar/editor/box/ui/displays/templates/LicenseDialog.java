package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.exceptions.*;
import io.quassar.editor.box.*;
import io.quassar.editor.box.schemas.*;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.displays.templates.AbstractLicenseDialog;
import io.quassar.editor.model.License;

import java.util.function.Consumer;

public class LicenseDialog extends AbstractLicenseDialog<EditorBox> {
	private License license;
	private Consumer<License> renewListener;

	public LicenseDialog(EditorBox box) {
		super(box);
	}

	public void license(License license) {
		this.license = license;
	}

	public void onRenew(Consumer<License> listener) {
		this.renewListener = listener;
	}

	public void open() {
		dialog.open();
	}

	@Override
	public void init() {
		super.init();
		dialog.onOpen(e -> refreshDialog());
		renewDialog.onRenew(e -> renewListener.accept(e));
		renew.onExecute(e -> openRenewDialog());
	}

	private void refreshDialog() {
		message.value(message());
		renew.visible(license != null);
	}

	private String message() {
		if (license == null) return "You don't have a license to use this language";
		if (license.isExpired()) return "Your license has expired. Please renew it to continue using this language";
		return null;
	}

	private void openRenewDialog() {
		dialog.close();
		renewDialog.license(license);
		renewDialog.open();
	}
}