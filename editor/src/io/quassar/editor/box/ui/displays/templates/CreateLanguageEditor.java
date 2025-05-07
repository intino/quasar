package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.ui.displays.UserMessage;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.commands.LanguageCommands;
import io.quassar.editor.box.util.ModelHelper;
import io.quassar.editor.model.Language;
import io.quassar.editor.model.Model;

import java.util.function.Consumer;

public class CreateLanguageEditor extends AbstractCreateLanguageEditor<EditorBox> {
	private Model metamodel;
	private Consumer<Language> createListener;
	private String release;

	public CreateLanguageEditor(EditorBox box) {
		super(box);
	}

	public void model(Model metamodel) {
		this.metamodel = metamodel;
	}

	public void release(String release) {
		this.release = release;
	}

	public void onCreate(Consumer<Language> listener) {
		this.createListener = listener;
	}

	@Override
	public void init() {
		super.init();
		editorStamp.onCheckId(valid -> create.readonly(!valid));
		editorStamp.onChangeId(valid -> createLanguage());
		create.onExecute(e -> createLanguage());
	}

	@Override
	public void refresh() {
		super.refresh();
		metamodelTitle.value("Create DSL");
		editorStamp.focus();
	}

	private void createLanguage() {
		if (!editorStamp.check()) return;
		create.readonly(true);
		notifyUser("Creating language...", UserMessage.Type.Loading);
		Language language = box().commands(LanguageCommands.class).create(editorStamp.languageId(), metamodel, Language.Level.L1, editorStamp.logo(), username());
		hideUserNotification();
		create.readonly(false);
		createListener.accept(language);
	}

}