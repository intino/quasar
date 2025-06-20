package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.UserHelper;
import io.quassar.editor.model.License;
import io.quassar.editor.model.User;

import java.time.Instant;

import static io.quassar.editor.box.commands.collection.AssignLicenseCommand.AssignResult;
import static io.quassar.editor.box.commands.collection.RenewLicenseCommand.*;

public class RenewLicenseCommand extends Command<RenewResult> {
	public License license;
	public int duration;

	public RenewLicenseCommand(EditorBox box) {
		super(box);
	}

	public record RenewResult(boolean success, String message, License license) {}

	@Override
	public RenewResult execute() {
		if (!PermissionsHelper.hasCredit(duration, author, box)) return new RenewResult(false, "You don't have enough credit to renew this license", null);
		if (license.status() == License.Status.Revoked) return new RenewResult(false, "This license have been revoked. Contact collection owner", null);
		if (license.status() != License.Status.Assigned) return new RenewResult(false, "This license is not assigned yet", null);
		license.assignDate(Instant.now());
		license.duration(duration);
		User user = UserHelper.user(author, box);
		int licenseTime = UserHelper.licenseTime(author, box);
		user.licenseTime(licenseTime - duration);
		return new RenewResult(true, null, license);
	}

}
