package io.quassar.editor.box.ui.displays.templates;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.model.Collection;

public class CollectionItemTemplate extends AbstractCollectionItemTemplate<EditorBox> {
	private Collection collection;

	public CollectionItemTemplate(EditorBox box) {
		super(box);
	}

	public void collection(io.quassar.editor.model.Collection collection) {
		this.collection = collection;
	}

	@Override
	public void refresh() {
		super.refresh();
		collectionLink.address(path -> PathHelper.collectionPath(path, collection));
		collectionLink.title(collection.name());
	}
}