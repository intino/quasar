package io.quassar.editor.box.ui.displays;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.schemas.IntinoFileBrowserInfo;
import io.quassar.editor.box.schemas.IntinoFileBrowserItem;
import io.quassar.editor.box.schemas.IntinoFileBrowserMoveInfo;
import io.quassar.editor.box.schemas.IntinoFileBrowserRenameInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IntinoFileBrowser extends AbstractIntinoFileBrowser<EditorBox> {
	private List<IntinoFileBrowserItem> items;
	private Consumer<IntinoFileBrowserItem> openListener;
	private BiConsumer<IntinoFileBrowserItem, String> renameListener;
	private BiConsumer<IntinoFileBrowserItem, IntinoFileBrowserItem> moveListener;
	private String itemsAddress;
	private IntinoFileBrowserItem selectedItem;

	public IntinoFileBrowser(EditorBox box) {
		super(box);
	}

	public void itemAddress(String itemAddress) {
		this.itemsAddress = itemAddress;
	}

	public void items(List<IntinoFileBrowserItem> items) {
		this.items = items;
	}

	public void onOpen(Consumer<IntinoFileBrowserItem> listener) {
		this.openListener = listener;
	}

	public void onRename(BiConsumer<IntinoFileBrowserItem, String> listener) {
		this.renameListener = listener;
	}

	public void onMove(BiConsumer<IntinoFileBrowserItem, IntinoFileBrowserItem> listener) {
		this.moveListener = listener;
	}

	public void open(String item) {
		openListener.accept(items.stream().filter(i -> i.uri().equals(item)).findFirst().orElse(null));
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
		if (items == null) return;
		notifier.refresh(info());
		if (selectedItem != null) notifier.select(selectedItem);
	}

	private IntinoFileBrowserInfo info() {
		return new IntinoFileBrowserInfo().itemAddress(itemsAddress).items(withRoot(fix(items)));
	}

	private List<IntinoFileBrowserItem> fix(List<IntinoFileBrowserItem> items) {
		items.forEach(IntinoFileBrowser::fix);
		return items;
	}

	private static void fix(IntinoFileBrowserItem item) {
		List<String> parents = new ArrayList<>();
		for (int i = 0; i < item.parents().size(); i++) parents.add(String.join("/", item.parents().subList(0, i + 1)));
		item.parents(parents);
	}

	private List<IntinoFileBrowserItem> withRoot(List<IntinoFileBrowserItem> items) {
		List<IntinoFileBrowserItem> children = items.stream().filter(IntinoFileBrowserItem::isRoot).toList();
		items.addFirst(rootItem(children).id(items.size()));
		return items;
	}

	private IntinoFileBrowserItem rootItem(List<IntinoFileBrowserItem> children) {
		return new IntinoFileBrowserItem().name("root").uri("root").type(IntinoFileBrowserItem.Type.Folder).children(children.stream().map(IntinoFileBrowserItem::name).toList());
	}

}