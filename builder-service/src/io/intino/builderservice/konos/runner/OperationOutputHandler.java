package io.intino.builderservice.konos.runner;

import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.scheduler.directory.DirectorySentinel;
import io.intino.builder.BuildConstants;
import io.intino.builder.CompilerMessage;
import io.intino.builder.OutputItem;
import io.intino.builderservice.konos.schemas.Message;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.intino.builder.BuildConstants.*;
import static io.intino.builder.CompilerMessage.WARNING;

public class OperationOutputHandler {
	private static final String COMPILER_IN_OPERATION = "Compiler in operation...";
	private final List<OutputItem> compiledItems = new ArrayList<>();
	private final List<CompilerMessage> compilerMessages = new ArrayList<>();
	private final Sentinel sentinel;
	private final ProjectDirectory project;
	private Consumer<String> statusUpdater;

	public OperationOutputHandler(ProjectDirectory project) {
		this.project = project;
		this.sentinel = new Sentinel(project.logFile());
		this.sentinel.init(this::notifyTextAvailable);
	}

	public OperationOutputHandler statusUpdater(Consumer<String> statusUpdater) {
		this.statusUpdater = statusUpdater;
		return this;
	}

	public List<OutputItem> compiledItems() {
		return compiledItems;
	}

	public List<CompilerMessage> compilerMessages() {
		return compilerMessages;
	}

	public void stop() {
		sentinel.stop();
	}

	private void notifyTextAvailable(final String text) {
		final String trimmed = text.trim();
		if (trimmed.startsWith(PRESENTABLE_MESSAGE)) {
			updateStatus(trimmed.substring(PRESENTABLE_MESSAGE.length()));
			return;
		}
		if (BuildConstants.CLEAR_PRESENTABLE.equals(trimmed)) {
			updateStatus(null);
			return;
		}
		if (!trimmed.isEmpty()) for (String line : trimmed.split("\n")) {
			if (line.startsWith(MESSAGES_START)) processMessage(line);
			if (line.startsWith(COMPILED_START)) processCompiledItem(line);
		}
	}

	private void processCompiledItem(String line) {
		final List<String> list = splitAndTrim(line);
		compiledItems.add(new OutputItem(project.reverseDirectoryMapper().apply(list.get(0)), project.reverseDirectoryMapper().apply(list.get(1))));
	}

	private void processMessage(String text) {
		List<String> tokens = splitAndTrim(text);
		String category = tokens.get(0);
		String message = tokens.get(1);
		String url = project.reverseDirectoryMapper().apply(tokens.get(2));
		String lineNum = tokens.get(3);
		String columnNum = tokens.get(4);
		int line = 0, column = 0;
		try {
			line = Integer.parseInt(lineNum);
			column = Integer.parseInt(columnNum);
		} catch (NumberFormatException ignored) {
		}
		Message.Kind kind = category.equals(io.intino.builder.CompilerMessage.ERROR)
				? Message.Kind.ERROR
				: category.equals(WARNING)
				? Message.Kind.WARNING
				: Message.Kind.INFO;
		compilerMessages.add(new CompilerMessage(kind.name(), message, url, line, column));
	}

	private void updateStatus(String status) {
		if (statusUpdater != null) statusUpdater.accept(status == null ? COMPILER_IN_OPERATION : status);
	}

	private List<String> splitAndTrim(String compiled) {
		return map(List.of(compiled.split(BuildConstants.SEPARATOR)), String::trim);
	}


	public static <T, V> List<V> map(Collection<? extends T> collection, Function<? super T, ? extends V> mapping) {
		if (collection.isEmpty()) return Collections.emptyList();
		return collection.stream().map(mapping::apply).collect(Collectors.toCollection(() -> new ArrayList<>(collection.size())));
	}

	private static class Sentinel {
		private final File file;
		private DirectorySentinel sentinel;

		private Sentinel(File file) {
			this.file = file;
		}

		public void init(Consumer<String> consumer) {
			try {
				File directory = file.getParentFile();
				if (!directory.exists())
					Logger.warn("Directory to listen" + directory.getAbsolutePath() + " not found");
				else {
					AtomicInteger bytesRead = new AtomicInteger();
					sentinel = new DirectorySentinel(directory, (f, event) -> {
						if (f.equals(file) && event.equals(DirectorySentinel.Event.OnCreate) || event.equals(DirectorySentinel.Event.OnModify))
							bytesRead.set(notifyChanges(file, bytesRead, consumer));
					}, DirectorySentinel.Event.OnModify);
					sentinel.watch();
				}
			} catch (Exception e) {
				Logger.error(e.getMessage());
			}
		}

		private static int notifyChanges(File file, AtomicInteger bytesRead, Consumer<String> consumer) {
			try {
				byte[] bytes = Files.readAllBytes(file.toPath());
				int from = bytesRead.get();
				if (from >= bytes.length) return from;
				byte[] newBytes = Arrays.copyOfRange(bytes, from, bytes.length);
				consumer.accept(new String(newBytes));
				return bytes.length;
			} catch (IOException e) {
				Logger.error(e);
			}
			return 0;
		}

		public void stop() {
			sentinel.stop();
		}
	}
}
