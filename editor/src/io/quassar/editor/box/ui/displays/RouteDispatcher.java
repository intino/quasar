package io.quassar.editor.box.ui.displays;

import io.intino.alexandria.ui.Soul;
import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.ui.types.*;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Model;

public class RouteDispatcher extends AbstractRouteDispatcher {

	@Override
	public void dispatchHome(Soul soul, String dialog) {
		soul.currentLayer(HomeTemplate.class).openHome(dialog);
	}

	@Override
	public void dispatchAbout(Soul soul) {
		soul.currentLayer(HomeTemplate.class).openAbout();
	}

	@Override
	public void dispatchCollection(Soul soul, String collection) {
		soul.currentLayer(HomeTemplate.class).openCollection(collection);
	}

	@Override
	public void dispatchLanguage(Soul soul, String language, String tab) {
		SessionHelper.register(soul.session(), LanguageTab.from(tab));
		soul.currentLayer(HomeTemplate.class).openLanguage(language, tab);
	}

	@Override
	public void dispatchLanguageAction(Soul soul, String language, String action, String version) {
		if (action.equalsIgnoreCase("new")) soul.currentLayer(HomeTemplate.class).createModel(language);
		else if (action.equalsIgnoreCase("help")) soul.currentLayer(HomeTemplate.class).openHelp(language, version);
		else if (action.equalsIgnoreCase("template")) soul.currentLayer(HomeTemplate.class).openTemplate(language, version);
		else soul.currentLayer(HomeTemplate.class).openModel(action, null, null, null, null, null);
	}

	@Override
	public void dispatchModel(Soul soul, String language, String model, String release, String tab, String view, String file, String position) {
		SessionHelper.register(soul.session(), ModelView.from(view));
		soul.currentLayer(HomeTemplate.class).openModel(model, release != null && !release.isEmpty() ? release : null, tab, view, clean(file), position);
	}

	@Override
	public void dispatchForge(Soul soul, String model, String release, String view) {
		SessionHelper.register(soul.session(), ForgeView.from(view));
		soul.currentLayer(HomeTemplate.class).open(model, release, view);
	}

	@Override
	public void dispatchLogin(Soul soul) {
	}

	private String clean(String file) {
		return file != null && file.endsWith("|") ? file.substring(0, file.length()-1) : file;
	}

}