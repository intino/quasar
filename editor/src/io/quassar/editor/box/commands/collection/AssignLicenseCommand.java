package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.model.License;

import java.time.Instant;

import static io.quassar.editor.box.commands.collection.AssignLicenseCommand.*;

public class AssignLicenseCommand extends Command<AssignResult> {
	public String license;

	public AssignLicenseCommand(EditorBox box) {
		super(box);
	}

	public record AssignResult(boolean success, String message, License license) {}

	@Override
	public AssignResult execute() {
		License license = box.collectionManager().getLicense(this.license);
		if (license == null) return new AssignResult(false, "No license was found with the provided code", null);
		if (license.status() == License.Status.Assigned) return new AssignResult(false, "This license code has already been used", null);
		License currentLicense = license.collection().validLicense(author);
		if (currentLicense != null) return new AssignResult(false, "You already have an active license for collection %s", currentLicense);
		license.status(License.Status.Assigned);
		license.assignDate(Instant.now());
		license.user(author);
		return new AssignResult(true, null, license);
	}

}
