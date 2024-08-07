package io.intino.ime.box.ui.displays;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.schemas.IntinoFileBrowserInfo;
import io.intino.ime.box.schemas.IntinoFileBrowserItem;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.workspaces.WorkspaceContainer;

import java.util.List;
import java.util.function.Consumer;

public class IntinoFileBrowser extends AbstractIntinoFileBrowser<ImeBox> {
	private List<IntinoFileBrowserItem> items;
	private Consumer<String> openListener;
	private String itemsAddress;

	public IntinoFileBrowser(ImeBox box) {
		super(box);
	}

	public void itemAddress(String itemAddress) {
		this.itemsAddress = itemAddress;
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

	public void select(IntinoFileBrowserItem item) {
		notifier.select(item);
	}

	@Override
	public void refresh() {
		super.refresh();
		notifier.refresh(info());
	}

	private IntinoFileBrowserInfo info() {
		return new IntinoFileBrowserInfo().itemAddress(itemsAddress).items(withRoot(items));
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