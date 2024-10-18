package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.DisplayRouteDispatcher;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.intino.alexandria.ui.displays.events.KeyPressEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;

import java.util.function.Consumer;

public class SearchHeaderTemplate extends AbstractSearchHeaderTemplate<ImeBox> {
	private Consumer<String> searchListener;
	private String condition;

	public SearchHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void onSearch(Consumer<String> listener) {
		this.searchListener = listener;
	}

	public void condition(String condition) {
		this.condition = condition;
	}

	@Override
	public void init() {
		super.init();
		header.view(HeaderTemplate.View.Search);
		searchField.onChange(this::search);
		searchField.onEnterPress(this::search);
	}

	@Override
	public void refresh() {
		super.refresh();
		header.refresh();
		searchField.value(condition);
		showMain.readonly(condition == null);
	}

	private void search(ChangeEvent e) {
		search((String) e.value());
	}

	private void search(KeyPressEvent e) {
		search((String) e.value());
	}

	private void search(String condition) {
		searchDispatcher.address(path -> PathHelper.searchPath(condition));
		searchDispatcher.launch();
	}

}