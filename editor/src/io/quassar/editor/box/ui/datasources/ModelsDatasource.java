package io.quassar.editor.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.models.ModelManager;
import io.quassar.editor.box.ui.types.LanguageTab;
import io.quassar.editor.box.util.DatasourceHelper;
import io.quassar.editor.box.util.SessionHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageRelease;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ModelsDatasource extends PageDatasource<Model> {
	protected final EditorBox box;
	protected final UISession session;
	private final Language language;
	private final LanguageTab tab;
	private final LanguageRelease release;
	private String condition;
	private List<Filter> filters;
	private Sorting sorting;

	public ModelsDatasource(EditorBox box, UISession session, Language language, LanguageRelease release, LanguageTab tab) {
		this.box = box;
		this.session = session;
		this.language = language;
		this.release = release;
		this.tab = tab;
	}

	public enum Sorting { MostRecent, LastModified}
	public void sort(Sorting sorting) {
		this.sorting = sorting;
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
		if (key.equalsIgnoreCase(DatasourceHelper.Owner)) return load().stream().map(Model::owner).distinct().map(o -> new Group().name(o).label(o)).toList();
		return new ArrayList<>();
	}

	protected List<Model> load() {
		ModelManager manager = box.modelManager();
		return tab == LanguageTab.Examples ? manager.exampleModels(language, release) : manager.models(language, username());
	}

	protected String username() {
		return session.user() != null ? session.user().username() : User.Anonymous;
	}

	private List<Model> load(String condition, List<Filter> filters) {
		List<Model> models = load();
		models = filterOwner(models, filters);
		models = filterCondition(models, condition);
		return models;
	}

	private List<Model> filterOwner(List<Model> models, List<Filter> filters) {
		List<String> owners = DatasourceHelper.categories(DatasourceHelper.Owner, filters);
		if (owners.isEmpty()) return models;
		return models.stream().filter(l -> owners.contains(l.owner())).collect(toList());
	}

	private List<Model> filterCondition(List<Model> models, String condition) {
		if (condition == null || condition.isEmpty()) return models;
		String[] conditions = condition.toLowerCase().split(" ");
		return models.stream().filter(m ->
				DatasourceHelper.matches(m.name(), conditions) ||
				DatasourceHelper.matches(m.title(), conditions) ||
				DatasourceHelper.matches(m.qualifiedTitle(), conditions) ||
				DatasourceHelper.matches(m.description(), conditions) ||
				DatasourceHelper.matches(m.project(), conditions) ||
				DatasourceHelper.matches(m.module(), conditions) ||
				DatasourceHelper.matches(m.owner(), conditions) ||
				DatasourceHelper.matches(m.language().toString(), conditions)
		).collect(toList());
	}

	private List<Model> sort(List<Model> models, List<String> sortings) {
		String sorting = sortings.isEmpty() ? SessionHelper.modelsSorting(session).name() : sortings.getFirst();
		if (sorting != null && sorting.equalsIgnoreCase(Sorting.LastModified.name())) return models.stream().sorted(lastModifiedComparator()).toList();
		return models.stream().sorted(mostRecentComparator()).toList();
	}

	private Comparator<? super Model> mostRecentComparator() {
		return (Comparator<Model>) (o1, o2) -> {
			Instant instant1 = o1.createDate();
			Instant instant2 = o2.createDate();
			if (instant1 == null || instant2 == null) return -1;
			return instant2.compareTo(instant1);
		};
	}

	private Comparator<? super Model> lastModifiedComparator() {
		return (Comparator<Model>) (o1, o2) -> {
			Instant instant1 = o1.updateDate();
			Instant instant2 = o2.updateDate();
			if (instant1 == null || instant2 == null) return -1;
			return instant2.compareTo(instant1);
		};
	}

	private void saveParameters(String condition, List<Filter> filters) {
		this.condition = condition;
		this.filters = filters;
	}

}
