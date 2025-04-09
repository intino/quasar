package io.quassar.editor.box.users;

import io.intino.alexandria.Json;
import io.intino.alexandria.logger.Logger;
import io.quassar.archetype.Archetype;
import io.quassar.editor.model.User;
import org.apache.commons.io.FileUtils;
import systems.intino.alexandria.datamarts.anchormap.AnchorMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class UserManager {
	private final Archetype archetype;
	private final AnchorMap index;

	public UserManager(Archetype archetype, AnchorMap index) {
		this.archetype = archetype;
		this.index = index;
	}

	public List<User> users() {
		File[] root = archetype.users().root().listFiles();
		if (root == null) return Collections.emptyList();
		return Arrays.stream(root).filter(File::isDirectory).map(d -> get(d.getName())).filter(Objects::nonNull).collect(toList());
	}

	public boolean exists(String key) {
		return locate(key) != null;
	}

	public User get(String key) {
		try {
			String id = locate(key);
			File properties = archetype.users().properties(id);
			if (!properties.exists()) {
				archetype.users().user(id).delete();
				return null;
			}
			return Json.fromJson(Files.readString(properties.toPath()), User.class);
		} catch (IOException e) {
			Logger.error(e);
			return null;
		}
	}

	private String locate(String key) {
		Map<String, String> usersMap = index.search("user").execute().stream().collect(toMap(u -> nameOf(index.get(u.replace(":user", ""), "user")), u -> u.replace(":user", "")));
		return usersMap.getOrDefault(key, key);
	}

	private String nameOf(List<String> values) {
		return values.stream().filter(v -> v.startsWith("name=")).map(v -> v.replace("name=", "")).findFirst().orElse(null);
	}

	public User create(String name) {
		User user = new User();
		user.id(UUID.randomUUID().toString());
		user.name(name);
		save(user);
		return user;
	}

	public void save(User user) {
		try {
			index.on(user.id(), "user").set("name", user.name()).commit();
			Files.writeString(archetype.users().properties(user.id()).toPath(), Json.toString(user));
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	public void remove(User user) {
		try {
			// TODO JJ index.on(user.id(), "user").remove().commit();
			File rootDir = archetype.users().user(user.name());
			if (!rootDir.exists()) return;
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

}
