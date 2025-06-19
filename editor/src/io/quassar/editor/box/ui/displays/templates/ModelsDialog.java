package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.Collection;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.ui.datasources.ModelsDatasource;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModelsDialog extends AbstractModelsDialog<EditorBox> {
	private Consumer<Boolean> addModelListener;
	private Language language;
	private Function<Language, Long> countItemsProvider = null;
	private Collection<?, ?> collection;

	public ModelsDialog(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void countItemsProvider(Function<Language, Long> provider) {
		this.countItemsProvider = provider;
	}

	@Override
	public void init() {
		super.init();
		addModelTrigger.onExecute(e -> notifyAddModel());
		mostRecentLink.onExecute(e -> updateSorting(ModelsDatasource.Sorting.MostRecent));
		lastModifiedLink.onExecute(e -> updateSorting(ModelsDatasource.Sorting.LastModified));
		licenseDialog.onRenew(e -> notifyAddModel());
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshSorting(SessionHelper.modelsSorting(session()));
		searchBox.condition("");
		addModelTrigger.visible(addModelListener != null && PermissionsHelper.canAddModel(language, session(), box()));
		if (addModelTrigger.isVisible()) addModelTrigger.readonly(!PermissionsHelper.hasValidLicense(language, session(), box()));
		catalogOperations.visible(countItemsProvider.apply(language) > DisplayHelper.MinItemsCount);
	}

	public void onAddModel(Consumer<Boolean> listener) {
		this.addModelListener = listener;
	}

	public void bindTo(Collection<?, ?> collection) {
		this.collection = collection;
		searchBox.bindTo(collection);
	}

	private void notifyAddModel() {
		if (!PermissionsHelper.hasValidLicense(language, session(), box())) {
			openLicenseDialog();
			return;
		}
		addModelListener.accept(true);
	}

	private void openLicenseDialog() {
		licenseDialog.license(box().collectionManager().anyLicense(language.collection(), username()));
		licenseDialog.open();
	}

	private void updateSorting(ModelsDatasource.Sorting sorting) {
		SessionHelper.registerModelsSorting(session(), sorting);
		collection.sortings(List.of(sorting.name()));
		refreshSorting(sorting);
	}

	private void refreshSorting(ModelsDatasource.Sorting sorting) {
		mostRecentLink.visible(sorting != ModelsDatasource.Sorting.MostRecent);
		mostRecentText.visible(sorting == ModelsDatasource.Sorting.MostRecent);
		lastModifiedLink.visible(sorting != ModelsDatasource.Sorting.LastModified);
		lastModifiedText.visible(sorting == ModelsDatasource.Sorting.LastModified);
	}

}