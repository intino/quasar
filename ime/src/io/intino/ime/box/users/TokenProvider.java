package io.intino.ime.box.users;

import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TokenProvider {
	private final File root;
	private final Supplier<Map<String, Record>> MapFactory = HashMap::new;;
	private volatile Map<String, Record> records = MapFactory.get();

	public TokenProvider(File root) {
		this.root = root;
		load();
	}

	private static final Map<String, BiConsumer<Record, String>> setters = new HashMap<>() {{
		put("dockerhub.token", (r, v) -> r.dockerHubToken = v);
	}};

	public void load() {
		try {
			if (!root.exists()) return;
			Map<String, Record> newRecords = MapFactory.get();
			Files.readAllLines(root.toPath()).stream()
					.filter(l -> !l.trim().isEmpty())
					.map(l -> l.split("\t", -1))
					.forEach(l -> setters.getOrDefault(l[1], nullSetter()).accept(record(newRecords, l[0]), l[2]));

			records = newRecords;
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private BiConsumer<Record, String> nullSetter() {
		return (record, s) -> {
		};
	}

	public Record of(String username) {
		return records.getOrDefault(username, emptyRecord(username));
	}

	public void save(Record record) {
		records.put(record.username, record);
		save();
	}

	private Record record(Map<String, Record> records, String id) {
		if (!records.containsKey(id)) {
			Record record = new Record(id);
			records.put(id, record);
		}
		return records.get(id);
	}

	public static class Record {
		private final String username;
		private String dockerHubToken;

		public Record(String username) {
			this.username = username;
		}

		public String username() {
			return username;
		}

		public String dockerHubToken() {
			return dockerHubToken;
		}

		public Record dockerHubToken(String dockerHubToken) {
			this.dockerHubToken = dockerHubToken;
			return this;
		}
	}

	private void save() {
		try {
			String content = records.values().stream().map(this::serialize).collect(Collectors.joining("\n"));
			Files.writeString(root.toPath(), content);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private static final String Triple = "%s	%s	%s";
	private String serialize(Record record) {
		return String.format(Triple, record.username, "dockerhub.token", record.dockerHubToken);
	}

	private Record emptyRecord(String username) {
		return new Record(username).dockerHubToken(null);
	}

}