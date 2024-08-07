package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Language;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class LanguagesDatasource extends PageDatasource<Language> {
	protected final ImeBox box;
	protected final UISession session;
	private String condition;
	private List<Filter> filters;

	public LanguagesDatasource(ImeBox box, UISession session) {
		this.box = box;
		this.session = session;
	}

	public long itemCount() {
		return itemCount(condition, filters);
	}

	@Override
	public List<Language> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		saveParameters(condition, filters);
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
		return List.of();
	}

	private List<Language> load() {
		return box.languageManager().publicLanguages(username());
	}

	protected String username() {
		return session.user() != null ? session.user().username() : null;
	}

	private List<Language> load(String condition, List<Filter> filters) {
		List<Language> workspaces = load();
		workspaces = filterCondition(workspaces, condition);
		return workspaces;
	}

	private List<Language> filterCondition(List<Language> languages, String condition) {
		if (condition == null || condition.isEmpty()) return languages;
		String[] conditions = condition.toLowerCase().split(" ");
		return languages.stream().filter(w ->
				DatasourceHelper.matches(w.name(), conditions) ||
				DatasourceHelper.matches(w.version(), conditions) ||
				DatasourceHelper.matches(w.level().value().name(), conditions) ||
				DatasourceHelper.matches(w.owner(), conditions)
		).collect(toList());
	}

	private List<Language> sort(List<Language> languages, List<String> sortings) {
		return languages.stream().sorted((o1, o2) -> o2.createDate().compareTo(o1.createDate())).toList();
	}

	private void saveParameters(String condition, List<Filter> filters) {
		this.condition = condition;
		this.filters = filters;
	}

}
