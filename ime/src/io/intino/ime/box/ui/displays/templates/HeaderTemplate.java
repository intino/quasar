package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.model.Language;

import java.util.function.Consumer;

public class HeaderTemplate extends AbstractHeaderTemplate<ImeBox> {
	private Consumer<String> searchListener;
	private Language language;
	private Consumer<ViewMode> changeViewListener;
	private boolean showDashboardButton = true;

	public HeaderTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void showDashboardButton(boolean value) {
		this.showDashboardButton = value;
	}

	public void onSearch(Consumer<String> listener) {
		this.searchListener = listener;
	}

	public void onChangeView(Consumer<ViewMode> listener) {
		this.changeViewListener = listener;
	}

	@Override
	public void init() {
		super.init();
		searchField.onChange(e -> searchListener.accept(e.value()));
		searchField.onEnterPress(e -> searchListener.accept(e.value()));
		appViewSwitch.onToggle(this::changeView);
		appViewSwitch.state(ToggleEvent.State.Off);
	}

	@Override
	public void refresh() {
		super.refresh();
		ViewMode viewMode = DisplayHelper.viewMode(session());
		appViewSwitch.state(viewMode == ViewMode.Languages ? ToggleEvent.State.Off : ToggleEvent.State.On);
		appViewText.value(translate(viewMode == ViewMode.Languages ? "Languages" : "Models"));
		login.visible(session().user() == null);
		dashboard.visible(showDashboardButton && user() != null);
		user.visible(session().user() != null);
		if (session().user() == null) return;
		userHome.visible(showDashboardButton);
		userHome.path(PathHelper.dashboardPath(session()));
	}

	private void changeView(ToggleEvent event) {
		ViewMode viewMode = event.state() == ToggleEvent.State.On ? ViewMode.Models : ViewMode.Languages;
		DisplayHelper.updateViewMode(viewMode, session());
		if (changeViewListener != null) {
			changeViewListener.accept(viewMode);
			refresh();
			return;
		}
		if (viewMode == ViewMode.Languages) notifier.redirect(PathHelper.languagesUrl(session(), language));
		else notifier.redirect(PathHelper.modelsUrl(session(), language));
	}
}