package io.intino.ime.box.ui.displays;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.schemas.IntinoFileBrowserInfo;
import io.intino.ime.box.schemas.IntinoFileBrowserItem;
import io.intino.ime.box.schemas.IntinoFileBrowserMoveInfo;
import io.intino.ime.box.schemas.IntinoFileBrowserRenameInfo;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IntinoFileBrowser extends AbstractIntinoFileBrowser<ImeBox> {
	private List<IntinoFileBrowserItem> items;
	private Consumer<String> openListener;
	private BiConsumer<IntinoFileBrowserItem, String> renameListener;
	private BiConsumer<IntinoFileBrowserItem, IntinoFileBrowserItem> moveListener;
	private String itemsAddress;
	private IntinoFileBrowserItem selectedItem;

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

	public void onRename(BiConsumer<IntinoFileBrowserItem, String> listener) {
		this.renameListener = listener;
	}

	public void onMove(BiConsumer<IntinoFileBrowserItem, IntinoFileBrowserItem> listener) {
		this.moveListener = listener;
	}

	public void open(String item) {
		openListener.accept(item);
	}

	public void select(IntinoFileBrowserItem item) {
		this.selectedItem = item;
	}

	public void rename(IntinoFileBrowserRenameInfo info) {
		IntinoFileBrowserItem item = items.stream().filter(i -> i.id() == info.id()).findFirst().orElse(null);
		if (item == null) return;
		renameListener.accept(item, info.newName());
	}

	public void move(IntinoFileBrowserMoveInfo info) {
		IntinoFileBrowserItem item = items.stream().filter(i -> i.id() == info.file()).findFirst().orElse(null);
		IntinoFileBrowserItem directory = items.stream().filter(i -> i.name().equals(info.directory())).findFirst().orElse(null);
		if (item == null || directory == null) return;
		moveListener.accept(item, directory);
	}

	@Override
	public void refresh() {
		super.refresh();
		notifier.refresh(info());
		notifier.select(selectedItem);
	}

	private IntinoFileBrowserInfo info() {
		return new IntinoFileBrowserInfo().itemAddress(itemsAddress).items(withRoot(items));
	}

	private List<IntinoFileBrowserItem> withRoot(List<IntinoFileBrowserItem> items) {
		List<IntinoFileBrowserItem> children = items.stream().filter(IntinoFileBrowserItem::isRoot).toList();
		items.addFirst(rootItem(children).id(items.size()));
		return items;
	}

	private IntinoFileBrowserItem rootItem(List<IntinoFileBrowserItem> children) {
		return new IntinoFileBrowserItem().name("root").type(IntinoFileBrowserItem.Type.Folder).children(children.stream().map(IntinoFileBrowserItem::name).toList());
	}

}