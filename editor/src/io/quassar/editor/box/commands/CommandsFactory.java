package io.quassar.editor.box.commands;

import io.quassar.editor.box.EditorBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandsFactory {
	private final EditorBox box;

	private static final Map<Class<? extends Commands>, Function<EditorBox, ? extends Commands>> builder = new HashMap<>();

	public CommandsFactory(EditorBox box) {
		this.box = box;
		buildCommands();
	}

	private void buildCommands() {
		builder.put(ModelCommands.class, ModelCommands::new);
		builder.put(CollectionCommands.class, CollectionCommands::new);
		builder.put(LanguageCommands.class, LanguageCommands::new);
		builder.put(UserCommands.class, UserCommands::new);
	}

	public <T extends Commands> T commands(Class<T> clazz) {
		return (T) builder.get(clazz).apply(box);
	}

}
