package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.Collection;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.PermissionsHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.Collections;
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
		mostRecentLink.onExecute(e -> updateSorting("most recent"));
		lastModifiedLink.onExecute(e -> updateSorting("last modified"));
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshSorting("most recent");
		searchBox.condition(null);
		addModelTrigger.visible(addModelListener != null && PermissionsHelper.canAddModel(language, session(), box()));
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
		addModelListener.accept(true);
	}

	private void updateSorting(String sorting) {
		collection.sortings(List.of(sorting));
		refreshSorting(sorting);
	}

	private void refreshSorting(String sorting) {
		mostRecentLink.visible(!sorting.equals("most recent"));
		mostRecentText.visible(sorting.equals("most recent"));
		lastModifiedLink.visible(!sorting.equals("last modified"));
		lastModifiedText.visible(sorting.equals("last modified"));
	}

}