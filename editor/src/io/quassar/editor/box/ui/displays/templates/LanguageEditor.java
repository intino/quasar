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
import java.nio.file.Files;
import java.util.function.Consumer;

public class LanguageEditor extends AbstractLanguageEditor<EditorBox> {
	private Language language;
	private boolean logoExists;
	private Consumer<Boolean> checkIdListener;
	private Consumer<String> changeIdListener;
	private Consumer<File> changeLogoListener;

	public LanguageEditor(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void onCheckId(Consumer<Boolean> listener) {
		this.checkIdListener = listener;
	}

	public void onChangeId(Consumer<String> listener) {
		this.changeIdListener = listener;
	}

	public void onChangeLogo(Consumer<File> listener) {
		this.changeLogoListener = listener;
	}

	public String languageId() {
		return idField.value();
	}

	public File logo() {
		if (!logoExists) return null;
		File tmpFile = new File(box().archetype().tmp().root(), id() + "-logo.png");
		return tmpFile.exists() ? tmpFile : (language != null ? box().languageManager().loadLogo(language) : null);
	}

	public void focus() {
		idField.focus();
	}

	public boolean check() {
		return DisplayHelper.checkLanguageId(idField, this::translate, box()) && DisplayHelper.checkLanguageInUse(idField, this::translate, box());
	}

	@Override
	public void init() {
		super.init();
		idField.onEnterPress(e -> changeName());
		idField.onChange(e -> refreshState());
		changeId.onExecute(e -> changeName());
		changeId.signChecker((sign, reason) -> sign.equals(idField.value()));
		logoField.onChange(this::updateLogo);
		generateLogo.onExecute(e -> generateLogo());
	}

	@Override
	public void refresh() {
		super.refresh();
		File logo = language != null ? box().languageManager().loadLogo(language) : null;
		logoExists = logo != null && logo.exists();
		idField.value(language != null ? language.key() : null);
		logoField.value(logoExists ? logo : null);
		changeId.visible(language != null);
		generateLogo.readonly(language == null);
		if (changeId.isVisible()) changeId.readonly(true);
	}

	private void refreshState() {
		boolean sameName = language == null || Language.nameFrom(idField.value()).equals(language.name());
		boolean valid = sameName && DisplayHelper.checkLanguageId(idField, this::translate, box()) && (language != null || DisplayHelper.checkLanguageInUse(idField, this::translate, box()));
		if (!sameName) idField.error(translate("Language can't change its language name part (%s) once created").formatted(language.name()));
		boolean emptyId = idField.value() == null || idField.value().isEmpty();
		validIdIcon.visible(valid && !emptyId);
		invalidIdIcon.visible(!valid && !emptyId);
		generateLogo.readonly(emptyId);
		changeId.readonly(!valid);
		if (checkIdListener != null) checkIdListener.accept(valid);
	}

	private void generateLogo() {
		File destiny = logoFile();
		if (destiny.exists()) destiny.delete();
		LanguageHelper.generateLogo(Language.nameFrom(idField.value()), destiny);
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
			logoField.value(value != null ? (language != null ? box().languageManager().loadLogo(language) : tmpFile) : null);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@NotNull
	private File logoFile() {
		return new File(box().archetype().tmp().root(), id() + "-logo.png");
	}

	private void changeName() {
		if (changeIdListener == null) return;
		changeIdListener.accept(idField.value());
		refresh();
	}

}