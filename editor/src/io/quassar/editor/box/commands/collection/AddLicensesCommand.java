package io.quassar.editor.box.commands.collection;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.Command;
import io.quassar.editor.box.util.LicenseGenerator;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.UserHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.License;
import io.quassar.editor.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class AddLicensesCommand extends Command<List<License>> {
	public Collection collection;
	public int count;
	public int duration;

	public AddLicensesCommand(EditorBox box) {
		super(box);
	}

	@Override
	public List<License> execute() {
		if (!hasCredit()) return Collections.emptyList();
		List<License> licenses = IntStream.range(0, count).mapToObj(i -> box.collectionManager().createLicense(collection, LicenseGenerator.generate(), duration)).toList();
		consumeCredit();
		return licenses;
	}

	private boolean hasCredit() {
		if (collection.subscriptionPlan() == Collection.SubscriptionPlan.Enterprise) return PermissionsHelper.hasCredit(duration, collection, box);
		return PermissionsHelper.hasCredit(count*duration, author, box);
	}

	private void consumeCredit() {
		if (collection.subscriptionPlan() == Collection.SubscriptionPlan.Enterprise) return;
		User user = UserHelper.user(author, box);
		int licenseTime = UserHelper.licenseTime(author, box);
		user.licenseTime(licenseTime - count*duration);
	}

}
