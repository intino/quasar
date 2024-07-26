package io.intino.ime.box.util;

import io.intino.alexandria.Scale;
import io.intino.alexandria.Timetag;
import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class WorkspaceSequence {
	private static File file;

	public static void init(File file) {
		WorkspaceSequence.file = file;
		WorkspaceSequence.file.getParentFile().mkdirs();
	}

	public static synchronized long next() {
		Timetag timetag = new Timetag(Instant.now(), Scale.Year);
		Long current = WorkspaceSequence.get();
		long next = current + 1;
		WorkspaceSequence.save(timetag, next);
		return next;
	}

	public static Long get() {
		Timetag timetag = new Timetag(Instant.now(), Scale.Year);
		return load().getOrDefault(timetag, 0L);
	}

	private static Map<Timetag, Long> load() {
		if (file == null)
			throw new RuntimeException("Input file not defined. Use setup method.");
		try {
			if (!file.exists()) return Collections.emptyMap();
			return Files.lines(file.toPath())
					.collect(toMap(line -> new Timetag(line.split("\t")[0]), line -> Long.valueOf(line.split("\t")[1])));
		} catch (IOException e) {
			Logger.error(e);
			return Collections.emptyMap();
		}
	}

	private synchronized static void save(Timetag timetag, long sequence) {
		try {
			Map<Timetag, Long> map = new HashMap<>(load());
			map.put(timetag, sequence);
			String content = map.entrySet().stream().map(e -> e.getKey() + "\t" + e.getValue()).collect(Collectors.joining("\n"));
			Files.write(file.toPath(), content.getBytes());
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
