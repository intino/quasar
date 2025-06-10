package io.quassar.editor.box.ui.displays;

import io.intino.alexandria.ui.Soul;
import io.quassar.editor.box.ui.displays.templates.ForgeTemplate;
import io.quassar.editor.box.ui.displays.templates.HomeTemplate;
import io.quassar.editor.box.ui.types.*;
import io.quassar.editor.box.util.PathHelper;
import io.quassar.editor.box.util.SessionHelper;

import java.time.Instant;

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
	public void dispatchLanguage(Soul soul, String language, String tab, String view) {
		SessionHelper.register(soul.session(), LanguageTab.from(tab));
		SessionHelper.register(soul.session(), LanguageView.from(view));
		soul.currentLayer(HomeTemplate.class).openLanguage(language, tab, view);
	}

	@Override
	public void dispatchLanguageReleaseHelp(Soul soul, String language, String version) {
		soul.currentLayer(HomeTemplate.class).openHelp(language, version);
	}

	@Override
	public void dispatchLanguageReleaseTemplate(Soul soul, String language, String version) {
		soul.currentLayer(HomeTemplate.class).openTemplate(language, version);
	}

	@Override
	public void dispatchModel(Soul soul, String language, String model, String release, String view, String file, String position) {
		SessionHelper.register(soul.session(), ModelView.from(view));
		if (model.equalsIgnoreCase("new")) soul.currentLayer(HomeTemplate.class).createModel(language);
		else soul.currentLayer(HomeTemplate.class).openModel(model, release, view, clean(file), position);
	}

	@Override
	public void dispatchStartingModel(Soul soul, String language, String model) {
		soul.currentLayer(HomeTemplate.class).openStartingModel(model);
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