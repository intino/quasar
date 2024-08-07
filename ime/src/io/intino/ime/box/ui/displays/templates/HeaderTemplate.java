package io.intino.ime.box.ui.displays.templates;

import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.util.WorkspaceHelper;

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

	@Override
	public void refresh() {
		super.refresh();
		notLoggedToolbar.visible(session().user() == null);
		user.visible(session().user() != null);
		if (session().user() == null) return;
		workspaces.path(PathHelper.workspacesPath(session()));
	}
}