package io.quassar.editor.box.ui.displays;

import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.schemas.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IntinoFileBrowser extends AbstractIntinoFileBrowser<EditorBox> {
	private List<IntinoFileBrowserItem> items;
	private boolean hideExtension;
	private List<IntinoFileBrowserOperation> operations;
	private Consumer<IntinoFileBrowserItem> openListener;
	private BiConsumer<String, IntinoFileBrowserItem> executeOperationListener;
	private BiConsumer<IntinoFileBrowserItem, String> renameListener;
	private BiConsumer<IntinoFileBrowserItem, IntinoFileBrowserItem> moveListener;
	private String itemsAddress;
	private IntinoFileBrowserItem selectedItem;
	private String rootItem = null;
	private boolean historyEnabled = true;

	public IntinoFileBrowser(EditorBox box) {
		super(box);
	}

	public void itemAddress(String itemAddress) {
		this.itemsAddress = itemAddress;
	}

	public void rootItem(String value) {
		this.rootItem = value;
	}

	public void items(List<IntinoFileBrowserItem> items, boolean hideExtension, boolean historyEnabled) {
		this.items = items;
		this.hideExtension = hideExtension;
		this.historyEnabled = historyEnabled;
	}

	public void operations(List<IntinoFileBrowserOperation> operations) {
		this.operations = operations;
	}

	public void openContextMenu(List<IntinoFileBrowserOperation> operations) {
		notifier.openContextMenu(operations);
	}

	public void onOpen(Consumer<IntinoFileBrowserItem> listener) {
		this.openListener = listener;
	}

	public void onExecuteOperation(BiConsumer<String, IntinoFileBrowserItem> listener) {
		this.executeOperationListener = listener;
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

	public void selection(IntinoFileBrowserItem item) {
		this.selectedItem = item;
	}

	public void select(IntinoFileBrowserItem item) {
		this.selectedItem = item;
		notifier.select(selectedItem);
	}

	public void executeOperation(IntinoFileBrowserOperationInfo info) {
		IntinoFileBrowserItem target = info.target() != -1 ? items.stream().filter(i -> i.id() == info.target()).findFirst().orElse(null) : null;
		executeOperationListener.accept(info.operation(), target);
	}

	public void rename(IntinoFileBrowserRenameInfo info) {
		IntinoFileBrowserItem item = items.stream().filter(i -> i.id() == info.id()).findFirst().orElse(null);
		if (item == null) return;
		renameListener.accept(item, info.newName());
	}

	public void move(IntinoFileBrowserMoveInfo info) {
		IntinoFileBrowserItem item = items.stream().filter(i -> i.id() == info.file()).findFirst().orElse(null);
		IntinoFileBrowserItem directory = items.stream().filter(i -> i.uri().equals(info.directory())).findFirst().orElse(null);
		if (item == null) return;
		moveListener.accept(item, directory);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (items == null) return;
		notifier.refresh(info());
		notifier.select(selectedItem);
	}

	private IntinoFileBrowserInfo info() {
		return new IntinoFileBrowserInfo().rootItem(rootItem).itemAddress(itemsAddress).items(withRoot(fix(items))).operations(operations).hideExtension(hideExtension).historyEnabled(historyEnabled);
	}

	private List<IntinoFileBrowserItem> fix(List<IntinoFileBrowserItem> items) {
		List<IntinoFileBrowserItem> result = new ArrayList<>(items.stream().sorted(Comparator.comparing(o -> o.uri().toLowerCase())).toList());
		for (int i=0; i<result.size(); i++) fix(result.get(i), i);
		return result;
	}

	private static void fix(IntinoFileBrowserItem item, int index) {
		List<String> parents = new ArrayList<>();
		for (int i = 0; i < item.parents().size(); i++) parents.add(String.join("/", item.parents().subList(0, i + 1)));
		item.id(index);
		item.parents(parents);
	}

	private List<IntinoFileBrowserItem> withRoot(List<IntinoFileBrowserItem> items) {
		if (rootItem != null) return items;
		List<IntinoFileBrowserItem> children = items.stream().filter(IntinoFileBrowserItem::isRoot).toList();
		items.addFirst(rootItem(children).id(items.size()));
		return items;
	}

	private IntinoFileBrowserItem rootItem(List<IntinoFileBrowserItem> children) {
		return new IntinoFileBrowserItem().name("root").uri("root").type(IntinoFileBrowserItem.Type.Folder).children(children.stream().map(IntinoFileBrowserItem::name).toList());
	}

}