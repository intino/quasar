package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;

import java.util.function.Consumer;

public class HeaderTemplate extends AbstractHeaderTemplate<ImeBox> {
	private Consumer<String> searchListener;

	public HeaderTemplate(ImeBox box) {
		super(box);
	}

	public void onSearch(Consumer<String> listener) {
		this.searchListener = listener;
	}

	@Override
	public void init() {
		super.init();
		searchField.onChange(e -> searchListener.accept(e.value()));
		searchField.onEnterPress(e -> searchListener.accept(e.value()));
	}
}