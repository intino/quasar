package io.quassar.editor.box.ui.datasources;

import io.intino.alexandria.ui.model.datasource.Filter;
import io.intino.alexandria.ui.model.datasource.Group;
import io.intino.alexandria.ui.model.datasource.PageDatasource;
import io.intino.alexandria.ui.services.push.UISession;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.users.UserManager;
import io.quassar.editor.box.util.DatasourceHelper;
import io.quassar.editor.model.Model;
import io.quassar.editor.model.User;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CollaboratorsDatasource extends PageDatasource<User> {
	protected final EditorBox box;
	protected final UISession session;
	private final List<String> blackList;

	public CollaboratorsDatasource(EditorBox box, UISession session, List<User> blackList) {
		this.box = box;
		this.session = session;
		this.blackList = blackList.stream().map(User::name).toList();
	}

	@Override
	public List<User> items(int start, int count, String condition, List<Filter> filters, List<String> sortings) {
		List<User> result = sort(load(condition, filters), sortings);
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

	protected List<User> load(String condition, List<Filter> filters) {
		UserManager manager = box.userManager();
		List<User> result = manager.users();
		result = filterBlackList(result);
		result = filterCondition(result, condition);
		return result;
	}

	private List<User> filterBlackList(List<User> users) {
		return users.stream().filter(u -> !isLoggedUser(u) && !blackList.contains(u.name())).toList();
	}

	private boolean isLoggedUser(User user) {
		return session.user() != null && session.user().username().equals(user.name());
	}

	private List<User> filterCondition(List<User> users, String condition) {
		if (condition == null || condition.isEmpty()) return users;
		String[] conditions = condition.toLowerCase().split(" ");
		return users.stream().filter(m -> DatasourceHelper.matches(m.name(), conditions)).collect(toList());
	}

	private List<User> sort(List<User> users, List<String> sortings) {
		return users.stream().sorted(Comparator.comparing(m -> m.name().toLowerCase())).toList();
	}

}
