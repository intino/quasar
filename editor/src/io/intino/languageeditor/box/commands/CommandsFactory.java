package io.intino.languageeditor.box.commands;

import io.intino.languageeditor.box.LanguageEditorBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandsFactory {
	private final LanguageEditorBox box;

	private static final Map<Class<? extends Commands>, Function<LanguageEditorBox, ? extends Commands>> builder = new HashMap<>();

	public CommandsFactory(LanguageEditorBox box) {
		this.box = box;
		buildCommands();
	}

	private void buildCommands() {
		builder.put(WorkspaceCommands.class, WorkspaceCommands::new);
	}

	public <T extends Commands> T command(Class<T> clazz) {
		return (T) builder.get(clazz).apply(box);
	}

}
