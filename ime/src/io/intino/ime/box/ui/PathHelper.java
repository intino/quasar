package io.intino.ime.box.ui;

import io.intino.alexandria.Json;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.alexandria.ui.services.push.User;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;
import org.eclipse.jetty.util.UrlEncoded;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class PathHelper {

	public static final String PublicUser = "public";

	public static String homeUrl(UISession session) {
		return session.browser().baseUrl();
	}

	public static String dashboardUrl(UISession session) {
		return session.browser().baseUrl() + "/" + (session.isLogged() ? "dashboard" : "");
	}

	public static String languagesUrl(UISession session) {
		return session.browser().baseUrl() + "/languages";
	}

	public static String languagesUrl(UISession session, Language parent) {
		return session.browser().baseUrl() + languagesPath(parent);
	}

	private record LanguageFilters(String parent) { }
	public static String languagesPath(Language language) {
		if (language == null) return "";
		return "/languages?filters=" + stringify(new LanguageFilters(language.name()));
	}

	public static String languagePath(Language language) {
		return languagePath(language.name());
	}

	public static String languagePath(String language) {
		return "/languages/" + language;
	}

	public static String modelsUrl(UISession session) {
		return session.browser().baseUrl() + "/models";
	}

	public static String modelsUrl(UISession session, Language language) {
		if (language == null) return "";
		return session.browser().baseUrl() + modelsPath(language);
	}

	public static String modelsPath(Language language) {
		return modelsPath(language.name());
	}

	private record ModelFilters(String language) { }
	public static String modelsPath(String language) {
		return "/models?filters="+ stringify(new ModelFilters(language));
	}

	public static String modelUrl(UISession session, Model model) {
		return session.browser().baseUrl() + "/models/" + model.id();
	}

	public static String modelPath(UISession session, Model model) {
		return modelPath(model);
	}

	public static String modelPath(Model model) {
		if (model == null) return null;
		return "/models/" + model.id();
	}

	public static String dashboardPath(UISession session) {
		return dashboardPath(userOf(session));
	}

	public static String dashboardPath(String username) {
		return "/dashboard";
	}

	public static Map<String, String> filtersFrom(String filters) {
		if (filters == null) return Collections.emptyMap();
		return Json.fromString(URLDecoder.decode(filters, StandardCharsets.UTF_8), Map.class);
	}

	private static String userOf(UISession session) {
		User user = session.user();
		return user != null ? user.username() : "public";
	}

	private static String stringify(Object filters) {
		return URLEncoder.encode(Json.toString(filters), StandardCharsets.UTF_8);
	}

}
