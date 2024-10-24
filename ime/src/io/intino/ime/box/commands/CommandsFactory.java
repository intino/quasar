package io.intino.ime.box.commands;

import io.intino.ime.box.ImeBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandsFactory {
	private final ImeBox box;

	private static final Map<Class<? extends Commands>, Function<ImeBox, ? extends Commands>> builder = new HashMap<>();

	public CommandsFactory(ImeBox box) {
		this.box = box;
		buildCommands();
	}

	private void buildCommands() {
		builder.put(ModelCommands.class, ModelCommands::new);
		builder.put(LanguageCommands.class, LanguageCommands::new);
		builder.put(UserCommands.class, UserCommands::new);
	}

	public <T extends Commands> T command(Class<T> clazz) {
		return (T) builder.get(clazz).apply(box);
	}

}
