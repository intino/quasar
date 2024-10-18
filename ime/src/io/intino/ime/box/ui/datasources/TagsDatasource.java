package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.model.Language;
import io.intino.ime.box.ui.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class TagsDatasource extends PageDatasource<Tag> {
	protected final ImeBox box;
	protected final UISession session;

	public TagsDatasource(ImeBox box, UISession session) {
		this.box = box;
		this.session = session;
	}

	@Override
	public List<Tag> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		List<Tag> result = sort(load(condition, filters), sortings);
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
		return new ArrayList<>();
	}

	protected String username() {
		return session.user() != null ? session.user().username() : null;
	}

	private List<Tag> load(String condition, List<Filter> filters) {
		return load();
	}

	protected List<Tag> load() {
		Map<String, Tag> result = new HashMap<>();
		Map<List<String>, List<Language>> tagList = box.languageManager().publicLanguages(username()).stream().collect(groupingBy(Language::tagList));
		tagList.forEach((key, value) -> key.forEach(t -> register(t, value, result)));
		return new ArrayList<>(result.values());
	}

	private void register(String tag, List<Language> languages, Map<String, Tag> tags) {
		if (!tags.containsKey(tag)) tags.put(tag, new Tag(tag));
		tags.get(tag).addAll(languages);
	}

	private List<Tag> sort(List<Tag> searchItems, List<String> sortings) {
		return searchItems.stream().sorted((o1, o2) -> Integer.compare(o2.languageList().size(), o1.languageList().size())).toList();
	}

}
