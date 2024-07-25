package io.intino.ime.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.intino.ime.box.ImeBox;
import io.intino.ime.box.workspaces.Workspace;

import java.util.List;

public abstract class WorkspacesDatasource extends PageDatasource<Workspace> {
	protected final ImeBox box;
	protected final UISession session;

	public WorkspacesDatasource(ImeBox box, UISession session) {
		this.box = box;
		this.session = session;
	}

	@Override
	public List<Workspace> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		List<Workspace> result = sort(load(condition, filters), sortings);
		int from = Math.min(start, result.size());
		int end = Math.min(start + count, result.size());
		return result.subList(from, end);
	}

	@Override
	public long itemCount(String condition, List<Filter> filters) {
		return 0;
	}

	@Override
	public List<Group> groups(String key) {
		return List.of();
	}

	protected abstract List<Workspace> load();

	protected String username() {
		return session.user() != null ? session.user().username() : "Anonymous";
	}

	private List<Workspace> load(String condition, List<Filter> filters) {
		return load();
	}

	private List<Workspace> sort(List<Workspace> workspaces, List<String> sortings) {
		return workspaces.stream().sorted((o1, o2) -> o2.lastModifyDate().compareTo(o1.lastModifyDate())).toList();
	}

}
