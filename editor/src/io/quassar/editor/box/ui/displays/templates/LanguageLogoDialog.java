package io.quassar.editor.box.ui.displays.templates;

import io.intino.alexandria.Resource;
import io.intino.alexandria.logger.Logger;
import io.intino.alexandria.ui.displays.events.ChangeEvent;
import io.quassar.editor.box.EditorBox;
import io.quassar.editor.box.util.LanguageHelper;
import io.quassar.editor.model.Language;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Function;

public class LanguageLogoDialog extends AbstractLanguageLogoDialog<EditorBox> {
	private Language language;
	private Function<Boolean, String> idProvider;
	private Function<Boolean, String> nameProvider;
	private Consumer<File> changeLogoListener;
	private boolean logoExists;
	private File selectedLogo;

	public LanguageLogoDialog(EditorBox box) {
		super(box);
	}

	public void language(Language language) {
		this.language = language;
	}

	public void languageIdProvider(Function<Boolean, String> provider) {
		this.idProvider = provider;
	}

	public void languageNameProvider(Function<Boolean, String> provider) {
		this.nameProvider = provider;
	}

	public void onChangeLogo(Consumer<File> listener) {
		this.changeLogoListener = listener;
	}

	public File logo() {
		if (!logoExists) return null;
		File tmpFile = new File(box().archetype().tmp().root(), id() + "-logo.png");
		return tmpFile.exists() ? tmpFile : (language != null ? box().languageManager().loadLogo(language) : null);
	}

	public void readonly(boolean value) {
		openDialogTrigger.readonly(value);
	}

	@Override
	public void init() {
		super.init();
		logoField.onChange(this::changeLogo);
		generateLogo.onExecute(e -> generateLogo());
		dialog.onOpen(e -> refreshDialog());
		dialog.onClose(e -> refresh());
		changeLogo.onExecute(e -> saveLogo());
	}

	@Override
	public void refresh() {
		super.refresh();
		File logo = selectedLogo != null && selectedLogo.exists() ? selectedLogo : language != null ? box().languageManager().loadLogo(language) : null;
		logoExists = logo != null && logo.exists();
		if (logoExists) this.logo.value(logo);
		else this.logo.value(LanguageLogoDialog.class.getResource("/images/language-logo.png"));
	}

	private void refreshDialog() {
		File logo = selectedLogo != null && selectedLogo.exists() ? selectedLogo : language != null ? box().languageManager().loadLogo(language) : null;
		logoExists = logo != null && logo.exists();
		if (logoExists) logoPreview.value(logo);
		else logoPreview.value(LanguageLogoDialog.class.getResource("/images/language-logo.png"));
		logoField.value((URL) null);
		languageId.value(idProvider.apply(true));
	}

	private void generateLogo() {
		File destiny = logoFile();
		if (destiny.exists()) destiny.delete();
		selectedLogo = destiny;
		LanguageHelper.generateLogo(nameProvider.apply(true), destiny);
		logoPreview.value(destiny);
	}

	private void changeLogo(ChangeEvent event) {
		try {
			File tmpFile = logoFile();
			if (tmpFile.exists()) tmpFile.delete();
			Resource value = event.value();
			logoExists = value != null;
			if (value != null) Files.write(tmpFile.toPath(), value.bytes());
			logoPreview.value(value != null ? tmpFile : null);
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@NotNull
	private File logoFile() {
		return new File(box().archetype().tmp().root(), id() + "-logo.png");
	}

	private void saveLogo() {
		dialog.close();
		File logo = logoFile().exists() ? logoFile() : language != null ? box().languageManager().loadLogo(language) : null;
 		if (changeLogoListener != null) changeLogoListener.accept(logo);
		logo = logoFile().exists() ? logoFile() : language != null ? box().languageManager().loadLogo(language) : null;
		selectedLogo = logo;
		this.logo.value(logo);
	}

}