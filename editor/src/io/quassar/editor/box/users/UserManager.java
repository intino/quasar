package io.quassar.editor.box.users;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.User;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectindex.SubjectTree;
import systems.intino.datamarts.subjectindex.model.Subject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class UserManager {
	private final Archetype archetype;
	private final SubjectTree subjectTree;

	public UserManager(Archetype archetype, SubjectTree subjectTree) {
		this.archetype = archetype;
		this.subjectTree = subjectTree;
	}

	public List<User> users() {
		return subjectTree.subjects(SubjectHelper.UserType).roots().stream().map(this::get).toList();
	}

	public boolean exists(String key) {
		return get(key) != null;
	}

	public User get(String key) {
		Subject subject = subjectTree.get(SubjectHelper.userPath(key));
		if (subject.isNull()) subject = subjectTree.subjects(SubjectHelper.UserType).with("name", key).roots().stream().findFirst().orElse(null);
		return get(subject);
	}

	public User create(String name) {
		String id = UUID.randomUUID().toString();
		User user = new User(subjectTree.create(SubjectHelper.userPath(id)));
		user.id(id);
		user.name(name);
		return user;
	}

	public void remove(User user) {
		try {
			File rootDir = archetype.users().user(user.name());
			if (!rootDir.exists()) return;
			subjectTree.drop(SubjectHelper.pathOf(user));
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private User get(Subject subject) {
		if (subject == null || subject.isNull()) return null;
		return new User(subject);
	}

}
