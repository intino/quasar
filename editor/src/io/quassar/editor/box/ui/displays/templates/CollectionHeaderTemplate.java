package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Collection;

public class CollectionHeaderTemplate extends AbstractCollectionHeaderTemplate<EditorBox> {
	private Collection collection;

	public CollectionHeaderTemplate(EditorBox box) {
		super(box);
	}

	public void collection(Collection collection) {
		this.collection = collection;
	}

	@Override
	public void refresh() {
		super.refresh();
		title.value(collection.name());
	}
}