package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.CollectionCommands;
import io.quassar.editor.box.commands.collection.AssignLicenseCommand;
import io.quassar.editor.box.util.LicenseGenerator;
import io.quassar.editor.model.License;

import java.util.function.Consumer;

public class AddLicenseEditor extends AbstractAddLicenseEditor<EditorBox> {
	private Consumer<License> addListener;
	private AssignLicenseCommand.AssignResult assignResult;

	public AddLicenseEditor(EditorBox box) {
		super(box);
	}

	public void onAdd(Consumer<License> listener) {
		this.addListener = listener;
	}

	@Override
	public void init() {
		super.init();
		licenseField.onChange(e -> assign());
		licenseField.onEnterPress(e -> assign());
		failureDialog.onOpen(e -> refreshFailureDialog());
	}

	@Override
	public void refresh() {
		super.refresh();
		licenseField.value(null);
	}

	private void assign() {
		String value = licenseField.value();
		if (value == null || value.length() < LicenseGenerator.size()) return;
		licenseField.value(null);
		assignResult = box().commands(CollectionCommands.class).assignLicense(value, username());
		if (!assignResult.success()) failureDialog.open();
		else addListener.accept(assignResult.license());
	}

	private void refreshFailureDialog() {
		failureMessage.value(translate(assignResult.message()).formatted(assignResult.license() != null ? assignResult.license().collection().name() : ""));
	}

}