package io.quassar.editor.box.ui.displays;

import io.intino.alexandria.ui.Soul;
import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.ui.types.ModelView;
import io.quassar.editor.box.util.SessionHelper;

public class RouteDispatcher extends AbstractRouteDispatcher {

	@Override
	public void dispatchHome(Soul soul) {
		soul.currentLayer(HomeTemplate.class).openHome();
	}

	@Override
	public void dispatchAbout(Soul soul) {
		soul.currentLayer(HomeTemplate.class).openAbout();
	}

	@Override
	public void dispatchLanguages(Soul soul, String tab) {
		SessionHelper.register(soul.session(), LanguagesTab.from(tab));
		soul.currentLayer(HomeTemplate.class).openLanguages(tab);
	}

	@Override
	public void dispatchLanguage(Soul soul, String language, String tab) {
		SessionHelper.register(soul.session(), LanguageTab.from(tab));
		soul.currentLayer(HomeTemplate.class).openLanguage(language, tab);
	}

	@Override
	public void dispatchModel(Soul soul, String language, String model, String release, String view, String file, String position) {
		SessionHelper.register(soul.session(), ModelView.from(view));
		soul.currentLayer(HomeTemplate.class).openModel(language, model, release, view, file, position);
	}

	@Override
	public void dispatchLogin(Soul soul) {
	}

}