package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.commands.UserCommands;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.model.Language;

import java.util.Set;
import java.util.function.Consumer;

public class HeaderTemplate extends AbstractHeaderTemplate<ImeBox> {
	private Consumer<Boolean> openSearchListener;
	private Language language;
	private Consumer<ViewMode> changeViewListener;
	private View view = View.Normal;
	private boolean canEditViewMode = true;

	public HeaderTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public enum View { Home, Documentation, Search, Dashboard, Normal }
	public void view(View view) {
		this.view = view;
	}

	public void canEditViewMode(boolean value) {
		canEditViewMode = value;
	}

	public void onOpenSearch(Consumer<Boolean> listener) {
		this.openSearchListener = listener;
	}

	public void onChangeView(Consumer<ViewMode> listener) {
		this.changeViewListener = listener;
	}

	@Override
	public void init() {
		super.init();
		openSearch.onExecute(e -> notifyOpenSearch());
		appViewSwitch.onToggle(this::changeView);
		appViewSwitch.state(ToggleEvent.State.Off);
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			notifier.redirect(session().login(session().browser().baseUrl()));
		});
		settingsDialog.onOpen(e -> refreshSettingsDialog());
		saveSettings.onExecute(e -> saveSettings());
	}

	@Override
	public void refresh() {
		super.refresh();
		ViewMode viewMode = DisplayHelper.viewMode(session());
		User loggedUser = session().user();
		openSearch.visible(view != View.Search);
		openDocumentation.visible(view != View.Documentation);
		appViewBlock.visible(view != View.Home && view != View.Documentation && view != View.Search);
		logo.visible(view != View.Home && view != View.Documentation && view != View.Search);
		logoLink.visible(view != View.Home && view != View.Documentation);
		logoExpandedLink.visible(view == View.Search || view == View.Documentation);
		logoExpanded.visible(view == View.Home || view == View.Documentation || view == View.Search);
		content.formats(formats(viewMode));
		appViewSwitch.readonly(!canEditViewMode);
		appViewSwitch.state(viewMode == ViewMode.Languages ? ToggleEvent.State.Off : ToggleEvent.State.On);
		appViewText.value(appViewLabel(viewMode));
		login.visible(loggedUser == null);
		user.visible(loggedUser != null);
		notLoggedBlock.visible(loggedUser == null);
		userSettings.visible(loggedUser != null);
		userHome.visible(loggedUser != null && view != View.Dashboard);
		if (userHome.isVisible()) userHome.path(PathHelper.dashboardPath(session()));
	}

	private String appViewLabel(ViewMode viewMode) {
		if (viewMode == ViewMode.Languages) return translate(view == View.Dashboard ? "Languages" : "Language");
		return translate(view == View.Dashboard ? "Models" : "Model");
	}

	private Set<String> formats(ViewMode viewMode) {
		if (view == View.Search) return Set.of("headerStyle");
		return viewMode == ViewMode.Languages ? Set.of("languagesHeaderStyle") : Set.of("modelsHeaderStyle");
	}

	private void changeView(ToggleEvent event) {
		ViewMode viewMode = event.state() == ToggleEvent.State.On ? ViewMode.Models : ViewMode.Languages;
		DisplayHelper.updateViewMode(viewMode, session());
		content.formats(viewMode == ViewMode.Languages ? Set.of("languagesHeaderStyle") : Set.of("modelsHeaderStyle"));
		changeViewListener.accept(viewMode);
		refresh();
	}

	private void notifyOpenSearch() {
		if (openSearchListener == null) return;
		openSearchListener.accept(true);
	}

	private void refreshSettingsDialog() {
		settingsEditor.refresh();
	}

	private void saveSettings() {
		if (!settingsEditor.check()) return;
		settingsDialog.close();
		box().commands(UserCommands.class).save(settingsEditor.tokens(), username());
		notifyUser(translate("Saved successfully"), UserMessage.Type.Success);
	}
}