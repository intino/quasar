package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.model.SearchItem;
import io.intino.ime.model.Language;
import io.intino.ime.model.LanguageLevel;

import javax.xml.crypto.Data;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class LanguagesDatasource extends PageDatasource<Language> {
	protected final ImeBox box;
	protected final UISession session;
	private String tag;
	private String owner;
	private String condition;
	private List<Filter> filters;
	private Boolean isPrivate = null;
	private LanguageLevel level = null;
	private Sorting sorting;

	public LanguagesDatasource(ImeBox box, UISession session) {
		this(box, session, null, null, null);
	}

	public LanguagesDatasource(ImeBox box, UISession session, LanguageLevel level) {
		this(box, session, null, null, level);
	}

	public LanguagesDatasource(ImeBox box, UISession session, String owner) {
		this(box, session, owner, null, null);
	}

	public LanguagesDatasource(ImeBox box, UISession session, String owner, boolean isPrivate) {
		this(box, session, owner, isPrivate, null);
	}

	public LanguagesDatasource(ImeBox box, UISession session, String owner, Boolean isPrivate, LanguageLevel level) {
		this.box = box;
		this.session = session;
		this.owner = owner;
		this.isPrivate = isPrivate;
		this.level = level;
	}

	public void tag(String tag) {
		this.tag = tag;
	}

	public void owner(String owner) {
		this.owner = owner;
	}

	public enum Sorting { MostUsed, MostRecent, Name, Description, Owner }
	public void sort(Sorting sorting) {
		this.sorting = sorting;
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
		if (key.equalsIgnoreCase(DatasourceHelper.Owner)) return load().stream().map(Language::owner).distinct().map(o -> new Group().name(o).label(o)).toList();
		return new ArrayList<>();
	}

	protected String username() {
		return session.user() != null ? session.user().username() : null;
	}

	private List<Language> load(String condition, List<Filter> filters) {
		List<Language> languages = load();
		languages = filterTag(languages, filters);
		languages = filterOwner(languages, filters);
		languages = filterPrivate(languages);
		languages = filterCondition(languages, condition);
		return languages;
	}

	protected List<Language> load() {
		Map<String, List<Language>> groupedLanguages = box.languageManager().publicLanguages(username()).stream().collect(groupingBy(Language::name));
		return groupedLanguages.values().stream().map(List::getLast).collect(toList());
	}

	private List<Language> filterTag(List<Language> languages, List<Filter> filters) {
		List<String> tags = this.tag != null ? List.of(this.tag) : Collections.emptyList();
		if (tags.isEmpty()) return languages;
		return languages.stream().filter(l -> tags.stream().anyMatch(t -> l.tagList().contains(t))).collect(toList());
	}

	private List<Language> filterOwner(List<Language> languages, List<Filter> filters) {
		List<String> owners = this.owner != null ? List.of(this.owner) : DatasourceHelper.categories(DatasourceHelper.Owner, filters);
		if (owners.isEmpty()) return languages;
		return languages.stream().filter(l -> owners.contains(l.owner())).collect(toList());
	}

	private List<Language> filterPrivate(List<Language> languages) {
		if (isPrivate == null) return languages;
		return languages.stream().filter(l -> isPrivate ? l.isPrivate() : l.isPublic()).collect(toList());
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
		if (sorting == Sorting.MostRecent) return languages.stream().sorted((o1, o2) -> o2.createDate().compareTo(o1.createDate())).toList();
		else if (sortings.contains(Sorting.Name.name())) return languages.stream().sorted(Comparator.comparing(Language::name)).toList();
		else if (sortings.contains(Sorting.Description.name())) return languages.stream().sorted(Comparator.comparing(Language::description)).toList();
		else if (sortings.contains(Sorting.Owner.name())) return languages.stream().sorted(Comparator.comparing(Language::owner)).toList();
		return languages.stream().sorted((o1, o2) -> Integer.compare(o2.modelsCount(), o1.modelsCount())).toList();
	}

	private void saveParameters(String condition, List<Filter> filters) {
		this.condition = condition;
		this.filters = filters;
	}

}
