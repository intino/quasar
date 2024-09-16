package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.models.ModelManager;
import io.intino.ime.model.Model;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ModelsDatasource extends PageDatasource<Model> {
	protected final ImeBox box;
	protected final UISession session;
	private final Boolean onlyPrivate;
	private String condition;
	private List<Filter> filters;

	public ModelsDatasource(ImeBox box, UISession session, Boolean onlyPrivate) {
		this.box = box;
		this.session = session;
		this.onlyPrivate = onlyPrivate;
	}

	public long itemCount() {
		return itemCount(condition, filters);
	}

	@Override
	public List<Model> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		saveParameters(condition, filters);
		List<Model> result = sort(load(condition, filters), sortings);
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

	protected List<Model> load() {
		ModelManager manager = box.modelManager();
		String username = username();
		if (onlyPrivate == null) return manager.ownerModels(username);
		return onlyPrivate ? manager.privateModels(username) : manager.publicModels(username);
	}

	protected String username() {
		return session.user() != null ? session.user().username() : "Anonymous";
	}

	private List<Model> load(String condition, List<Filter> filters) {
		List<Model> models = load();
		models = filterCondition(models, condition);
		return models;
	}

	private List<Model> filterCondition(List<Model> models, String condition) {
		if (condition == null || condition.isEmpty()) return models;
		String[] conditions = condition.toLowerCase().split(" ");
		return models.stream().filter(w ->
				DatasourceHelper.matches(w.name(), conditions) ||
				DatasourceHelper.matches(w.title(), conditions) ||
				DatasourceHelper.matches(w.owner().name(), conditions) ||
				DatasourceHelper.matches(w.owner().fullName(), conditions) ||
				DatasourceHelper.matches(w.language(), conditions)
		).collect(toList());
	}

	private List<Model> sort(List<Model> models, List<String> sortings) {
		if (sortings.contains("Language")) return models.stream().sorted(Comparator.comparing(m -> m.language() != null ? m.language() : "z")).toList();
		else if (sortings.contains("Owner")) return models.stream().sorted(Comparator.comparing(m -> m.owner() != null ? m.owner().fullName() : "z")).toList();
		else if (sortings.contains("Last modified")) return models.stream().sorted(Comparator.comparing(Model::lastModifyDate)).toList();
		return models.stream().sorted(Comparator.comparing(m -> m.title().toLowerCase())).toList();
	}

	private void saveParameters(String condition, List<Filter> filters) {
		this.condition = condition;
		this.filters = filters;
	}

}
