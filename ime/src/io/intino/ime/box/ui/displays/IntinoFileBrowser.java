package io.intino.ime.box.ui.displays;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.schemas.IntinoFileBrowserItem;

import java.util.List;
import java.util.function.Consumer;

public class IntinoFileBrowser extends AbstractIntinoFileBrowser<ImeBox> {
	private List<IntinoFileBrowserItem> items;
	private Consumer<String> openListener;

	public IntinoFileBrowser(ImeBox box) {
		super(box);
	}

	public void items(List<IntinoFileBrowserItem> items) {
		this.items = items;
	}

	public void onOpen(Consumer<String> listener) {
		this.openListener = listener;
	}

	public void open(String item) {
		openListener.accept(item);
	}

	@Override
	public void refresh() {
		super.refresh();
		notifier.refresh(withRoot(items));
	}

	private List<IntinoFileBrowserItem> withRoot(List<IntinoFileBrowserItem> items) {
		List<IntinoFileBrowserItem> children = items.stream().filter(IntinoFileBrowserItem::isRoot).toList();
		items.addFirst(rootItem(children));
		return items;
	}

	private IntinoFileBrowserItem rootItem(List<IntinoFileBrowserItem> children) {
		return new IntinoFileBrowserItem().name("root").type(IntinoFileBrowserItem.Type.Folder).children(children.stream().map(IntinoFileBrowserItem::name).toList());
	}

}