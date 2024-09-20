package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.util.VersionNumberComparator;
import io.intino.ime.model.Release;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class LanguageReleasesDatasource extends PageDatasource<Release> {
	protected final ImeBox box;
	protected final UISession session;
	private final String name;
	private String condition;
	private List<Filter> filters;

	public LanguageReleasesDatasource(ImeBox box, UISession session, String name) {
		this.box = box;
		this.session = session;
		this.name = name;
	}

	public long itemCount() {
		return itemCount(condition, filters);
	}

	@Override
	public List<Release> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		saveParameters(condition, filters);
		List<Release> result = sort(load(condition, filters), sortings);
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

	private List<Release> load() {
		return box.languageManager().releases(name);
	}

	protected String username() {
		return session.user() != null ? session.user().username() : null;
	}

	private List<Release> load(String condition, List<Filter> filters) {
		List<Release> result = load();
		result = filterCondition(result, condition);
		return result;
	}

	private List<Release> filterCondition(List<Release> releases, String condition) {
		if (condition == null || condition.isEmpty()) return releases;
		String[] conditions = condition.toLowerCase().split(" ");
		return releases.stream().filter(w ->
				DatasourceHelper.matches(w.language(), conditions) ||
				DatasourceHelper.matches(w.version(), conditions) ||
				DatasourceHelper.matches(w.level().name(), conditions)
		).collect(toList());
	}

	private List<Release> sort(List<Release> releases, List<String> sortings) {
		return releases.stream().sorted((o1, o2) -> VersionNumberComparator.getInstance().compare(o2.version(), o1.version())).toList();
	}

	private void saveParameters(String condition, List<Filter> filters) {
		this.condition = condition;
		this.filters = filters;
	}

}
