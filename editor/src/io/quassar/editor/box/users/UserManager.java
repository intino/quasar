package io.quassar.editor.box.users;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.model.User;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class UserManager {
	private final Archetype archetype;

	public UserManager(Archetype archetype) {
		this.archetype = archetype;
	}

	public List<User> users() {
		File[] root = archetype.users().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(d -> get(d.getName())).filter(Objects::nonNull).collect(toList());
	}

	public boolean exists(String username) {
		return users().stream().anyMatch(m -> m.name().equals(username));
	}

	public User get(String username) {
		try {
			File properties = archetype.users().properties(username);
			if (!properties.exists()) return null;
			return Json.fromJson(Files.readString(properties.toPath()), User.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	public User create(String name) {
		User user = new User();
		user.name(name);
		save(user);
		return user;
	}

	public void save(User user) {
		try {
			Files.writeString(archetype.users().properties(user.name()).toPath(), Json.toString(user));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void remove(User user) {
		try {
			File rootDir = archetype.users().user(user.name());
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
