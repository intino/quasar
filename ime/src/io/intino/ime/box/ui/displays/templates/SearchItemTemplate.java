package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.model.SearchItem;

import java.util.function.Consumer;

public class SearchItemTemplate extends AbstractSearchItemTemplate<ImeBox> {
	private SearchItem item;
	private Consumer<SearchItem> selectListener;

	public SearchItemTemplate(ImeBox box) {
		super(box);
	}

	public void item(SearchItem value) {
		this.item = value;
	}

	public void onSelect(Consumer<SearchItem> listener) {
		this.selectListener = listener;
	}

	@Override
	public void init() {
		super.init();
		name.onExecute(e -> selectListener.accept(item));
	}

	@Override
	public void refresh() {
		super.refresh();
		name.title(item.name());
		count.value(item.languageList().size());
	}

}