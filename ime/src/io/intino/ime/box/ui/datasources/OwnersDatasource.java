package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.ui.model.Owner;
import io.intino.ime.model.Language;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class OwnersDatasource extends PageDatasource<Owner> {
	protected final ImeBox box;
	protected final UISession session;

	public OwnersDatasource(ImeBox box, UISession session) {
		this.box = box;
		this.session = session;
	}

	@Override
	public List<Owner> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		List<Owner> result = sort(load(condition, filters), sortings);
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

	private List<Owner> load(String condition, List<Filter> filters) {
		return load();
	}

	protected List<Owner> load() {
		Map<String, Owner> result = new HashMap<>();
		Map<String, List<Language>> languageList = box.languageManager().publicLanguages(username()).stream().collect(groupingBy(Language::owner));
		languageList.forEach((key, value) -> registerLanguages(key, value, result));
		return new ArrayList<>(result.values());
	}

	private void registerLanguages(String owner, List<Language> languages, Map<String, Owner> owners) {
		if (!owners.containsKey(owner)) owners.put(owner, new Owner(owner));
		owners.get(owner).addAll(languages);
	}

	private List<Owner> sort(List<Owner> owners, List<String> sortings) {
		return owners;
	}

}
