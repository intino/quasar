package io.quassar.editor.box.users;

import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.box.util.SubjectHelper;
import io.quassar.editor.model.User;
import org.apache.commons.io.FileUtils;
import systems.intino.datamarts.subjectstore.SubjectStore;
import systems.intino.datamarts.subjectstore.model.Subject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UserManager {
	private final Archetype archetype;
	private final SubjectStore subjectStore;
	private final int newUserLicenseTime;

	public UserManager(Archetype archetype, SubjectStore store, int newUserLicenseTime) {
		this.archetype = archetype;
		this.subjectStore = store;
		this.newUserLicenseTime = newUserLicenseTime;
	}

	public List<User> users() {
		return subjectStore.query().isType(SubjectHelper.UserType).isRoot().collect().stream().map(this::get).toList();
	}

	public boolean exists(String key) {
		return get(key) != null;
	}

	public User get(String key) {
		Subject subject = subjectStore.open(SubjectHelper.userPath(key));
		if (subject == null || subject.isNull()) subject = subjectStore.query().isType(SubjectHelper.UserType).where("name").equals(key).collect().stream().findFirst().orElse(null);
		return get(subject);
	}

	public User create(String id, String name) {
		User user = new User(subjectStore.create(SubjectHelper.userPath(id)));
		user.id(id);
		user.name(name);
		user.licenseTime(newUserLicenseTime);
		return user;
	}

	public void remove(User user) {
		try {
			File rootDir = archetype.users().user(user.name());
			if (rootDir.exists()) FileUtils.deleteDirectory(rootDir);
			subjectStore.open(SubjectHelper.pathOf(user)).drop();
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
