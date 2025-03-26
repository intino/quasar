package io.quassar.editor.box.ui.displays;

import io.intino.alexandria.ui.Soul;
import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguageView;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.LanguagesView;
import io.quassar.editor.box.util.SessionHelper;

public class RouteDispatcher extends AbstractRouteDispatcher {

	@Override
	public void dispatchHome(Soul soul) {
		soul.currentLayer(HomeTemplate.class).openHome();
	}

	@Override
	public void dispatchLanguages(Soul soul, String tab, String view) {
		SessionHelper.register(soul.session(), LanguagesTab.from(tab));
		SessionHelper.register(soul.session(), LanguagesView.from(view));
		soul.currentLayer(HomeTemplate.class).openLanguages(tab, view);
	}

	@Override
	public void dispatchLanguage(Soul soul, String language, String tab, String view) {
		SessionHelper.register(soul.session(), LanguageTab.from(tab));
		SessionHelper.register(soul.session(), LanguageView.from(view));
		soul.currentLayer(HomeTemplate.class).openLanguage(language, tab, view);
	}

	@Override
	public void dispatchModel(Soul soul, String language, String model, String release, String file) {
		soul.currentLayer(HomeTemplate.class).openModel(language, model, release, file);
	}

	@Override
	public void dispatchLogin(Soul soul) {
	}

}