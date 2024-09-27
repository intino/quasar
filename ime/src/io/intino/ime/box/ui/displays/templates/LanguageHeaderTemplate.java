package io.intino.ime.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.actionable.ToggleEvent;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.DisplayHelper;
import io.intino.ime.box.ui.PathHelper;
import io.intino.ime.box.ui.ViewMode;
import io.intino.ime.model.Language;

import java.util.function.Consumer;

import static io.intino.alexandria.ui.displays.events.actionable.ToggleEvent.State.Off;

public class LanguageHeaderTemplate extends AbstractLanguageHeaderTemplate<ImeBox> {
	private Language language;
	private Consumer<ViewMode> viewModeChangeListener;

	public LanguageHeaderTemplate(ImeBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onChangeViewMode(Consumer<ViewMode> listener) {
		this.viewModeChangeListener = listener;
	}

	@Override
	public void init() {
		super.init();
		login.onExecute(e -> {
			session().add("callback", session().browser().requestUrl());
			notifier.redirect(session().login(session().browser().baseUrl()));
		});
	}

	@Override
	public void refresh() {
		super.refresh();
		login.visible(user() == null);
		dashboard.visible(user() != null);
		user.visible(user() != null);
		if (session().user() == null) return;
		userHome.path(PathHelper.dashboardPath(session()));
	}

}