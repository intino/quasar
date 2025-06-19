package io.quassar.editor.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.GridDatasource;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.grid.GridColumn;
import io.intino.alexandria.ui.model.datasource.grid.GridColumnMode;
import io.intino.alexandria.ui.model.datasource.grid.GridGroupBy;
import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Collection;
import io.quassar.editor.model.License;

import java.util.Comparator;
import java.util.List;

public class LicensesDatasource extends GridDatasource<License> {
	protected final EditorBox box;
	protected final UISession session;
	private final Collection collection;

	public LicensesDatasource(EditorBox box, UISession session, Collection collection) {
		this.box = box;
		this.session = session;
		this.collection = collection;
	}

	@Override
	public String name() {
		return collection.name() + "-licenses";
	}

	@Override
	public List<License> items(int start, int count, String condition, List<Filter> filters, List<String> sortings, GridGroupBy groupBy) {
		List<License> result = sort(load(condition, filters), sortings);
		int from = Math.min(start, result.size());
		int end = Math.min(start + count, result.size());
		return result.subList(from, end);
	}

	@Override
	public long itemCount(String condition, List<Filter> filters, GridGroupBy groupBy) {
		return load(condition, filters).size();
	}

	@Override
	public List<String> columnGroups(GridColumn<License> column, String mode, String condition, List<Filter> filters) {
		return List.of();
	}

	@Override
	public List<GridColumnMode> columnModes() {
		return List.of();
	}

	@Override
	public long itemCount(String condition, List<Filter> filters) {
		return load(condition, filters).size();
	}

	@Override
	public List<Group> groups(String key) {
		return List.of();
	}

	protected List<License> load(String condition, List<Filter> filters) {
		return collection.licenses();
	}

	protected List<License> sort(List<License> licenses, List<String> sortings) {
		if (sortings.isEmpty()) return licenses;
		String column = sortings.getFirst().split("=")[0];
		boolean descending = sortings.getFirst().split("=")[1].equals("D");
		Comparator<License> comparator = null;
		switch (column) {
			case "code": { comparator = codeComparator(descending); break; }
			case "creationDate": { comparator = creationDateComparator(descending); break; }
			case "duration": { comparator = durationComparator(descending); break; }
			case "status": { comparator = statusComparator(descending); break; }
			case "user": { comparator = userComparator(descending); break; }
			case "assignmentDate": { comparator = assignmentDateComparator(descending); break; }
			case "expirationDate": { comparator = expirationDateComparator(descending); break; }
		}
		if (comparator == null) return licenses;
		return licenses.stream().sorted(comparator).toList();
	}

	private Comparator<License> codeComparator(boolean descending) {
		return (o1, o2) -> {
			if (descending) return o2.code().compareTo(o1.code());
			return o1.code().compareTo(o2.code());
		};
	}

	private Comparator<License> creationDateComparator(boolean descending) {
		return (o1, o2) -> {
			if (descending) return o2.createDate().compareTo(o1.createDate());
			return o1.createDate().compareTo(o2.createDate());
		};
	}

	private Comparator<License> durationComparator(boolean descending) {
		return (o1, o2) -> {
			if (descending) return Integer.compare(o2.duration(), o1.duration());
			return Integer.compare(o1.duration(), o2.duration());
		};
	}

	private Comparator<License> statusComparator(boolean descending) {
		return (o1, o2) -> {
			if (descending) return o2.status().name().compareTo(o1.status().name());
			return o1.status().name().compareTo(o2.status().name());
		};
	}

	private Comparator<License> userComparator(boolean descending) {
		return (o1, o2) -> {
			if (o1.user() == null && o2.user() == null) return -1;
			if (descending) return o2.user() == null ? -1 : o1.user() == null ? 1 : o2.user().compareTo(o1.user());
			return o1.user() == null ? -1 : o2.user() == null ? 1 : o1.user().compareTo(o2.user());
		};
	}

	private Comparator<License> assignmentDateComparator(boolean descending) {
		return (o1, o2) -> {
			if (o1.assignDate() == null || o2.assignDate() == null) return -1;
			if (descending) return o2.assignDate() == null ? -1 : o1.assignDate() == null ? 1 : o2.assignDate().compareTo(o1.assignDate());
			return o1.assignDate() == null ? -1 : o2.assignDate() == null ? 1 : o1.assignDate().compareTo(o2.assignDate());
		};
	}

	private Comparator<License> expirationDateComparator(boolean descending) {
		return (o1, o2) -> {
			if (o1.expireDate() == null || o2.expireDate() == null) return -1;
			if (descending) return o2.expireDate() == null ? -1 : o1.expireDate() == null ? 1 : o2.expireDate().compareTo(o1.expireDate());
			return o1.expireDate() == null ? -1 : o2.expireDate() == null ? 1 : o1.expireDate().compareTo(o2.expireDate());
		};
	}

}
