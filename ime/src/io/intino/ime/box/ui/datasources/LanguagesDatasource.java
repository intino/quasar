package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.languages.LanguageManager;
import io.intino.ime.model.Language;
import io.intino.ime.model.Model;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class LanguagesDatasource extends PageDatasource<Language> {
	protected final ImeBox box;
	protected final UISession session;
	private String condition;
	private List<Filter> filters;
	private String owner;
	private Sorting sorting;

	public LanguagesDatasource(ImeBox box, UISession session) {
		this.box = box;
		this.session = session;
	}

	public enum Sorting { MostUsed, MostRecents;}
	public void sort(Sorting sorting) {
		this.sorting = sorting;
	}

	public String owner() {
		return owner;
	}

	public void owner(String owner) {
		this.owner = owner;
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

	protected String username() {
		return session.user() != null ? session.user().username() : null;
	}

	private List<Language> load(String condition, List<Filter> filters) {
		List<Language> workspaces = load();
		workspaces = filterOwner(workspaces);
		workspaces = filterCondition(workspaces, condition);
		return workspaces;
	}

	private List<Language> load() {
		Map<String, List<Language>> groupedLanguages = box.languageManager().publicLanguages(username()).stream().collect(groupingBy(Language::name));
		return groupedLanguages.values().stream().map(List::getLast).collect(toList());
	}

	private List<Language> filterOwner(List<Language> languages) {
		if (owner == null) return languages;
		return languages.stream().filter(l -> l.owner().equals(owner)).collect(toList());
	}

	private List<Language> filterCondition(List<Language> languages, String condition) {
		if (condition == null || condition.isEmpty()) return languages;
		String[] conditions = condition.toLowerCase().split(" ");
		return languages.stream().filter(w ->
				DatasourceHelper.matches(w.name(), conditions) ||
				DatasourceHelper.matches(w.version(), conditions) ||
				DatasourceHelper.matches(w.info().level().name(), conditions) ||
				DatasourceHelper.matches(w.owner(), conditions)
		).collect(toList());
	}

	private List<Language> sort(List<Language> languages, List<String> sortings) {
		if (sorting == Sorting.MostRecents) return languages.stream().sorted((o1, o2) -> o2.createDate().compareTo(o1.createDate())).toList();
		return languages.stream().sorted((o1, o2) -> Integer.compare(o2.modelsCount(), o1.modelsCount())).toList();
	}

	private void saveParameters(String condition, List<Filter> filters) {
		this.condition = condition;
		this.filters = filters;
	}

}
