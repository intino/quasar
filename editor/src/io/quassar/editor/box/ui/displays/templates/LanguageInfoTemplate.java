package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.events.SelectionEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.ui.types.ForgeView;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.LanguageProperty;
import io.quassar.editor.model.Visibility;

import java.util.Arrays;
import java.util.List;

public class LanguageInfoTemplate extends AbstractLanguageInfoTemplate<EditorBox> {
	private Language language;
	private String release;

	public LanguageInfoTemplate(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void release(String release) {
		this.release = release;
	}

	@Override
	public void init() {
		super.init();
		editorStamp.onChangeId(this::rename);
		editorStamp.onChangeLogo(e -> save(LanguageProperty.Logo, e));
	}

	@Override
	public void refresh() {
		super.refresh();
		editorStamp.language(language);
		editorStamp.refresh();
		refreshProperties();
	}

	private void refreshProperties() {
		propertiesStamp.language(language);
		propertiesStamp.release(release);
		propertiesStamp.refresh();
	}

	private void rename(String newName) {
		box().commands(LanguageCommands.class).rename(language, newName, username());
	}

	private void save(LanguageProperty property, Object value) {
		box().commands(LanguageCommands.class).save(language, property, value, username());
	}

}