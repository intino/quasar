package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.DisplayHelper;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Language;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.function.Consumer;

public class LanguageEditor extends AbstractLanguageEditor<EditorBox> {
	private Language language;
	private boolean logoExists;
	private Consumer<Boolean> checkNameListener;
	private Consumer<String> changeNameListener;
	private Consumer<File> changeLogoListener;

	public LanguageEditor(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onCheckName(Consumer<Boolean> listener) {
		this.checkNameListener = listener;
	}

	public void onChangeName(Consumer<String> listener) {
		this.changeNameListener = listener;
	}

	public void onChangeLogo(Consumer<File> listener) {
		this.changeLogoListener = listener;
	}

	public String name() {
		return nameField.value();
	}

	public File logo() {
		if (!logoExists) return null;
		File tmpFile = new File(box().archetype().tmp().root(), id() + "-logo.png");
		return tmpFile.exists() ? tmpFile : (language != null ? box().languageManager().loadLogo(language) : null);
	}

	public void focus() {
		nameField.focus();
	}

	public boolean check() {
		return DisplayHelper.checkLanguageName(nameField, this::translate, box());
	}

	@Override
	public void init() {
		super.init();
		nameField.onEnterPress(e -> notifyChangeName());
		nameField.onChange(e -> refreshState());
		changeName.onExecute(e -> changeName());
		changeName.signChecker((sign, reason) -> sign.equals(nameField.value()));
		logoField.onChange(this::updateLogo);
		generateLogo.onExecute(e -> generateLogo());
	}

	@Override
	public void refresh() {
		super.refresh();
		File logo = language != null ? box().languageManager().loadLogo(language) : null;
		logoExists = logo != null && logo.exists();
		nameField.value(language != null ? language.name() : null);
		logoField.value(logoExists ? logo : null);
		changeName.visible(language != null);
		generateLogo.readonly(language == null);
		if (changeName.isVisible()) changeName.readonly(true);
	}

	private void refreshState() {
		boolean valid = DisplayHelper.checkLanguageName(nameField, this::translate, box());
		boolean emptyName = nameField.value() == null || nameField.value().isEmpty();
		validNameIcon.visible(valid && !emptyName);
		invalidNameIcon.visible(!valid && !emptyName);
		generateLogo.readonly(emptyName);
		changeName.readonly(!valid || (language != null && language.name().equals(nameField.value())));
		if (checkNameListener != null) checkNameListener.accept(valid);
	}

	private void generateLogo() {
		File destiny = logoFile();
		if (destiny.exists()) destiny.delete();
		LanguageHelper.generateLogo(nameField.value(), destiny);
		if (changeLogoListener != null) changeLogoListener.accept(destiny);
		logoField.value(language != null ? box().languageManager().loadLogo(language) : destiny);
	}

	private void updateLogo(ChangeEvent event) {
		try {
			File tmpFile = logoFile();
			if (tmpFile.exists()) tmpFile.delete();
			Resource value = event.value();
			logoExists = value != null;
			if (value != null) Files.write(tmpFile.toPath(), value.bytes());
			if (changeLogoListener != null) changeLogoListener.accept(value != null ? logo() : null);
			logoField.value(value != null ? box().languageManager().loadLogo(language) : null);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@NotNull
	private File logoFile() {
		return new File(box().archetype().tmp().root(), id() + "-logo.png");
	}

	private void notifyChangeName() {
		if (changeNameListener == null) return;
		changeNameListener.accept(nameField.value());
	}

	private void changeName() {
		if (changeNameListener == null) return;
		changeNameListener.accept(nameField.value());
		refresh();
	}

}