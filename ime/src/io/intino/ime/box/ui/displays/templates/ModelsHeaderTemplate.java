package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.model.Language;

import java.util.function.Consumer;

public class ModelsHeaderTemplate extends AbstractModelsHeaderTemplate<ImeBox> {
	private String filters;
	private Language language;
	private Consumer<String> searchListener;

	public ModelsHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onSearch(Consumer<String> listener) {
		this.searchListener = listener;
	}

	@Override
	public void init() {
		super.init();
		searchField.onChange(e -> searchListener.accept(e.value()));
		searchField.onEnterPress(e -> searchListener.accept(e.value()));
		appViewSwitch.state(ToggleEvent.State.On);
		appViewSwitch.onToggle(this::redirectToLanguages);
	}

	@Override
	public void refresh() {
		super.refresh();
		login.visible(session().user() == null);
		dashboard.visible(user() != null);
		user.visible(session().user() != null);
		userHome.path(PathHelper.dashboardPath(session()));
	}

	private void redirectToLanguages(ToggleEvent event) {
		ViewMode viewMode = event.state() == ToggleEvent.State.On ? ViewMode.Models : ViewMode.Languages;
		DisplayHelper.updateViewMode(viewMode, session());
		if (viewMode == ViewMode.Languages) notifier.redirect(PathHelper.languagesUrl(session(), language));
		else notifier.redirect(PathHelper.modelsUrl(session(), language));
	}
}