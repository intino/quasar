package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.CollectionCommands;
import io.quassar.editor.box.commands.collection.RenewLicenseCommand;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.UserHelper;
import io.quassar.editor.model.License;

import java.util.function.Consumer;

public class RenewLicenseDialog extends AbstractRenewLicenseDialog<EditorBox> {
	private License license;
	private Consumer<License> renewListener;

	public RenewLicenseDialog(EditorBox box) {
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
		dialog.onOpen(e -> refreshDialog());
		renew.onExecute(e -> renew());
	}

	public void open() {
		renew();
//		dialog.open();
	}

	private void refreshDialog() {
		boolean hasCredit = PermissionsHelper.hasCredit(renewPeriod(), session(), box());
		renew.readonly(!hasCredit);
		message.value(message(hasCredit));
	}

	private String message(boolean hasCredit) {
		if (!hasCredit) return translate("You don't have enough credit to renew this license");
		int remainingPeriod = UserHelper.licenseTime(session(), box()) - renewPeriod();
		return translate("This license will be renewed for %s months. Your remaining credit will be: %s months").formatted(renewPeriod(), remainingPeriod);
	}

	private int renewPeriod() {
		return Integer.parseInt(box().configuration().renewLicenseTimeDuration());
	}

	private void renew() {
		RenewLicenseCommand.RenewResult result = box().commands(CollectionCommands.class).renew(license, renewPeriod(), username());
		if (!result.success()) {
			notifyUser(result.message(), UserMessage.Type.Error);
			return;
		}
		dialog.close();
		renewListener.accept(license);
	}

}