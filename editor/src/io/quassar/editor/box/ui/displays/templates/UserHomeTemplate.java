package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.Formatters;
import io.quassar.editor.box.util.UserHelper;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.License;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserHomeTemplate extends AbstractUserHomeTemplate<EditorBox> {

	public UserHomeTemplate(EditorBox box) {
		super(box);
	}

	@Override
	public void init() {
		super.init();
		addLicenseEditor.onAdd(e -> notifier.redirect());
		renewLicenseDialog.onRenew(e -> refreshLicenses());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshLicenses();
		refreshCollections();
	}

	private void refreshLicenses() {
		licenses.clear();
		Map<String, List<License>> licenseMap = box().collectionManager().anyLicenses(username()).stream().collect(Collectors.groupingBy(l -> l.collection().name()));
		List<License> licenseList = licenseMap.values().stream().map(l -> l.stream().filter(l1 -> !l1.isExpired()).findFirst().orElse(l.getFirst())).toList();
		licenseList.forEach(l -> fill(l, licenses.add()));
	}

	private void refreshCollections() {
		List<Collection> collectionList = box().collectionManager().collections(username());
		myCollectionsBlock.visible(!collectionList.isEmpty());
		if (!myCollectionsBlock.isVisible()) return;
		collections.clear();
		collectionList.forEach(c -> fill(c, collections.add()));
	}

	private void fill(License license, LicenseItemTemplate display) {
		display.license(license);
		display.onRenew(this::openRenewDialog);
		display.refresh();
	}

	private void fill(Collection collection, CollectionItemTemplate display) {
		display.collection(collection);
		display.refresh();
	}

	private void openRenewDialog(License license) {
		renewLicenseDialog.license(license);
		renewLicenseDialog.open();
	}

}