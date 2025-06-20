package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.collection.*;
import io.quassar.editor.model.*;

import java.util.List;

public class CollectionCommands extends Commands {

	public CollectionCommands(EditorBox box) {
		super(box);
	}

	public Collection create(String name, Collection.SubscriptionPlan plan, String username) {
		CreateCollectionCommand command = setup(new CreateCollectionCommand(box), username);
		command.name = name;
		command.plan = plan;
		return command.execute();
	}

	public List<License> addLicenses(Collection collection, double count, double duration, String username) {
		AddLicensesCommand command = setup(new AddLicensesCommand(box), username);
		command.collection = collection;
		command.count = Double.valueOf(count).intValue();
		command.duration = Double.valueOf(duration).intValue();
		return command.execute();
	}

	public License addLicense(Collection collection, int duration, String username) {
		AddLicenseCommand command = setup(new AddLicenseCommand(box), username);
		command.collection = collection;
		command.duration = Double.valueOf(duration).intValue();
		return command.execute();
	}

	public boolean revokeLicense(Collection collection, License license, String username) {
		RevokeLicenseCommand command = setup(new RevokeLicenseCommand(box), username);
		command.collection = collection;
		command.license = license;
		return command.execute();
	}

	public AssignLicenseCommand.AssignResult assignLicense(String license, String username) {
		AssignLicenseCommand command = setup(new AssignLicenseCommand(box), username);
		command.license = license;
		return command.execute();
	}

	public RenewLicenseCommand.RenewResult renew(License license, int duration, String username) {
		RenewLicenseCommand command = setup(new RenewLicenseCommand(box), username);
		command.license = license;
		command.duration = duration;
		return command.execute();
	}

	public void save(Collection collection, List<String> collaborators, String username) {
		SaveCollectionCollaboratorsCommand command = setup(new SaveCollectionCollaboratorsCommand(box), username);
		command.collection = collection;
		command.collaborators = collaborators;
		command.execute();
	}

	public boolean remove(Collection collection, String username) {
		RemoveCollectionCommand command = setup(new RemoveCollectionCommand(box), username);
		command.collection = collection;
		return command.execute();
	}

}
