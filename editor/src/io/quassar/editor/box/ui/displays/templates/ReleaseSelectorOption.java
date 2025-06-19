package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.components.selector.SelectorOption;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.model.Language;

import java.util.UUID;
import java.util.function.Consumer;

public class ReleaseSelectorOption extends AbstractReleaseSelectorOption<EditorBox> implements SelectorOption {
	private Language language;
	private String release;
	private Consumer<String> removeListener;

	public ReleaseSelectorOption(EditorBox box) {
		super(box);
		id(UUID.randomUUID().toString());
	}

	public ReleaseSelectorOption language(Language language) {
		this.language = language;
		return this;
	}

	public String release() {
		return release;
	}

	public ReleaseSelectorOption release(String release) {
		name(release);
		this.release = release;
		return this;
	}

	public ReleaseSelectorOption onRemove(Consumer<String> listener) {
		this.removeListener = listener;
		return this;
	}

	@Override
	public void update() {
		super.update();
		refresh();
	}

	@Override
	public void init() {
		super.init();
		remove.onExecute(e -> removeListener.accept(release));
		remove.signChecker((sign, reason) -> release.equals(sign));
	}

	@Override
	public void refresh() {
		super.refresh();
		version.value(release);
		remove.visible(language.release(release) != null);
	}

}