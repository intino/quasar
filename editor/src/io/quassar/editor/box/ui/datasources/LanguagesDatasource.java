package io.quassar.editor.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.languages.LanguageManager;
import io.quassar.editor.box.ui.types.LanguagesTab;
import io.quassar.editor.box.util.DatasourceHelper;
import io.quassar.editor.model.Language;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class LanguagesDatasource extends PageDatasource<Language> {
	protected final EditorBox box;
	protected final UISession session;
	private final LanguagesTab tab;

	public LanguagesDatasource(EditorBox box, UISession session, LanguagesTab tab) {
		this.box = box;
		this.session = session;
		this.tab = tab;
	}

	@Override
	public List<Language> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		List<Language> result = sort(load(condition, filters), sortings);
		int from = Math.min(start, result.size());
		int end = Math.min(start + count, result.size());
		return result.subList(from, end);
	}

	@Override
	public long itemCount(String condition, List<Filter> filters) {
		return load(condition, filters).size();
	}

	@Override
	public List<Group> groups(String key) {
		if (key.equalsIgnoreCase(DatasourceHelper.Owner)) return load().stream().map(Language::owner).distinct().map(o -> new Group().name(o).label(o)).toList();
		return new ArrayList<>();
	}

	protected String username() {
		return session.user() != null ? session.user().username() : null;
	}

	private List<Language> load(String condition, List<Filter> filters) {
		List<Language> languages = load();
		languages = filterCondition(languages, condition);
		return languages;
	}

	protected List<Language> load() {
		LanguageManager manager = box.languageManager();
		return tab == null || tab == LanguagesTab.PublicLanguages ? manager.publicLanguages() : manager.ownerLanguages(username());
	}

	private List<Language> filterCondition(List<Language> languages, String condition) {
		if (condition == null || condition.isEmpty()) return languages;
		String[] conditions = condition.toLowerCase().split(" ");
		return languages.stream().filter(l ->
				DatasourceHelper.matches(l.name(), conditions) ||
				DatasourceHelper.matches(l.description(), conditions) ||
				DatasourceHelper.matches(l.owner(), conditions)
		).collect(toList());
	}

	private List<Language> sort(List<Language> languages, List<String> sortings) {
		return languages.stream().sorted((o1, o2) -> Long.compare(o2.modelsCount(), o1.modelsCount())).toList();
	}

}
