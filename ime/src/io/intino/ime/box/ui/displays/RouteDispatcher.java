package io.intino.ime.box.ui.displays;

import io.intino.alexandria.ui.Soul;
import io.intino.ime.box.ui.displays.templates.HomeTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class RouteDispatcher extends AbstractRouteDispatcher {
	@Override
	public void dispatchHome(Soul soul) {
		soul.currentLayer(HomeTemplate.class).openMain();
	}

	@Override
	public void dispatchSearch(Soul soul, String condition) {
		soul.currentLayer(HomeTemplate.class).search(condition != null ? URLDecoder.decode(condition, StandardCharsets.UTF_8) : null);
	}

	@Override
	public void dispatchDocumentation(Soul soul) {
		soul.currentLayer(HomeTemplate.class).openDocumentation();
	}

	@Override
	public void dispatchModel(Soul soul, String id, String file) {
		soul.currentLayer(HomeTemplate.class).openModel(id, file);
	}

	@Override
	public void dispatchDashboard(Soul soul) {
		soul.currentLayer(HomeTemplate.class).openDashboard();
	}

	@Override
	public void dispatchLanguage(Soul soul, String id) {
		soul.currentLayer(HomeTemplate.class).openLanguage(id);
	}

	@Override
	public void dispatchNotFound(Soul soul, String model) {
	}

	@Override
	public void dispatchPermissions(Soul soul, String username, String model, String callback) {
	}

}